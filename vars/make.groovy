def call(Map config) {
    def mk = new Makefile(this)

    switch (config.type) {
        case 'buildCmake':
            mk.buildCmake(config)
            break
        case 'buildGeneric':
            mk.buildGeneric(config)
            break
        case 'clean':
            mk.clean(config.part)
            break
        case 'formatClang':
            mk.formatClang()
            break
        case 'packit':
            mk.packit(config.branch, config.sharepoint, config.v)
            break
        case 'publish':
            mk.publish(config.repo, config.gdf, config.component, config.project)
            break
        case 'sast':
            mk.sast()
            break
        default:
            throw new Exception("Unrecognized type <${config.type}>.")
    }
}
