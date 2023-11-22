def call(Map config) {
    (new DockerTools(this))."${config.cmd}"(config)
}
