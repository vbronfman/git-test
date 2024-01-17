class Email implements Serializable
{
    enum PipelineType {
        BuildPipeline,
        DockerPipeline,
        SyncCachePipeline
    }

    enum TriggerType {
        ScriptTrigger,
        UserTrigger
    }

    enum ResultType {
        AcceptableResult,
        UnacceptableResult
    }

    enum EmailTo {
        CommitOwner,
        Listeners,
        NoOne,
        TriggerOwner
    }

    private def steps
    private def runtimeVars

    Email(steps) {
        this.steps = steps
        this.runtimeVars = new RuntimeVars(steps)
    }

    def getTriggerType() {
        // NOTE: Will return _UserTrigger_ for jenkins-cli hooks that impersonate users.
        //   (e.g., how jobs are triggered from Azure at the time of writing).
        if (steps.currentBuild.getBuildCauses('hudson.model.Cause$UserIdCause').size() != 0) {
            return TriggerType.UserTrigger
        }

        return TriggerType.ScriptTrigger
    }

    def getResultType() {
        if (steps.currentBuild.resultIsBetterOrEqualTo('UNSTABLE'))
            return ResultType.AcceptableResult

        return ResultType.UnacceptableResult
    }

    def emailRule(pipelineType) {
        def rules = [
            (PipelineType.BuildPipeline): [
                (TriggerType.UserTrigger): [
                    (ResultType.AcceptableResult): (EmailTo.Listeners), (ResultType.UnacceptableResult): (EmailTo.TriggerOwner)
                ],
                (TriggerType.ScriptTrigger): [
                    (ResultType.AcceptableResult): (EmailTo.Listeners), (ResultType.UnacceptableResult): (EmailTo.Listeners)
                ]
            ],
            (PipelineType.DockerPipeline): [
                (TriggerType.UserTrigger): [
                    (ResultType.AcceptableResult): (EmailTo.Listeners), (ResultType.UnacceptableResult): (EmailTo.TriggerOwner)
                ],
                (TriggerType.ScriptTrigger): [
                    (ResultType.AcceptableResult): (EmailTo.Listeners), (ResultType.UnacceptableResult): (EmailTo.CommitOwner)
                ]
            ],
            (PipelineType.SyncCachePipeline): [
                (TriggerType.UserTrigger): [
                    (ResultType.AcceptableResult): (EmailTo.TriggerOwner), (ResultType.UnacceptableResult): (EmailTo.TriggerOwner)
                ],
                (TriggerType.ScriptTrigger): [
                    (ResultType.AcceptableResult): (EmailTo.NoOne), (ResultType.UnacceptableResult): (EmailTo.Listeners)
                ]
            ]
        ]
        def trigger = getTriggerType()
        def result  = getResultType()
        def o = rules[pipelineType][trigger][result]
        steps.echo("RULE <${pipelineType}:${trigger}:${result}> -> ${o}")

        //WARN: Workaround as we do not have email groups yet
        if (o == EmailTo.Listeners && trigger == TriggerType.UserTrigger) {
            o = EmailTo.TriggerOwner
        }

        return o
    }

    def getRecipients(pipelineType) {
        def recipients

        switch (emailRule(pipelineType)) {
            case EmailTo.CommitOwner:
                //TODO
                break
            case EmailTo.Listeners:
                //TODO
                break
            case EmailTo.NoOne:
                break
            case EmailTo.TriggerOwner:
                recipients = steps.env.BUILD_USER_EMAIL
                break
        }

        if (!recipients) {
            throw new Exception('Run has no recipients')
        }

        return recipients
    }

    def postBuildEmail(config) {
        try {
            def recipients = config?.recipients

            if (!recipients) {
                recipients = getRecipients(PipelineType.BuildPipeline)
            }

            // _recv_ returns a map, only get _first_ (and only) of the
            // _values_ set.
            def artifactUrl = runtimeVars.recv(['ARTIFACT_URL']).values().first()

            switch (getResultType()) {
                case ResultType.AcceptableResult:
                    steps.emailext(
                        subject: "BUILD SUCCESSFUL: Job '${steps.env.JOB_NAME} [${steps.env.BUILD_NUMBER}]'",
                        body: """<p>BUILD SUCCESSFUL: Job '${steps.env.JOB_NAME} [${steps.env.BUILD_NUMBER}]':</p>
                            <p>Finished with result &QUOT;${steps.currentBuild.getResult()}&QUOT;</p>
                            <p>Package is available at &QUOT;<a href='${artifactUrl}'>${artifactUrl}</a>&QUOT;</p>
                            <p>Check console output at &QUOT;<a href='${steps.env.BUILD_URL}'>${steps.env.JOB_NAME} [${steps.env.BUILD_NUMBER}]</a>&QUOT;</p>""",
                        to: recipients,
                        mimeType: 'text/html')
                    break
                case ResultType.UnacceptableResult:
                    steps.emailext(
                        subject: "BUILD FAILED: Job '${steps.env.JOB_NAME} [${steps.env.BUILD_NUMBER}]'",
                        body: """<p>BUILD FAILED: Job '${steps.env.JOB_NAME} [${steps.env.BUILD_NUMBER}]':</p>
                            <p>Finished with result &QUOT;${steps.currentBuild.getResult()}&QUOT;</p>
                            <p>Check console output at &QUOT;<a href='${steps.env.BUILD_URL}'>${steps.env.JOB_NAME} [${steps.env.BUILD_NUMBER}]</a>&QUOT;</p>""",
                        to: recipients,
                        mimeType: 'text/html')
                    break
             }
        }
        catch(e) {
             steps.echo("${e}")
        }
    }

    def postSyncCacheEmail(config) {
        try {
            def recipients = config?.recipients

            if (!recipients) {
                recipients = getRecipients(PipelineType.SyncCachePipeline)
            }

            switch (getResultType()) {
                case ResultType.AcceptableResult:
                    steps.emailext(
                        subject: "SYNC SUCCESSFUL: Job '${steps.env.JOB_NAME} [${steps.env.BUILD_NUMBER}]'",
                        body: """<p>SYNC SUCCESSFUL: Job '${steps.env.JOB_NAME} [${steps.env.BUILD_NUMBER}]':</p>
                            <p>Finished with result &QUOT;${steps.currentBuild.getResult()}&QUOT;</p>
                            <p>Check console output at &QUOT;<a href='${steps.env.BUILD_URL}'>${steps.env.JOB_NAME} [${steps.env.BUILD_NUMBER}]</a>&QUOT;</p>""",
                        to: recipients,
                        mimeType: 'text/html')
                    break
                case ResultType.UnacceptableResult:
                    steps.emailext(
                        subject: "SYNC FAILED: Job '${steps.env.JOB_NAME} [${steps.env.BUILD_NUMBER}]'",
                        body: """<p>SYNC FAILED: Job '${steps.env.JOB_NAME} [${steps.env.BUILD_NUMBER}]':</p>
                            <p>Finished with result &QUOT;${steps.currentBuild.getResult()}&QUOT;</p>
                            <p>Check console output at &QUOT;<a href='${steps.env.BUILD_URL}'>${steps.env.JOB_NAME} [${steps.env.BUILD_NUMBER}]</a>&QUOT;</p>""",
                        to: recipients,
                        mimeType: 'text/html')
                    break
             }
        }
        catch(e) {
             steps.echo("${e}")
        }
    }

    def postDockerEmail(config) {
        try {
            def recipients = config?.recipients

            if (!recipients) {
                recipients = getRecipients(PipelineType.DockerPipeline)
            }

            def vars = runtimeVars.recv(['SHORT_NAME', 'FULL_NAME'])

            switch (getResultType()) {
                case ResultType.AcceptableResult:
                    steps.emailext(
                        subject: "DOCKER BUILD SUCCESSFUL: Job '${steps.env.JOB_NAME} [${steps.env.BUILD_NUMBER}]'",
                        body: """<p>DOCKER BUILD SUCCESSFUL: Job '${steps.env.JOB_NAME} [${steps.env.BUILD_NUMBER}]':</p>
                            <p>Finished with result &QUOT;${steps.currentBuild.getResult()}&QUOT;</p>
                            <p>Image ${vars['SHORT_NAME']} is available at &QUOT;<a href='${vars['LONG_NAME']}'>${vars['LONG_NAME']}</a>&QUOT;</p>
                            <p>Check console output at &QUOT;<a href='${steps.env.BUILD_URL}'>${steps.env.JOB_NAME} [${steps.env.BUILD_NUMBER}]</a>&QUOT;</p>""",
                        to: recipients,
                        mimeType: 'text/html')
                    break
                case ResultType.UnacceptableResult:
                    steps.emailext(
                        subject: "DOCKER BUILD FAILED: Job '${steps.env.JOB_NAME} [${steps.env.BUILD_NUMBER}]'",
                        body: """<p>DOCKER BUILD FAILED: Job '${steps.env.JOB_NAME} [${steps.env.BUILD_NUMBER}]':</p>
                            <p>Finished with result &QUOT;${steps.currentBuild.getResult()}&QUOT;</p>
                            <p>Check console output at &QUOT;<a href='${steps.env.BUILD_URL}'>${steps.env.JOB_NAME} [${steps.env.BUILD_NUMBER}]</a>&QUOT;</p>""",
                        to: recipients,
                        mimeType: 'text/html')
                    break
             }
        }
        catch(e) {
             steps.echo("${e}")
        }
    }
}
