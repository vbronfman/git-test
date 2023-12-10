def call(Map config) {
    // _queryJob_ gets the runtime variables from a job
    return (new RuntimeVars(this)).queryJob(config)
}
