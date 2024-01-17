class DockerTools implements Serializable {
    private def steps
    private def runtimeVars

    DockerTools(steps) {
        this.steps = steps
        this.runtimeVars = new RuntimeVars(steps)
    }

    def createImage(config) {
        if (!config.tag)
        {
            config.tag = "${steps.sh(script: "date +%Y%m%d", returnStdout: true).trim()}.${steps.currentBuild.number}"
        }

        def fqin = "${config.registry}/${config.name}:${config.tag}"
        runtimeVars.send([
            TAG:        config.tag,
            SHORT_NAME: "${config.name}:${config.tag}",
            FULL_NAME:  fqin])
        steps.currentBuild.displayName = "#${config.tag}"
        steps.sh """
            docker image build --no-cache --force-rm -t ${fqin} .
            docker image push ${fqin}
            ${ !config.noClean ? "docker image rm ${fqin}" : '' }
        """
    }
}
