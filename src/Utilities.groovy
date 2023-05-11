class Utilities
{
    // get global constant
    static def getConstant(name)
    {
        def constants = [
            defaultArtifactoryDomainName: "jfrog.gilat.com",
            defaultArtifactoryScheme: "https",
        ]
        constants << [defaultArtifactoryUrl: "${constants.defaultArtifactoryScheme}://${constants.defaultArtifactoryDomainName}/artifactory"]
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
                credentialsId: 'af7ca964-7371-4a18-aaf4-66e791da3650',
                url: "https://GilatDevOps@dev.azure.com/GilatDevOps/${opt.repo}"
            ]]
        ])
    }

}
