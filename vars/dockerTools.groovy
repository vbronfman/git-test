def call(Map config) {
    def dt = new DockerTools(this)

    dt."${config.cmd}"(config)
}
