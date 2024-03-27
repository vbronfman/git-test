class Scheduler implements Serializable {
    private def jobName

    Scheduler(jobName) {
        this.jobName = jobName
    }

    static def maybeBuild()
    {
        def gitName = "GilatDevOps/SE4/ipm"
        def buildResult = getLastBuild()
        if (!buildResult) {
            jobBuild(jobName)
        }
    }

    def getLastBuild()
    {
        echo "Getting details on job $jobName"
        def jenkins = Jenkins.getInstance()
        def job = jenkins.getItem(jobName)
        def buildResult = job.getLastSuccessfulBuild()
        def jobUrl = results.getAbsoluteUrl()
        def buildVar = results.getBuildVariables()
        echo "${buildResult} ${jobUrl} ${buildVar}"
        return buildResult
    }

    def jobBuild()
    {
        echo "Building job $jobName"
        def jenkins = Jenkins.getInstance()
        def job = jenkins.getItem(jobName)
        def results = build propagate: false, job: $jobName
        def buildResult = results.getResult()
        def jobUrl = results.getAbsoluteUrl()
        def buildVar = results.getBuildVariables()
        echo "${buildResult} ${jobUrl} ${buildVar}"
        return buildResult
    }
}