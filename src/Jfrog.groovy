/*
*  Wrapper for jFrog Artifactory REST APIs
*  https://jfrog.com/help/r/jfrog-rest-apis/artifactory-rest-apis
*/

import groovy.json.JsonBuilder
import groovy.json.JsonSlurperClassic

class Jfrog
{

    private def ctx
    private def instance
    private def url

    Jfrog(ctx, name){ this.ctx = ctx }
    
    def getUrl()
    {
        if (this.url)
            return this.url
        def instance = Utilities.getConstant('artifactory')[name]
        return this.url = "${this.instance.schema}://${this.instance.domain}"
    }

    def promote(buildName, buildNumber, sourceRepo, targetRepo,
        status="promoted", comment='', properties=[:], ciUser='', dry=false)
    {
        def bearer = "Bearer " + this.ctx.token
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
            customHeaders: [[maskValue: false, name: 'Authorization', value: bearer]],
            authentication: 'aws-artifactory1-publisher')
        return (new JsonSlurperClassic()).parseText(response.content)
    }

}
