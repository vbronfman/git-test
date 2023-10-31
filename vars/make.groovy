def call(Map config) {
    def mk = new Makefile(this)

    if (config.cmd) {
        mk."${config.cmd}"(config)
    } else {
        mk."${config.type}"(config)
    }
}
