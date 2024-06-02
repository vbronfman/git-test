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
                   // steps.println "INFO environment BUILD_TAG " + last_success.environment['BUILD_TAG']
                    // !!!
                   // steps.println "DEBUG getActions methods " + last_success.getActions(hudson.plugins.git.util.BuildData.class)[1].class?.methods?.collect { it.name }
                    steps.println "DEBUG getActions: " + last_success.getActions(hudson.plugins.git.util.BuildData.class)[1].getLastBuiltRevision() //.getUserRemoteConfigs() //getBuildData(last_success)
         last_success.getActions(hudson.plugins.git.util.BuildData.class).each { it -> 
         steps.println "DEBUG getLastBuiltRevision.getBranch for each : " + it.getLastBuiltRevision().getBranches()
           if( it.getLastBuiltRevision().containsBranchName('refs/remotes/origin/' + last_success.environment['BRANCH_NAME'])) {  //oh, for g-d sake...
             def sha = it.getLastBuiltRevision().getSha1String()
             def branch = last_success.environment['BRANCH_NAME']
             def remote_url = last_success.getActions(hudson.plugins.git.util.BuildData.class)[1].getRemoteUrls()[0]
             steps.println "DEBUG remote_url class: " + remote_url.getClass()
                steps.println "DEBUG getLastBuiltRevision..getSha1String() " + sha  +  " URL : " + remote_url + " barch: " + branch


           //  if (isLastCommit(sha, remote_url , branch ))

           steps.println "DEBUG isLastBuild  : " + remote_url + " branch: " +  branch
        // git ls-remote --heads ${remote_url} ${branch}
            sshagent(["azure-worker-ssh-msharay"]) 
            {
              def  git_commit = ["git", "ls-remote", "git@ssh.dev.azure.com:v3/GilatDevOps/SE4/mcr"].execute().with{
    def output = new StringWriter()
    def error = new StringWriter()
    //wait for process ended and catch stderr and stdout.
    it.waitForProcessOutput(output, error)
    //check there is no error
    steps.println "error=$error"
    steps.println "output=$output"
    steps.println "code=${it.exitValue()}"
}
            steps.println "INFO git_commit : "+ git_commit
            } 
            //return sha == git_commit? true : false
            // adds to map 'Developers/ipm-build: branch' entries of last succesfull jobs  if any    
            branches[multibrjob.fullName]?.add (last_success.environment['BRANCH_NAME']) // REVIEW!!! is it 
           }
            
            }
// !!!
 

                    }

                  }
                }
                steps.println "DEBUG list of values to build " + branches
                return branches.findAll{ it.value!=null } // grabbed here https://stackoverflow.com/questions/55696504/groovy-remove-null-elements-from-a-map 
        } catch(Exception err){
                steps.println "ERROR error caught: " + err.getMessage()
                 steps.println(err.toString());
            steps.println(err.getStackTrace());
                return null
            }
    }

    def isLastCommit(String sha, String url, String branch){
        println "DEBUG isLastBuild  : " + url + " branch: " +  branch
        try {
            steps.sshagent(["azure-worker-ssh-msharay"]) {
             git_commit = steps.sh(
            returnStdout:  true,
            script: """
                git ls-remote --heads ${url} ${branch}
            """).strip()
            } 
            steps.println "INFO git_commit : "+ git_commit
            //return sha == git_commit? true : false
            return true
        } catch (Exception err){
            steps.println "ERROR isLastCommit error caught: " + err.getMessage()
            steps.println(err.toString());
            steps.println(err.getStackTrace());

            return null
        }
  
    }


}



