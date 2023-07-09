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

    def publish(component, artifact)
    {
        def path = "api/project/${this.project}/component/$component/version"
        def url = "${this.server_url}/${path}"
        def date = new Date()
        def sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss")
        def body = [
            ci_url: this.ctx.env.BUILD_URL,
            artifact_url: artifact,
            version: this.ctx.BUILD_NUMBER,
            comment: sdf.format(date)
        ]
        Utilities.request(this.ctx, "POST", url, body)
    }

}
