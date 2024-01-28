class DockerTools implements Serializable {
    private def steps
    private def runtimeVars

    DockerTools(steps) {
        this.steps = steps
        this.runtimeVars = new RuntimeVars(steps)
    }

    def fqin(config, tag) {
        return "${config.registry}/${config.name}:${tag}"
    }

    def createImage(config) {
        def unique
        def createAlias = false

        if (!config.tag) {
            unique = "${steps.sh(script: "date +%Y%m%d", returnStdout: true).trim()}.${steps.currentBuild.number}"
            config.tag = unique
        }
        else {
            unique = "${config.tag}-${steps.sh(script: "date +%Y%m%d", returnStdout: true).trim()}.${steps.currentBuild.number}"
            createAlias = true
        }

        def fqTag    = fqin(config, config.tag)
        def fqUnique = fqin(config, unique)

        runtimeVars.send([
            TAG:        config.tag,
            UNIQUE:     uniqueת
            SHORT_NAME: "${config.name}:${config.tag}",
            FULL_NAME:  fqTag])
        steps.currentBuild.displayName = "#${unique}"
        steps.sh """
            docker image build --no-cache --force-rm -t ${fqUnique} .
            docker image push ${fqUnique}
        """

        if (createAlias) {
            steps.sh """
                docker image tag ${fqUnique} ${fqTag}
                docker image push ${fqTag}
            """
        }

        if (!config.noClean) {
            steps.sh """
                docker image rm ${fqUnique}
                ${ createAlias ? "docker image rm ${fqTag}" : '' }
            """
        }
    }
}
