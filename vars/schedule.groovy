def call(Map config) {
    (new Schedule(config.jobName)).maybeBuild()
}