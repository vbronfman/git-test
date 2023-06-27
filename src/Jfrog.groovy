/*
*  Wrapper for:
*  - jFrog Artifactory REST APIs: https://jfrog.com/help/r/jfrog-rest-apis/artifactory-rest-apis
*  - jFrog Artifactory plugin: https://jfrog.com/help/r/jfrog-integrations-documentation/scripted-pipeline-syntax
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
    
    def publishArtifacts(files, opt=[:])
    {
        def art = this.ctx.Artifactory.server this.name
        def props = opt.props ?: [:]
        if (!props.BUILD_USER)
            props << [BUILD_USER: this.ctx.env.BUILD_USER ?: 'unknown']
        def publish_files = []
        for (file in files) {
            def file_props = file.props ?: [:]
            file_props << props
            file.props = file_props.inject(""){list, k, v -> "${k}=${v};${list}"}
            publish_files << file
        }
        def uploadSpec = ( new JsonBuilder(files: publish_files).toString() )
        if (opt.name)
        {
            def buildInfo = art.upload uploadSpec, module: opt.name
            buildInfo.name = opt.name
        }
        else
            def buildInfo = art.upload uploadSpec
        if (opt.keepLast)
            buildInfo.retention maxBuilds: opt.keepLast, deleteBuildArtifacts: true, async: !!opt.sync
        art.publishBuildInfo buildInfo
        return buildInfo
    }

    def getUrl()
    {
        def instance = Utilities.getConstant('artifactory')[this.name]
        return "${instance.schema}://${instance.domain}"
    }

    def targetToURL(target)
    {
        return "${this.getUrl()}/artifactory/${target}"
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
        // if you use buld obtained with getBuild(), pass into this method only buildInfo field
        this.put("artifactory/api/build", ( new JsonBuilder(build).toString() ))
    }

    def deleteBuild(buildName, buildNumber)
    {
        this.post("artifactory/api/build/delete", ( new JsonBuilder([
            buildName: buildName,
            buildNumbers: [buildNumber],
            deleteArtifacts: true,
            deleteAll: false,
        ]).toString() ))
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
        if (response.content)
        {
            try{
                return (new JsonSlurperClassic()).parseText(response.content)
            } catch(e){ return [message: response.content] }
        }
    }

}
