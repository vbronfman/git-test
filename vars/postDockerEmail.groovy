def call(Map config) {
    (new Email(this)).postDockerBuildEmail(config)
}
