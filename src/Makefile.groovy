class Makefile implements Serializable {
    private def steps

    Makefile(steps){
        this.steps = steps
    }

    def buildCmake(buildType, dir, clean) {
//        def extraMakeFlags = ""
//        def extraBuildFlags = ""
//
//        if (buildDir) {
//            extraMakeFlags += "BUILD_DIR=${dir}"
//        }
//
//        if (clean) {
//            extraBuildFlags += "--clean-first"
//        }
//
//        steps.sh """make build BUILD_TYPE=${buildType} BUILD_FLAGS='--parallel 12 ${extraBuildFlags}' ${extraMakeFlags} """
    }

    def formatClang() {
        steps.sh 'FORMAT_FLAGS="--Werror --dry-run" make format'
    }

    def packit(branch, sharepoint) {
        def dbAddr = Utilities.getConstant('versionGeneratorUrl')

        steps.withCredentials([steps.usernamePassword(credentialsId: 'postgres-user-for-production', usernameVariable: 'dbUser', passwordVariable: 'dbPass')]) {
            steps.sh """make packit VERSION_DB=\\"${dbAddr}?user=\$dbUser\\&password=\$dbPass\\" BRANCH=\\'${branch}\\' SHAREPOINT=${sharepoint ? 'sharepoint' : 'nosharepoint'} """
        }
    }

    def publish(repo, branch) {
        def artifactory = Utilities.getConstant('artifactory').AWS
        def url = "${artifactory.schema}://${artifactory.domain}/artifactory"

        steps.withCredentials([steps.string(credentialsId: 'aws-artifactory1-auth', variable: 'TOKEN')]) {
            steps.sh """make publish REPO=\\'${url}/${repo}\\' TOKEN=\$TOKEN """
        }
    }

    def sast() {
        steps.sh 'make sast'
    }
}
