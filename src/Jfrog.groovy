/*
*  Wrapper for jFrog Artifactory REST APIs
*  https://jfrog.com/help/r/jfrog-rest-apis/artifactory-rest-apis
*/

import groovy.json.JsonBuilder
import groovy.json.JsonSlurperClassic
import org.springframework.web.util.UriUtils

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

    def getAllBuilds()
    {
        this.get("artifactory/api/build/")
    }

    def getBuild(buildName, buildNumber)
    {
        this.get("artifactory/api/build/"+
            "${UriUtils.encodePath(buildName, 'UTF-8')}/${buildNumber}")
    }

    def promoteBuild(buildName, buildNumber, sourceRepo, targetRepo, opt=[:])
    {
        def path = "artifactory/api/build/promote/"+
            "${UriUtils.encodePath(buildName, 'UTF-8')}/${buildNumber}"
        opt = [
            status: "promoted",
            comment: '',
            properties: [:],
            ciUser: '',
            dry: false,
            artifacts : true, 
        ] << opt
        def body = (new JsonBuilder([
            status: opt.status,
            comment: opt.comment,
            ciUser: opt.ciUser,
            sourceRepo : sourceRepo, 
            targetRepo : targetRepo,
            artifacts : opt.artifacts, 
            properties: opt.properties,
            failFast: true,
            dryRun: opt.dry,
        ]).toString())
        this.ctx.echo body
        this.post(path, body)
    }
    
    def uploadBuild(build)
    {
        this.put("/api/build", build.toString())
    }

    private def get(path){ this.request(path, 'GET') }
    private def post(path, body){ this.request(path, 'POST', body) }
    private def put(path, body){ this.request(path, 'PUT', body) }

    private def request(path, mode, body=null)
    {
        def cred = Utilities.getConstant('artifactory')[this.name].credId
        def response = this.ctx.httpRequest(consoleLogResponseBody: true,
            httpMode: mode, ignoreSslErrors: true, responseHandle: 'NONE',
            wrapAsMultipart: false, url: "${this.getUrl()}/${path}", requestBody: body,
            customHeaders: [[maskValue: false, name: 'Content-type', value: "application/json"]],
            authentication: cred)
        return (new JsonSlurperClassic()).parseText(response.content)
    }

}
