def call(Map config) {
    def dt = new DockerTools(this)

    switch (config.type) {
        case 'build':
            dt.build(config.name)
            break
        case 'publish':
            dt.publish(config.name)
            break
        default:
            throw new Exception("Unrecognized type <${config.type}>.")
    }
}
