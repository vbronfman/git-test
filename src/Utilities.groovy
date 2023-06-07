class Utilities
{
    // get global constant
    static def getConstant(name)
    {
        def artifactory = [
                AWS: [
                    domain: "jfrog.rnd.gilat.com",
                    schema: "https",
                    credId:  "aws-artifactory1-publisher",
                ],
                IL: [
                    domain: "jfrog.gilat.com",
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

}
