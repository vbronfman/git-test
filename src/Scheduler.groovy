class Scheduler implements Serializable {
    private def steps
    private def jobName
    private def branchName

    Scheduler(steps, config) {
        this.steps = steps
        this.jobName = config.jobName
        this.branchName = config.branchName
    }

    def maybeBuild()
    {
        def gitName = "GilatDevOps/SE4/ipm"
        def commitCheck = getLastBuild()
        if (!commitCheck) {
            jobBuild()
        }
    }


    def getLastBuild()
    {
        // def jenkins = Jenkins.getInstance()
        // def job = jenkins.getItemByFullName(jobName)
        // def buildResult = job.getLastSuccessfulBuild().properties.result
        def lastCommit = (new Utilities(steps)).gitGetCommit()
        def fullJobName = jobName + java.net.URLEncoder.encode(branchName, "UTF-8")
        steps.echo "Getting details on job ${fullJobName} ${lastCommit}"
        // def jobCommit = (new RuntimeVars(this)).queryJob(name: fullJobName).GIT_COMMIT_HASH
        def jobCommit = steps.queryJobRuntime(name: fullJobName)
        steps.echo "${fullJobName} {jobCommit}"
        return lastCommit == jobCommit
    }

    def jobBuild()
    {
        steps.echo "Building job ${jobName}"
        // def jenkins = Jenkins.getInstance()
        // def job = jenkins.getItem(jobName)
        // def results = build propagate: false, job: "${jobName}"
        // def buildResult = results.getResult()
        // def jobUrl = results.getAbsoluteUrl()
        // def buildVar = results.getBuildVariables()
        // steps.echo "${buildResult} ${jobUrl} ${buildVar}"
        // return buildResult
    }
}