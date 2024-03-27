def call(Map config) {
    (new Scheduler(config.jobName)).maybeBuild()
}
