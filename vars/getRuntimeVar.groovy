def call(k) {
    // _recv_ returns a map, only get _first_ (and only) of the 
    // _values_ set.
    return (new RuntimeVars(this)).recv([k]).values().first()
}
