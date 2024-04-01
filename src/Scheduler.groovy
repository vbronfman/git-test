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

    def checkSuccessfulJob()
    {
        def jenkins = Jenkins.getInstance()
        def job = jenkins.getItemByFullName(jobName)
        def successJob = job.getLastSuccessfulBuild()
        if (!successJob) {
            steps.echo "No successful jobs found"
            exit 1
        }
        def buildResult = successJob.properties.result
    }

    def getLastBuild()
    {
        def fullJobName = jobName + java.net.URLEncoder.encode(branchName, "UTF-8")
        def lastCommit = (new Utilities(steps)).gitGetCommit()
        steps.echo "Getting details on job ${fullJobName} ${lastCommit}"
        def jobVars = steps.queryJobRuntime(name: fullJobName)
        steps.echo "Job Vars ${jobVars}"
        if (!jobVars) {
            steps.echo "Cannot get current job commit"
            exit 1
        }
        def jobCommit = jobVars.GIT_COMMIT_HASH
        steps.echo "${fullJobName} {jobCommit}"
        return lastCommit == jobCommit
    }

    def jobBuild()
    {
        steps.echo "Building job ${jobName}"
        def jenkins = Jenkins.getInstance()
        def job = jenkins.getItem(jobName)
        def results = build propagate: false, job: "${jobName}"
        def buildResult = results.getResult()
        def jobUrl = results.getAbsoluteUrl()
        def buildVar = results.getBuildVariables()
        steps.echo "${buildResult} ${jobUrl} ${buildVar}"
        return buildResult
    }
}