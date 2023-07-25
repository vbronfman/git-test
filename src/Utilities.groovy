import groovy.json.JsonBuilder
import groovy.json.JsonSlurperClassic
class Utilities
{
    // get global constant
    static def getConstant(name)
    {
        def artifactory = [
                AWS: [
                    domain: "jfrog.rnd.gilat.tech",
                    schema: "https",
                    credId:  "aws-artifactory1-publisher",
                ],
                IL: [
                    domain: "jfrog.gilat.tech",
                    schema: "https",
                    credId: "il-artifactory1-publisher",
                ]
        ]
        def constants = [
            artifactory: artifactory,
            defaultArtifactoryDomainName: artifactory.IL.domain,
            defaultArtifactorySchema: artifactory.IL.schema,
        ]
        constants << [defaultArtifactoryUrl: "${constants.defaultArtifactorySchema}://${constants.defaultArtifactoryDomainName}/artifactory"]
        constants << [versionGeneratorUrl: 'postgresql://g-versions-db.gilat.com:5432/se.4-versions-production']
        return constants.get(name)
    }

    // shortcut for scm checkout step
    static def gitClone(ctx, opt)
    {
        def extensions = []
        if (opt.dstPath)
            extensions << [$class: 'RelativeTargetDirectory', relativeTargetDir: opt.dstPath]
        if (opt.srcDirs)
        {
            def sparseCheckoutPaths = []
            for (path in opt.srcDirs)
                sparseCheckoutPaths << [$class: 'SparseCheckoutPath', path: path]
            extensions << [
                $class: 'SparseCheckoutPaths',
                sparseCheckoutPaths: sparseCheckoutPaths
            ]
        }
        if (opt.submodules) {
            extensions << ctx.submodule(parentCredentials: true, reference: '')
        }

        return ctx.checkout([
            $class: 'GitSCM',
            branches: [[name: opt.branch]],
            extensions: extensions,
            userRemoteConfigs: [[
                credentialsId: '47aa787b-0e1b-44bb-9fd7-04857bbc58f4',
                url: "https://GilatDevOps@dev.azure.com/GilatDevOps/${opt.repo}"
            ]]
        ])
    }

    static def request(ctx, mode, url, body='', cred='')
    {
        def b = body
        def result = [:]
        def response
        def error = false
        try
        {
            if (!(b instanceof String))
                b = (new JsonBuilder(b).toString())
            response = ctx.httpRequest(
                consoleLogResponseBody: true,
                httpMode: mode,
                validResponseCodes: "100:599",
                responseHandle: 'NONE',
                ignoreSslErrors: true,
                wrapAsMultipart: false,
                url: "${url}",
                requestBody: b,
                customHeaders: [[
                    maskValue: false,
                    name: 'Content-type',
                    value: "application/json"
                ]],
                authentication: cred
            )
            if (response.content)
            {
                error = !(response.status>=100 && response.status<=399)
                try{
                    result = (new JsonSlurperClassic()).parseText(response.content)
                } catch(e){ result = [message: response.content] }
            }
        } catch(e){
            result = [message: "$e", response: response]
            error = true
        }
        return [result, error]
    }

}
