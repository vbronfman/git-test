def call(Map config) {
    (new Email(this)).postDockerEmail(config)
}
