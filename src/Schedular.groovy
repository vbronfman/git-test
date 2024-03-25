class Schedular implements Serializable {
    private def jobName

    Schedular(jobName) {
        this.jobName = jobName
        this.jobName = "Developers/ipm-build/releases/r300.0"
    }

    static def getBuildResults(name)
    {
        echo "Getting details on job ${jobName} releases/r300.0"
        def gitName = "GilatDevOps/SE4/ipm"
        def jenkins = Jenkins.getInstance()
        def job = jenkins.getItem(jobName)
        def results = build propagate: false, job: jobName
        def buildResult = results.getResult()
        def jobUrl = results.getAbsoluteUrl()
        def buildVar = results.getBuildVariables()
        echo "${buildResult} ${jobUrl} ${buildVar}"
        return buildResult
    }
}