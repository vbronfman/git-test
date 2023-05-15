import groovy.json.JsonBuilder
import groovy.json.JsonSlurperClassic

class Argo
{
    private def ctx
    private HashMap opt
    private def debug(message){ if (this.opt.debug) this.ctx.println("DEBUG: ${message}") }
    private String url
    private String token
    Argo(ctx, opt){
        this.ctx = ctx
        this.opt = opt ?: [:]
        this.opt.debug = !!this.opt.debug
        this.url = this.opt.url ?: 'https://argocd-rnd.gilat.com/'
    }
    def connect()
    {
        def creds = this.opt.creds ?: '8357b9f8-f851-415f-85c5-e77332d0848a'
        this.debug("url = ${this.url}")
        this.debug("creds id = $creds")
        if (this.token)
        {
            this.debug("connect: using cached token")
            return this.token
        }
        this.ctx.withCredentials([this.ctx.usernamePassword(
            credentialsId: '8357b9f8-f851-415f-85c5-e77332d0848a',
            passwordVariable: 'password',
            usernameVariable: 'username',
        )]) {
            def body = """
                {"username":"${this.ctx.username}", "password":"${this.ctx.password}"}
            """
            def verbose = this.opt.debug
            def response = this.ctx.httpRequest requestBody: body,
                consoleLogResponseBody: verbose, httpMode: 'POST',
                ignoreSslErrors: true, responseHandle: 'NONE', wrapAsMultipart: false,
                url: "${this.url}/api/v1/session"
            this.debug("Status: "+response.status)
            this.debug("Content: "+response.content)
            def res = (new JsonSlurperClassic()).parseText(response.content)
            this.debug("token: "+res.token)
            return this.token = res.token
        }
    }
    private def req(path, mode, body)
    {
        def t = this.connect()
        def verbose = this.opt.debug
        def response = this.ctx.httpRequest consoleLogResponseBody: verbose,
            httpMode: mode, ignoreSslErrors: true, responseHandle: 'NONE',
            wrapAsMultipart: false, url: "${this.url}/${path}", requestBody: body,
            customHeaders: [[maskValue: false, name: 'Authorization', value: "Bearer $t"]]
        return response.content
    }
    private def get(path)
    {
        return this.req(path, 'GET', '')
    }
    private def post(path, body)
    {
        return this.req(path, 'POST', body)
    }
    private def put(path, body)
    {
        return this.req(path, 'PUT', body)
    }
    def getApplication(name)
    {
        def content = this.get("api/v1/applications/${name}")
        return (new JsonSlurperClassic()).parseText(content)
    }
    def syncApplication(name, obj)
    {
        def res = this.post("api/v1/applications/${name}/sync", (new JsonBuilder(obj).toString()))
    }
    def updateApplication(name, obj)
    {
        def res = this.put("api/v1/applications/${name}/spec", (new JsonBuilder(obj).toString()))
    }
}
