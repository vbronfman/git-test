def call(Map config) {
    def mk = new Makefile(this)

    mk."${config.cmd}"(config)
}
