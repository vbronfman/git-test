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
