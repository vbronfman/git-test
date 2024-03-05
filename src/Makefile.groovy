class Makefile implements Serializable {
    private def steps
    private def runtimeVars

    Makefile(steps) {
        this.steps = steps
        this.runtimeVars = new RuntimeVars(steps)
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
        if (!config.branch) {
            config.branch = steps.env.BRANCH_NAME
        }

        steps.withCredentials([steps.string(credentialsId: 'postgres-creds-base64-production', variable: 'dbCreds')]) {
            steps.sh """make packit VERSION_DB=\$dbCreds BRANCH=${config.branch} SHAREPOINT=${config.sharepoint ? 'sharepoint' : 'nosharepoint'} """
        }
    }

    def publish(config) {
        steps.dir('work/packit') {
            def res, err
            def project = config.project ?: 'se-iv'
            def gdf = config.gdf ?: 'config.gdfx'
            def v = steps.sh(
                returnStdout:  true,
                script: """
                    gawk 'match(\$0, /GdfxVersion="([0-9.]*)"/, a) { print a[1] }' "${gdf}"
                """).strip()
            def folder = (v =~ /\d+.\d+/)[0]

            steps.sh '''
                md5sum $(ls -t *.zip | head -n 1) | awk '{print "md5sum of built artifact: " $1}'
            '''

            (res, err) = steps.vision().setProject(project).publishArtifacts(
                config.component,
                [ [pattern: '*', path: "${folder}/${v}/"] ],
                [ sync: true, version: v ]
            )
            if (err)
                throw new Exception(res.message)

            def repo = res['spec']['artifactory_repo']
            def target = "${repo}/${folder}/${v}/"
            runtimeVars.send([
                ARTIFACT_URL:     steps.jfrog('IL').targetToURL(target),
                ARTIFACT_VERSION: v
            ])
        }
    }

    def publishMBC(config) {
        steps.dir('work/mbc') {
            def res, err
            def project = config.project ?: 'se-iv'
            def uid = steps.sh(returnStdout:  true, script: ''' date +%s ''').strip()
            def v = "${config.version}-${uid}"
            def folder = config.version
            steps.sh """
                mv mbcImage.bin  "mbc-${v}.bin"
                [ -f bundle.tar.gz ] && mv bundle.tar.gz "mi-mbc-sideload-${v}.tar.gz" || true
            """

            (res, err) = steps.vision().setProject(project).publishArtifacts(
                config.component,
                [ [pattern: '*', path: "${folder}/${v}/"] ],
                [ sync: true, version: v ]
            )
            if (err)
                throw new Exception(res.message)

            def repo = res['spec']['artifactory_repo']
            def target = "${repo}/${folder}/"
            runtimeVars.send([
                ARTIFACT_URL:     steps.jfrog('IL').targetToURL(target),
                ARTIFACT_VERSION: v
            ])
        }
    }

    def sast(config) {
        steps.sh 'make sast'
    }

    def secrets(config) {
        steps.sh 'make secrets'
    }    
}
