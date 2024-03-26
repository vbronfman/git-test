def call(Map config) {
    return (new Schedule(this)).maybeBuild(config.jobName)
}