import hudson.model.*
import jenkins.model.*
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition
import org.jenkinsci.plugins.workflow.job.WorkflowJob
import org.jenkinsci.plugins.workflow.job.WorkflowRun
import org.codehaus.groovy.control.CompilerConfiguration
import groovy.util.GroovyScriptEngine
import com.cloudbees.hudson.plugins.folder.*

class LibTest
{

    static def run(ctx, branch, clean)
    {
        def tests = loadTests(ctx)
        def jobs = tests.collect{ createJob(
                "${Utilities.normalizeBranchName(branch)}-${it.name}",
                """
                    //-- start of auto generated header
                    // Load library from the tested branch
                    @Library("devops@${branch}") _
                    //-- end of auto generated header
                """.stripIndent() + it.text
            )
        }
        def results = jobs.collect{ job ->
            def result = [name: job.name]
            try{
                def parameters = [
                    new StringParameterValue('tested_branch', branch),
                    new BooleanParameterValue('run_in_lib_test', true),
                ]
                def build = job.scheduleBuild2(0, new ParametersAction(parameters))
                while (!build.isDone())
                    sleep(1000)
                result << [result: build.getStartCondition().get().result ?: "RUNNING"]
                result << [logs: build.getStartCondition().get().getLog()]
                return result
            } catch(Exception e){
                return [name: job.name, result: "FAILURE", reason: "${e}"]
            }
        }
        if (clean)
            jobs.each{ job -> job.delete() }
        return results
    }

    static def loadTests(ctx)
    {
        def scriptPath = ctx.class.protectionDomain.codeSource.location.path
        def libraryRootPath = scriptPath.replaceAll("vars.*", "")
        def libraryRoot = "${libraryRootPath}resources"
        def testsFolder = "${libraryRoot}/tests"
        return new File(testsFolder).listFiles().findAll { file ->
            file.isFile() && file.name.endsWith('.groovy')
        }.collect { file ->
            [
                name: file.name.minus('.groovy'),
                text: new File(file.path).text
            ]
        }
    }

    static def createJob(jobName, text, folderPath='lib-tests' )
    {
        def folder = Jenkins.instance.getItemByFullName(folderPath, Folder.class)
        if (folder == null)
            folder = Jenkins.instance.createProject(Folder, folderPath)
        def childJob = folder.getItem(jobName)
        if (childJob != null)
            childJob.delete()
        childJob = folder.createProject(WorkflowJob, jobName)
        childJob.definition = new CpsFlowDefinition(text, true)
        childJob.save()
        return childJob
    }

}
