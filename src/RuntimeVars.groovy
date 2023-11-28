class RuntimeVars implements Serializable {
    private def steps

    RuntimeVars(steps) {
        this.steps = steps
    }

    static def getFileName() {
        return 'RuntimeVars.json'
    }

    static def getStashName() {
        return 'RuntimeVars'
    }

    def recv1() {
        steps.unstash(name: getStashName())
        def props = steps.readJSON(file: getFileName(), returnPojo: true)
        return props
    }

    def send(props) {
        def props1 = [:]

        // NOTE: This always fails on first invocation.
        try {
            props1 = this.recv1()
        }
        catch (e) {}
 
        // NOTE: If _k_ already exists in _props1_, the value shall be overwritten.
        props1.putAll(props)

        // Guarantees _props_ are useable from the workflow script as environment variables.
        props.each { k, v ->
            steps.env.setProperty(k, v)
        }

        steps.writeJSON(json: props1, file: getFileName(), pretty: 1)
        steps.stash(name: getStashName(), includes: getFileName())
    }

    def recv(keys) {
        return this.recv1().subMap(keys)
    }

    def archive() {
        // NOTE: Otherwise might prevent other post steps from running.
        try {
            steps.unstash(name: getStashName())
        }
        catch (e) {}

        steps.archiveArtifacts(artifacts: getFileName(), allowEmptyArchive: true)
    }
}
