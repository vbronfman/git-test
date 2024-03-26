def call(jobName) {
    return (new Schedule(this)).maybeBuild(jobName)
}