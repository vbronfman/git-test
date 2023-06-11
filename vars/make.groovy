def call(Map config) {
    def mk = new Makefile(this)

    switch (config.type) {
        case 'buildCmakeDebug':
            mk.buildCmakeDebug()
            break
        case 'buildCmakeRelease':
            mk.buildCmakeRelease()
            break
        case 'formatClang':
            mk.formatClang()
            break
        case 'packit':
            mk.packit(config.branch, config.sharepoint)
            break
        case 'publish':
            mk.publish(config.repo, config.branch)
            break
        case 'sast':
            mk.sast()
            break
        default:
            throw new Exception("Unrecognized type <${config.type}>.")
    }
}
