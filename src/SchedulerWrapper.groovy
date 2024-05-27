class SchedulerWrapper implements Serializable {
    private def steps
    // private def jobName
    // private def branchName
    private def components

    SchedulerWrapper(steps, config) {
        this.steps = steps
        //this.jobName = config.jobName
        //this.branchName = config.branchName
        this.components = config.components
    }

    def getJobNames() {
        steps.echo "DEBUG getJobNames components: " + components

        try {

                Jenkins.instance.getAllItems(org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject).each { multibrjob ->
                    if (multibrjob.fullName.contains('Developers') && arrJobs.any { multibrjob.fullName.contains(it+'-build') } )  {  // NOTE this -build!!!
                    steps.echo multibrjob.fullName;
                  }
                }
            return true
        } catch(Exception err){
    steps.echo "error caught"
    steps.echo err.getMessage()
}
    }

    def isLastBuild(){
        steps.echo "DEBUG isLastBuild  : "

    }

}



