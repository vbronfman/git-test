import groovy.json.JsonBuilder
import groovy.json.JsonSlurperClassic

class Utilities implements Serializable
{
    private def steps

    Utilities(steps) {
        this.steps = steps
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
            ],
            MDC: [
                domain: "jfrog-mdc.gilat.tech",
                schema: "https",
                credId: "mdc-artifactory-admin", // required to create/delete local repos
            ],
        ]
        def constants = [
            artifactory: artifactory,
            defaultArtifactoryDomainName: artifactory.IL.domain,
            defaultArtifactorySchema: artifactory.IL.schema,
            gitCredsSE4SSH: 'azure-worker-ssh-msharay',
            releaseNotesJob: 'Developers/Release_notes',
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

    def gitGetCommit()
    {
        return steps.sh(
            returnStdout:  true,
            script: """
                git rev-parse HEAD
            """).strip()
    }

    // def gitGetLatestCommit()
    // {
    //     import hudson.matrix.*
    //     import hudson.model.*

    //     hudsonInstance = hudson.model.Hudson.instance
    //     jobNames = hudsonInstance.getJobNames()
    //     allItems = []
    //     for (name in jobNames) {
    //     allItems += hudsonInstance.getItemByFullName(name)
    //     }

    //     // Iterate over all jobs and find the ones that have a hudson.plugins.git.util.BuildData
    //     // as an action.
    //     //
    //     // We then clean it by removing the useless array action.buildsByBranchName
    //     //

    //     for (job in allItems) {
    //         println("job: " + job.name);
    //         def counter = 0;
    //         for (build in job.getBuilds()) {
    //             // It is possible for a build to have multiple BuildData actions
    //             // since we can use the Mulitple SCM plugin.
    //             def gitActions = build.getActions(hudson.plugins.git.util.BuildData.class)
    //             if (gitActions != null) {
    //                 for (action in gitActions) {
    //                     action.buildsByBranchName = new HashMap<String, Build>();
    //                     hudson.plugins.git.Revision r = action.getLastBuiltRevision();
    //                     if (r != null) {
    //                         for (branch in r.getBranches()) {
    //                             action.buildsByBranchName.put(branch.getName(), action.lastBuild)
    //                         }
    //                     }
    //                     build.actions.remove(action)
    //                     build.actions.add(action)
    //                     build.save();
    //                     counter++;
    //                 }
    //             }
    //         }
    //     }
    // }

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
                git tag  ${ config.force ? '--force' : '' } ${config.tag}
                git push ${ config.force ? '--force' : '' } ${config.url} ${config.tag}
            """
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
