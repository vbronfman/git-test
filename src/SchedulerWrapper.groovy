class SchedulerWrapper implements Serializable {
    private def steps
    // private def jobName
    // private def branchName
    private def components

    Scheduler(steps, config) {
        this.steps = steps
        //this.jobName = config.jobName
        //this.branchName = config.branchName
        this.components = config.components
    }

    def getJobNames() {
        println "DEBUG getJobNames arrJobs: " + arrJobs
        return true
    }

    def isLastBuild(){
        println "DEBUG isLastBuild arrJobs: "

    }



