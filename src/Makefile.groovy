class Makefile implements Serializable {
    private def steps

    Makefile(steps){
        this.steps = steps
    }

    def buildCmake(buildType, dir, clean) {
        def extraMakeFlags = ""
        def extraBuildFlags = ""

        if (dir) {
            extraMakeFlags += "BUILD_DIR=${dir}"
        }

        if (clean) {
            extraBuildFlags += "--clean-first"
        }

        steps.sh """make build BUILD_TYPE=${buildType} BUILD_FLAGS='--parallel 12 ${extraBuildFlags}' ${extraMakeFlags} """
    }

    def formatClang() {
        steps.sh '''make format FORMAT_FLAGS='--Werror --dry-run' '''
    }

    def packit(branch, sharepoint) {
        def dbAddr = Utilities.getConstant('versionGeneratorUrl')

        steps.withCredentials([steps.usernamePassword(credentialsId: 'postgres-user-for-production', usernameVariable: 'dbUser', passwordVariable: 'dbPass')]) {
            steps.sh """make packit VERSION_DB=\\"${dbAddr}?user=\$dbUser\\&password=\$dbPass\\" BRANCH=${branch} SHAREPOINT=${sharepoint ? 'sharepoint' : 'nosharepoint'} """
        }
    }

    def publish(repo, branch, notify) {
        def artifactory = Utilities.getConstant('artifactory').AWS
        def url = "${artifactory.schema}://${artifactory.domain}/artifactory"
        def pretty = ''

        steps.withCredentials([steps.string(credentialsId: 'aws-artifactory1-auth', variable: 'TOKEN')]) {
            pretty = steps.sh(
                returnStdout: true,
                script: """make publish REPO='${url}/${repo}' TOKEN=\$TOKEN BRANCH=${branch} """).trim().tokenize().last())
        }

        if (notify) {
            emailext(
                subject: "SUCCESSFUL: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'",
                body: """<p>SUCCESSFUL: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]':</p>
                    <p>Package is available at &QUOT;<a href='${pretty}'>${pretty}</a>&QUOT;</p>
                    <p>Check console output at &QUOT;<a href='${env.BUILD_URL}'>${env.JOB_NAME} [${env.BUILD_NUMBER}]</a>&QUOT;</p>""",
                to: notify,
                mimeType: 'text/html')
        }
    }

    def sast() {
        steps.sh 'make sast'
    }
}
