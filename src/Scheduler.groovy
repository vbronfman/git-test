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
        def isCommitIdentical = getLastBuild()
        if (!isCommitIdentical) {
            jobBuild()
        }
    }

    def checkSuccessfulJob()
    {
        def fullJobName = jobName + java.net.URLEncoder.encode(branchName, "UTF-8")
        def jenkins = Jenkins.getInstance()
        def job = jenkins.getItemByFullName(fullJobName)
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


        
        def lastestCommit = steps.sh("git clone --bare --filter=blob:none") //? actually returns error


        steps.echo "Getting details on job ${fullJobName}"
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

    @NonCPS
    def jobBuild()
    {
        def fullJobName = jobName + java.net.URLEncoder.encode(branchName, "UTF-8")
        steps.echo "Building job ${fullJobName}"
        def jenkins = Jenkins.getInstance()
        def job = jenkins.getItemByFullName(fullJobName)
        def results = steps.build propagate: false, job: "${fullJobName}", wait: true
        def buildResult = results.getResult()
        def jobUrl = results.getAbsoluteUrl()
        def buildVar = results.getBuildVariables()
        steps.echo "${buildResult} ${jobUrl} ${buildVar}"
        return buildResult
    }
}