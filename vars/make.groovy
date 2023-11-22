def call(Map config) {
    (new Makefile(this))."${config.cmd}"(config)
}
