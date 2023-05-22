import groovy.json.JsonBuilder
import groovy.json.JsonSlurperClassic

class Argo
{
    private def ctx
    private HashMap opt
    private def debug(message){ if (this.opt.debug) this.ctx.println("DEBUG: ${message}") }
    private String url
    Argo(ctx, opt){
        this.ctx = ctx
        this.opt = opt ?: [:]
        this.opt.debug = !!this.opt.debug
        this.url = this.opt.url ?: 'https://argocd-rnd.gilat.com/'
    }
    private def req(path, mode, body)
    {
        def quiet = !this.opt.debug
        this.ctx.withCredentials([this.ctx.string(
            credentialsId: 'e5c5b02b-13e2-4fe3-85c3-09d780b9e3cd',
            variable: 'token'
        )]) {
            def bearer = "Bearer " + this.ctx.token
            def i = 0
            this.ctx.retry(10){
                this.debug("Fetching $path, try #${i++}")
                def response = this.ctx.httpRequest(consoleLogResponseBody: true,
                    httpMode: mode, ignoreSslErrors: true, responseHandle: 'NONE', quiet: quiet,
                    wrapAsMultipart: false, url: "${this.url}/${path}", requestBody: body,
                    customHeaders: [[maskValue: false, name: 'Authorization', value: bearer]])
                return (new JsonSlurperClassic()).parseText(response.content)
            }
        }
    }
    private def get(path) { return this.req(path, 'GET', '') }
    private def post(path, body) { return this.req(path, 'POST', body) }
    private def put(path, body) { return this.req(path, 'PUT', body) }
    def getApplication(name)
    {
        return this.get("api/v1/applications/${name}")
    }
    def syncApplication(name, obj)
    {
        return this.post("api/v1/applications/${name}/sync", (new JsonBuilder(obj).toString()))
    }
    def updateApplication(name, obj)
    {
        return this.put("api/v1/applications/${name}/spec", (new JsonBuilder(obj).toString()))
    }
}
