def call(Map config) {
    def ut = new Utilities(this)

    ut.postBuildEmail(config)
}
