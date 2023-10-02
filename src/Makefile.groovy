class Makefile implements Serializable {
    private def steps

    Makefile(steps) {
        this.steps = steps
    }

    def buildGeneric(config) {
        def buildCmd = 'build'
        def typeFlag = ''
        def extraMakeFlags = ''

        if (config.part) {
            buildCmd = "build-${config.part}"
        }

        if (config.buildType) {
            typeFlag = "BUILD_TYPE=${config.buildType}"
        }

        if (config.dir) {
            extraMakeFlags += "BUILD_DIR=${config.dir}"
        }

        config.extra="${config.extra ? config.extra : ''}"
        
        steps.sh """make ${buildCmd} ${typeFlag} ${config.extra} ${extraMakeFlags} """
    }

    def buildCmake(config) {
        def extraBuildFlags = ''
        def extra = ''

        if (config.clean) {
            extraBuildFlags += '--clean-first'
        }
        config.extra = """BUILD_FLAGS='--parallel 12 ${extraBuildFlags}' """

        buildGeneric(config)
    }

    def clean(part) {
        def cmd = 'clean'

        if (part) {
            cmd = "clean-${part}"
        }

        steps.sh """make ${cmd} """
    }

    def formatClang() {
        steps.sh '''make format FORMAT_FLAGS='--Werror --dry-run --ferror-limit=1' '''
    }

    def packit(branch, sharepoint, v) {
        if (!v) {
            def dbAddr = Utilities.getConstant('versionGeneratorUrl')

            steps.withCredentials([steps.usernamePassword(credentialsId: 'postgres-user-for-production', usernameVariable: 'dbUser', passwordVariable: 'dbPass')]) {
                steps.sh """make packit VERSION_DB=\\"${dbAddr}?user=\$dbUser\\&password=\$dbPass\\" BRANCH=${branch} SHAREPOINT=${sharepoint ? 'sharepoint' : 'nosharepoint'} """
            }
        } else {
            steps.withCredentials([steps.string(credentialsId: 'postgres-creds-base64-production', variable: 'dbCreds')]) {
                steps.sh """make packit VERSION_DB=\$dbCreds BRANCH=${branch} SHAREPOINT=${sharepoint ? 'sharepoint' : 'nosharepoint'} """
            }
        }
    }

    def publish(repo, gdf, component, project) {
        steps.dir('work/packit') {
            def res, err
            project = project ?: "se-iv"
            gdf = gdf ?: "config.gdfx"
            def v = steps.sh(
                returnStdout:  true,
                script: """gawk 'match(\$0, /GdfxVersion="([0-9.]*)"/, a) { print a[1] }' "${gdf}" """).strip()
            def folder = (v =~ /\d+.\d+/)[0]
            def target = "${repo}/${folder}/${v}/"

            try {
                (res, err) = steps.vision().setProject(project).publishArtifacts(
                    component,
                    [ [pattern: '*', path: "${folder}/${v}/"] ],
                    [ sync: true, version: v ]
                )
                if (err)
                    throw new Exception(res.message)
            } catch(e) {
                steps.echo "Error piblishing to vision: $e"
                steps.currentBuild.result = "UNSTABLE"
                steps.echo "Failed to publish via Vision. Retry using jfrog plugin"
                steps.jfrog("AWS").publishArtifacts(
                    [ [pattern: '*', target: target] ],
                    [ sync: true, name: repo ]
                )
            }
            steps.env.ARTIFACT_URL = steps.jfrog("IL").targetToURL(target)
            steps.env.ARTIFACT_VERSION = v
        }
    }

    def sast() {
        steps.sh 'make sast'
    }
}
