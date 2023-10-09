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

    def clean(config) {
        def cmd = 'clean'

        if (config.part) {
            cmd = "clean-${part}"
        }

        steps.sh """make ${cmd} """
    }

    def formatClang(config) {
        steps.sh '''make format FORMAT_FLAGS='--Werror --dry-run --ferror-limit=1' '''
    }

    def packit(config) {
        if (!config.v) {
            def dbAddr = Utilities.getConstant('versionGeneratorUrl')

            steps.withCredentials([steps.usernamePassword(credentialsId: 'postgres-user-for-production', usernameVariable: 'dbUser', passwordVariable: 'dbPass')]) {
                steps.sh """make packit VERSION_DB=\\"${dbAddr}?user=\$dbUser\\&password=\$dbPass\\" BRANCH=${config.branch} SHAREPOINT=${config.sharepoint ? 'sharepoint' : 'nosharepoint'} """
            }
        } else {
            steps.withCredentials([steps.string(credentialsId: 'postgres-creds-base64-production', variable: 'dbCreds')]) {
                steps.sh """make packit VERSION_DB=\$dbCreds BRANCH=${config.branch} SHAREPOINT=${config.sharepoint ? 'sharepoint' : 'nosharepoint'} """
            }
        }
    }

    def publish(config) {
        steps.dir('work/packit') {
            def res, err
            project = config.project ?: "se-iv"
            gdf = config.gdf ?: "config.gdfx"
            def v = steps.sh(
                returnStdout:  true,
                script: """gawk 'match(\$0, /GdfxVersion="([0-9.]*)"/, a) { print a[1] }' "${gdf}" """).strip()
            def folder = (v =~ /\d+.\d+/)[0]
            def target = "${config.repo}/${folder}/${v}/"

            try {
                (res, err) = steps.vision().setProject(project).publishArtifacts(
                    config.component,
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
                    [ sync: true, name: config.repo ]
                )
            }
            steps.env.ARTIFACT_URL = steps.jfrog("IL").targetToURL(target)
            steps.env.ARTIFACT_VERSION = v
        }
    }

    def sast(config) {
        steps.sh 'make sast'
    }
}
