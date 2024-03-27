class Schedule implements Serializable {
    // private def jobName

    Schedule() {}

    static def maybeBuild(jobName)
    {
        def gitName = "GilatDevOps/SE4/ipm"
        def buildResult = getLastBuild(jobName)
        if (!buildResult) {
            jobBuild(jobName)
        }
    }

    def getLastBuild(jobName)
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

    def jobBuild(jobName)
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