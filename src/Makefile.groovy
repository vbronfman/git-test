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

    def publish(repo) {
        steps.dir('work/packit') {
            def v = steps.sh(
                returnStdout:  true,
                script: '''gawk 'match($0, /GdfxVersion="([0-9.]*)"/, a) { print a[1] }' ./config.gdfx''').strip()
            def folder = (v =~ /\d+.\d+/)[0]
            def target = "${repo}/${folder}/${v}/"
            steps.jfrog("AWS").publishArtifacts(
                [
                   [pattern: '*', target: target]
                ],
                [
                    sync: true
                ])
            steps.env.ARTIFACT_URL = steps.jfrog("IL").targetToURL(target)
	    steps.env.ARTIFACT_VERSION = v
        }
    }

    def sast() {
        steps.sh 'make sast'
    }
}
