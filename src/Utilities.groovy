import groovy.json.JsonBuilder
import groovy.json.JsonSlurperClassic

class Utilities implements Serializable
{
    private def steps
    private def runtimeVars

    Utilities(steps) {
        this.steps = steps
        this.runtimeVars = new RuntimeVars(steps)
    }

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
            gitCredsSE4SSH: 'azure-worker-ssh-msharay',
            dockerRegSE4Embedded: "${artifactory.AWS.domain}/seiv-embedded-docker"
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

    def gitTag(config)
    {        
        if (!config.credentialsId)
        {
            config.credentialsId = steps.scm.userRemoteConfigs[0].credentialsId
        }
        
        if (!config.url)
        {
            config.url = steps.scm.userRemoteConfigs[0].url
        }

        steps.sshagent(credentials: [config.credentialsId]) {
            steps.sh """
                git tag ${config.tag}
                git push ${config.url} ${config.tag}
            """
        }
    }

    def postBuildEmail(config)
    {
        if (!config.recipients) {
            config.recipients = steps.env.BUILD_USER_EMAIL
        }

        def artifactUrl = runtimeVars.recv(['ARTIFACT_URL'])

        switch (config.status) {
            case 'success':
                steps.emailext(
                    subject: "SUCCESSFUL: Job '${steps.env.JOB_NAME} [${steps.env.BUILD_NUMBER}]'",
                    body: """<p>SUCCESSFUL: Job '${steps.env.JOB_NAME} [${steps.env.BUILD_NUMBER}]':</p>
                        <p>Package is available at &QUOT;<a href='${artifactUrl}'>${artifactUrl}</a>&QUOT;</p>
                        <p>Check console output at &QUOT;<a href='${steps.env.BUILD_URL}'>${steps.env.JOB_NAME} [${steps.env.BUILD_NUMBER}]</a>&QUOT;</p>""",
                    to: config.recipients,
                    mimeType: 'text/html')
                break
            case 'failure':
                steps.emailext(
                    subject: "FAILED: Job '${steps.env.JOB_NAME} [${steps.env.BUILD_NUMBER}]'",
                    body: """<p>FAILED: Job '${steps.env.JOB_NAME} [${steps.env.BUILD_NUMBER}]':</p>
                        <p>Check console output at &QUOT;<a href='${steps.env.BUILD_URL}'>${steps.env.JOB_NAME} [${steps.env.BUILD_NUMBER}]</a>&QUOT;</p>""",
                    to: config.recipients,
                    mimeType: 'text/html')
                break
        }
    }

    def static normalizeBranchName(name)
    {
        "/ ".each { c ->
            name = name.replace(c, '-')
        }
        return name.toLowerCase()
    }

}
