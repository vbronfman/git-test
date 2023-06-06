class Makefile
{
    private def ctx

    Makefile(ctx){
        this.ctx = ctx
    }

    def buildCmakeDebug() {
        ctx.sh '''make build BUILD_TYPE=Debug BUILD_FLAGS='--parallel 12' '''
    }

    def buildCmakeRelease() {
        ctx.sh '''make build BUILD_TYPE=Release BUILD_FLAGS='--parallel 12' '''
    }

    def formatClang() {
        ctx.sh '''make format FORMAT_FLAGS='--Werror --dry-run' '''
    }

    def packit(branch, sharepoint) {
        def dbAddr = postgresql://g-versions-db.gilat.com:5432/se.4-versions-production

        ctx.withCredentials([usernamePassword(credentialsId: 'postgres-user-for-production', usernameVariable: 'dbUser', passwordVariable: 'dbPass')]) {
            ctx.sh """make packit VERSION_DB='${dbAddr}?user=$dbUser&password=$dbPass' BRANCH='${branch}' SHAREPOINT=${sharepoint ? 'sharepoint' : 'nosharepoint'}sharepoint"""
        }
    }

    def sast() {
        ctx.sh 'make sast'
    }
}
