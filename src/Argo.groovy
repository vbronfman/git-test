import groovy.json.JsonSlurperClassic

class Argo
{
    private String token
    private def ctx
    private HashMap opt
    private def debug(message){ if (this.opt.debug) this.ctx.println("DEBUG: ${message}") }
    Argo(ctx, opt){
        this.ctx = ctx
        this.opt = opt ?: [:]
    }
    def connect(ctx, opt)
    {
        def url = opt.url ?: 'https://argocd-rnd.gilat.com/'
        def creds = opt.creds ?: '8357b9f8-f851-415f-85c5-e77332d0848a'
        this.debug("url = $url")
        this.debug("creds id = $creds")
        if (this.token)
        {
            this.debug("connect: using cached token")
            return this.token
        }
        ctx.withCredentials([ctx.usernamePassword(
            credentialsId: '8357b9f8-f851-415f-85c5-e77332d0848a',
            passwordVariable: 'password',
            usernameVariable: 'username',
        )]) {
            def body = """{"username":"${ctx.username}", "password":"${ctx.password}"}"""
            def response = ctx.httpRequest requestBody: body,
                consoleLogResponseBody: true, httpMode: 'POST', ignoreSslErrors: true, 
                responseHandle: 'NONE', wrapAsMultipart: false,
                url: "$url/api/v1/session"
            this.debug("Status: "+response.status)
            this.debug("Content: "+response.content)
            def res = (new JsonSlurperClassic()).parseText(response.content)
            this.debug("DEBUG: token: "+res.token)
            return this.token = res.token
        }
    }
}
