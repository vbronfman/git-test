class DockerTools implements Serializable {
    private def steps

    DockerTools(steps) {
        this.steps = steps
    }

    def createImage(config) {
        if (!config.tag)
        {
            config.tag = "${steps.sh(script: "date +%Y.%m.%d", returnStdout: true).trim()}.${steps.currentBuild.number}"
        }
        def fqin = "${config.registry}/${config.name}:${config.tag}"
        steps.currentBuild.displayName = "#${config.tag}"
        steps.sh """
            docker image build --no-cache --force-rm -t ${fqin} .
            docker image push ${fqin}
            ${ !config.noClean ? "docker image rm ${fqin}" : }
        """
    }
}
