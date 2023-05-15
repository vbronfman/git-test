import groovy.json.JsonSlurperClassic

def connections = [:]

class ArgoCD
{
    static def connect(ctx, opt)
    {
        def url = opt.url ?: 'https://argocd-rnd.gilat.com/'
        def creds = opt.creds ?: '8357b9f8-f851-415f-85c5-e77332d0848a'
        if (connections[url])
            return connections[url]
        withCredentials([usernamePassword(
            credentialsId: '8357b9f8-f851-415f-85c5-e77332d0848a',
            passwordVariable: 'password',
            usernameVariable: 'username',
        )]) {
            def rb = """{"username":"$username", "password":"$password\"}"""
            def response = httpRequest requestBody: rb,
                consoleLogResponseBody: true, httpMode: 'POST', ignoreSslErrors: true, 
                responseHandle: 'NONE', wrapAsMultipart: false
                url: "$url/api/v1/session"
            if (opt.debug)
            {
                println("DEBUG: Status: "+response.status)
                println("DEBUG: Content: "+response.content)
            }
            def res = (new JsonSlurperClassic()).parseText(response.content)
            if (opt.debug)
                println("DEBUG: token: "+res.token)
            return res.token
        }
    }

}
