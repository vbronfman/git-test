class DockerTools implements Serializable {
    private def steps

    DockerTools(steps) {
        this.steps = steps
    }

    def build(name) {
        def date = steps.sh(script: "date +%Y.%m.%d", returnStdout: true).trim()
        steps.env.VERSION = "${date}.${steps.currentBuild.number}"
        steps.env.ARTIFACTORY_URL = ${Utilities.getConstant('artifactoryPackitURL')}
        steps.currentBuild.displayName = "#${steps.env.VERSION}"
        steps.sh "docker build --no-cache --force-rm -t ${Utilities.getConstant('artifactoryPackitURL')}/${name}:${steps.env.VERSION} ."
    }

    def publish(name) {
        steps.sh """docker push ${Utilities.getConstant('artifactoryPackitURL')}/${name}:${steps.env.VERSION} """
    }
}