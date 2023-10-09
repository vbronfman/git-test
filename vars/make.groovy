def call(Map config) {
    def mk = new Makefile(this)

    mk."${config.type}"(config)
}
