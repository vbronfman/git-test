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
        def buildResult = getLastBuild()
        if (!buildResult) {
            jobBuild()
        }
    }

    def getLastBuild()
    {
        steps.echo "Getting details on job ${jobName}"
        def jenkins = Jenkins.getInstance()
        def job = jenkins.getItem(jobName)
        def buildResult = job.getLastSuccessfulBuild()
        def jobUrl = results.getAbsoluteUrl()
        def buildVar = results.getBuildVariables()
        steps.echo "${buildResult} ${jobUrl} ${buildVar}"
        return buildResult
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