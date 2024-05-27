def call(Map config) {
    (new SchedulerWrapper(this, config)).getJobNames()
}
