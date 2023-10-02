class DockerTools implements Serializable {
    private def steps

    DockerTools(steps){
        this.steps = steps
    }

    def build(name)
    {
        def date = steps.sh(script: 'date +%Y.%m.%d', returnStdout: true).trim()
        env.VERSION = "${date}.${currentBuild.number}"
        env.ARTIFACTORY_URL = ${Utilities.getConstant('artifactoryPackitURL')}
        currentBuild.displayName = "#${env.VERSION}"
        steps.sh "docker build --no-cache --force-rm -t ${Utilities.getConstant('artifactoryPackitURL')}/${name}:${env.VERSION} ."
    }

    def publish(name)
    {
        steps.sh """docker push ${Utilities.getConstant('artifactoryPackitURL')}/${name}:${env.VERSION} """
    }
}