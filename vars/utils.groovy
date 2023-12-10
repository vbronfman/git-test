def getConstant(name){ return Utilities.getConstant(name) }
def gitClone(opt){ return Utilities.gitClone(this, opt) }
def gitClone(repo, branch, srcDirs, dstPath){
    return Utilities.gitClone(this, [
        repo: repo,
        branch: branch,
        srcDirs: srcDirs,
        dstPath: dstPath
    ])
}
def request(url, mode, body='', creds=''){
    return Utilities.request(this, url, mode, body, creds)
}
def normalizeBranchName(name){
    return Utilities.normalizeBranchName(name)
}
