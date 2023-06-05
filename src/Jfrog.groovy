/*
*  Wrapper for jFrog Artifactory REST APIs
*  https://jfrog.com/help/r/jfrog-rest-apis/artifactory-rest-apis
*/

import groovy.json.JsonBuilder
import groovy.json.JsonSlurperClassic

class Jfrog
{

    private def ctx

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
        def path = "/api/build/promote/${buildName}/${buildNumber}"
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
        def response = this.ctx.httpRequest(consoleLogResponseBody: true,
            httpMode: 'POST', ignoreSslErrors: true, responseHandle: 'NONE',
            wrapAsMultipart: false, url: "${this.getUrl()}/${path}", requestBody: body,
            authentication: 'aws-artifactory1-publisher')
        return (new JsonSlurperClassic()).parseText(response.content)
    }

}
