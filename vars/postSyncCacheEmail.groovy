def call(Map config) {
    (new Email(this)).postSyncCacheEmail(config)
}
