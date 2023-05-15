import groovy.json.JsonSlurperClassic

class Argo
{
    static def connections = [:]
    static def connect(ctx, opt)
    {
        def url = opt.url ?: 'https://argocd-rnd.gilat.com/'
        if (opt.debug)
            ctx.println("DEBUG: url = $url")
        else
            ctx.println("Not a debug mode. DEBUG: url = $url")
        def creds = opt.creds ?: '8357b9f8-f851-415f-85c5-e77332d0848a'
        if (this.connections[url])
            return this.connections[url]
        ctx.withCredentials([ctx.usernamePassword(
            credentialsId: '8357b9f8-f851-415f-85c5-e77332d0848a',
            passwordVariable: 'password',
            usernameVariable: 'username',
        )]) {
            def rb = """{"username":"${ctx.username}", "password":"${ctx.password}\"}"""
            def response = ctx.httpRequest requestBody: rb,
                consoleLogResponseBody: true, httpMode: 'POST', ignoreSslErrors: true, 
                responseHandle: 'NONE', wrapAsMultipart: false
                url: "$url/api/v1/session"
            if (opt.debug)
            {
                ctx.println("DEBUG: Status: "+response.status)
                ctx.println("DEBUG: Content: "+response.content)
            }
            def res = (new JsonSlurperClassic()).parseText(response.content)
            if (opt.debug)
                ctx.println("DEBUG: token: "+res.token)
            return this.connections[url] = res.token
        }
    }

}
