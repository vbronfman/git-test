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
            // Get multibranch jobs in comply with array 'components' and custom pattern 'Developers/*-build to minify output

                Jenkins.instance.getAllItems(org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject).each { multibrjob ->
                    if (multibrjob.fullName.contains('Developers') && components.any { multibrjob.fullName.contains(it+'-build') } )  {  // NOTE this -build!!!
                    steps.echo multibrjob.fullName;

                    def item = Jenkins.instance.getItemByFullName(multibrjob.fullName) //hudson.model.Hudson.instance
                    steps.println "DEBUG getItemByFullName : " + item
                    def repositoryUrl = multibrjob.SCMSources[0].remote
                    def refSpecs = multibrjob.SCMSources[0].refSpecs
                    steps.println "DEBUG  repositoryUrl:" + repositoryUrl
                    steps.println "DEBUG  refSpecs: " + refSpecs

                    def jobs = item.getAllJobs();
    
                    steps.println "DEBUG jobs: " + jobs
    
                    for(job in jobs){
                        println "INFO job: " + job
                        last_success = job.getLastSuccessfulBuild()
                        if ( ! last_success  || ! (last_success.environment['BRANCH_NAME'] =~ /releases\// ) ) //    /releases\/\d+\./ ) //contains has to be refined!
                            continue
            
                    
                    // environment, id , parent=org.jenkinsci.plugins.workflow.job.WorkflowJob@773bf9a3[Developers/ctr-build/releases%2Fr200.0] , previousSuccessfulBuild , previousBuiltBuild , 
                    steps.println "INFO environment BRANCH_NAME " + last_success.environment['BRANCH_NAME']
                    steps.println "INFO environment BUILD_TAG " + last_success.environment['BUILD_TAG']

                    }


                  }
                }
        } catch(Exception err){
    steps.echo "error caught"
    steps.echo err.getMessage()
}
    }

    def isLastBuild(){
        steps.echo "DEBUG isLastBuild  : "

    }

}



