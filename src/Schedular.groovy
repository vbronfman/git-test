class Schedular implements Serializable {
    private def jobName

    Schedular(jobName) {
        this.jobName = jobName
    }

    static def getBuildResults(name)
    {
        // this.jobName = "Developers/ipm-build/releases/r300.0"
        echo "Getting details on job $jobName"
        def gitName = "GilatDevOps/SE4/ipm"
        def jenkins = Jenkins.getInstance()
        def job = jenkins.getItem($jobName)
        def results = build propagate: false, job: $jobName
        def buildResult = results.getResult()
        def jobUrl = results.getAbsoluteUrl()
        def buildVar = results.getBuildVariables()
        echo "${buildResult} ${jobUrl} ${buildVar}"
        return buildResult
    }
}