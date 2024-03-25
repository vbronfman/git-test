def call(Map config) {
    // _getBuildResults_ gets the build results from a job
    return (new Schedular(this)).getBuildResults(config)
}