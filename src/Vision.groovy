import java.text.SimpleDateFormat

class Vision
{
    private def ctx
    private def server_url
    private def project

    Vision(ctx, server_url)
    {
        this.ctx = ctx
        this.server_url = server_url
        if (!this.server_url)
            this.server_url = 'http://vision.gilat.com'
    }

    def setProject(project)
    {
        this.project = project
        return this
    }

    def getComponent(component)
    {
        def path = "api/project/${this.project}/component/$component"
        def url = "${this.server_url}/${path}"
        Utilities.request(this.ctx, "GET", url)
    }

    def publish(component, build_name, artifacts, opt=[:])
    {
        def path = "api/project/${this.project}/component/$component/version"
        if (opt.parent_name && opt.parent_version)
            path = "api/project/${this.project}/component/${opt.parent_name}/version/${opt.parent_version}"
        def url = "${this.server_url}/${path}"
        def date = new Date()
        def sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss")
        def body = [
            ci_url: this.ctx.env.BUILD_URL,
            artifacts: artifacts,
            version: this.ctx.BUILD_NUMBER,
            comment: sdf.format(date),
            ci_build_name: build_name,
            spec: component
        ]
        if (opt.children)
            body << [children: opt.children]
        Utilities.request(this.ctx, "POST", url, body)
    }

    def publishArtifacts(component, files, opt=[:])
    {
        def (c, err) = this.getComponent(component)
        if (err)
            return [ [message: "Cannot fetch component $component", ret: c], err ]
        // We publish only to AWS instance yet. If this changes,
        // have to get attr from component
        def a = new Jfrog(this.ctx, 'AWS') 
        def maturity = opt.maturity ?: "dev"
        def target_repo = "${c.artifactory_repo}-${maturity}"
        files = files.collect{[
            pattern: it.pattern,
            target: "$target_repo/${it.path?:''}"
        ]}

        def build = a.publishArtifacts(files, opt)

        this.publish(
            component,
            build.getName(),
            build.getArtifacts().collect{[path: it.remotePath]},
            opt
        )
    }

}
