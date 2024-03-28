class Scheduler implements Serializable {
    private def steps
    private def jobName
    private def runtimeVars

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
        runtimeVars = new RuntimeVars(steps)
        def jenkins = Jenkins.getInstance()
        def job = jenkins.getItemByFullName(jobName)
        def buildResult = job.getLastSuccessfulBuild().result
        def currCommit = (new Utilities(steps)).gitGetCommit()
        steps.echo "Getting details on job ${jobName} ${currCommit}"
        def jobCommit = runtimeVars.recv(['GIT_COMMIT_HASH']).values().first()
        steps.echo "${buildResult} ${jobName} {jobCommit}"
        return currCommit == jobCommit
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