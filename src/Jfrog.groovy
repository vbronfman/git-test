/*
*  Wrapper for jFrog Artifactory REST APIs
*  https://jfrog.com/help/r/jfrog-rest-apis/artifactory-rest-apis
*/

import groovy.json.JsonBuilder
import groovy.json.JsonSlurperClassic
import java.net.URLEncoder


class Jfrog
{

    private def ctx
    private def name

    Jfrog(ctx, name){
        this.ctx = ctx
        this.name = name
    }
    
    def getUrl()
    {
        def instance = Utilities.getConstant('artifactory')[this.name]
        return "${instance.schema}://${instance.domain}"
    }

    def promote(buildName, buildNumber, sourceRepo, targetRepo,
        status="promoted", comment='', properties=[:], ciUser='', dry=false)
    {
        def path = "artifactory/api/build/promote/${URLEncoder.encode(buildName)}/${buildNumber}"
        def body = (new JsonBuilder([
            status: status,
            comment: comment,
            ciUser: ciUser,
            sourceRepo : sourceRepo, 
            targetRepo : targetRepo,
            artifacts : true, 
            properties: properties,
            failFast: true,
            dryRun: dry,
        ]).toString())
        this.ctx.echo body
        def response = this.ctx.httpRequest(consoleLogResponseBody: true,
            httpMode: 'POST', ignoreSslErrors: true, responseHandle: 'NONE',
            wrapAsMultipart: false, url: "${this.getUrl()}/${path}", requestBody: body,
            customHeaders: [[maskValue: false, name: 'Content-type', value: "application/json"]],
            authentication: 'aws-artifactory1-publisher')
        return (new JsonSlurperClassic()).parseText(response.content)
    }

}
