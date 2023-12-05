def call(k) {
    // _queryJob_ gets the runtime variables from a job
    return (new RuntimeVars(this)).queryJob(k)
}
