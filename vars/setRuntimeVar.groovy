def call(k, v) {
    (new RuntimeVars(this)).send([(k): v])
}
