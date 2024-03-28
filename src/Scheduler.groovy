class Scheduler implements Serializable {
    private def steps
    private def jobName

    Scheduler(steps, jobName) {
        this.steps = steps
        this.jobName = jobName
    }

    def maybeBuild()
    {
        def gitName = "GilatDevOps/SE4/ipm"
        def commitCheck = getLastBuild()
        // if (!commitCheck) {
        //     jobBuild()
        // }
    }

    def getLastBuild()
    {
        def jenkins = Jenkins.getInstance()
        def job = jenkins.getItemByFullName(jobName)
        def buildResult = job.getLastSuccessfulBuild().result
        def currCommit = Utilities.gitGetCommit(steps)
        // steps.echo "Getting details on job ${jobName} ${currCommit}"
        // def jobCommit = getRuntimeVar('GIT_COMMIT_HASH')
        // steps.echo "${buildResult} ${jobName} {jobCommit}"
        // return currCommit == jobCommit
        return false
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