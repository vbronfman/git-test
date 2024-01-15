def call(Map config) {
    (new Email(this)).postBuildEmail(config)
}
