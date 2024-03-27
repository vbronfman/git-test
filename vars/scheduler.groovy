def call(Map config) {
    (new Scheduler(this, config.jobName)).maybeBuild()
}
