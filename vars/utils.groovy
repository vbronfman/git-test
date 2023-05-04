def test(){echo "library test"}
def gitClone(ctx, opt){ return Utilities.gitClone(this, opt) }
def gitClone(ctx, repo, branch, srcDirs, dstPath){
    return Utilities.gitClone(this, [
        repo: repo,
        branch: branch,
        srcDirs: srcDirs,
        dstPath: dstPath
    ])
}
