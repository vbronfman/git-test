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
        def branches = [:]
        try {
            // Get multibranch jobs in comply with array 'components' and custom pattern 'Developers/*-build to minify output;  as a matter of fact the getAllItems loop can be ommited in  
            // favor of loop over array of names are forged like 'Developers/<component>-build' .
            

                Jenkins.instance.getAllItems(org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject).each { multibrjob ->
                    if (multibrjob.fullName.contains('Developers') && components.any { multibrjob.fullName.contains(it+'-build') } )  {  // NOTE this -build!!!
                    steps.echo "INFO multibranch job: " + multibrjob.fullName; 

                    def item = Jenkins.instance.getItemByFullName(multibrjob.fullName) //hudson.model.Hudson.instance
                    steps.println "DEBUG getItemByFullName : " + item
                    def repositoryUrl = multibrjob.SCMSources[0].remote
                    def refSpecs = multibrjob.SCMSources[0].refSpecs
                    steps.println "DEBUG  repositoryUrl:" + repositoryUrl
                    steps.println "DEBUG  refSpecs: " + refSpecs

// !!!
                    steps.println "DEBUG getActions methods " + last_success.getActions(hudson.plugins.git.util.BuildData.class)[1].class?.methods?.collect { it.name }
                    steps.println "DEBUG getActions: " + last_success.getActions(hudson.plugins.git.util.BuildData.class)[1].getLastBuiltRevision() //.getUserRemoteConfigs() //getBuildData(last_success)
         last_success.getActions(hudson.plugins.git.util.BuildData.class).each { it ->
         steps.println "DEBUG getLastBuiltRevision.getBranch for each : " + it.getLastBuiltRevision().getBranches()
           steps.println it.getLastBuiltRevision().containsBranchName('refs/remotes/origin/' + last_success.environment['BRANCH_NAME'])   //oh, for g-d sake...
           	steps.println "DEBUG getLastBuiltRevision..getSha1String() " + it.getLastBuiltRevision().getSha1String()
            
      }
// !!!

                    def jobs = item.getAllJobs();
    
                    steps.println "DEBUG jobs: " + jobs
                    branches.put(multibrjob.fullName,[])
    
                    for(job in jobs){ // this step to collect release branch names if any
                        steps.println "INFO job: " + job
                        def last_success = job.getLastSuccessfulBuild()
                        if ( ! last_success  || ! (last_success.environment['BRANCH_NAME'] =~ /releases\// ) ) //    /releases\/\d+\./ ) //contains has to be refined!
                            continue
            
                    
                    // environment, id , parent=org.jenkinsci.plugins.workflow.job.WorkflowJob@773bf9a3[Developers/ctr-build/releases%2Fr200.0] , previousSuccessfulBuild , previousBuiltBuild , 
                    steps.println "INFO environment BRANCH_NAME " + last_success.environment['BRANCH_NAME']
                    steps.println "INFO environment JOB_NAME" + last_success.environment['JOB_NAME']
                    steps.println "INFO environment JOB_BASE_NAME" + last_success.environment['JOB_BASE_NAME']
                    steps.println "INFO environment BUILD_TAG " + last_success.environment['BUILD_TAG']

                // adds to map 'Developers/ipm-build: branch' entries of last succesfull jobs  if any    
                    branches[multibrjob.fullName]?.add (last_success.environment['BRANCH_NAME'])

                    }

                  }
                }
                steps.println "DEBUG list of values to build " + branches
                return branches.findAll{ it.value!=null } // grabbed here https://stackoverflow.com/questions/55696504/groovy-remove-null-elements-from-a-map 
        } catch(Exception err){
                steps.echo "error caught: " + err.getMessage()
                return null
            }
    }

    def isLastBuild(){
        steps.echo "DEBUG isLastBuild  : "
    }


}



