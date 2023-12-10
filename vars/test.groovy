def call(branch, clean=true){ 
    def test_result = true
    def res = LibTest.run(this, branch, clean)
    res.each {
        job_ok = "${it.result}"=="SUCCESS"
        test_result &= job_ok
        if (!job_ok)
        {
            if (it.logs)
                echo "${it.name} LOGS: ${it.logs}"
            if (it.reason)
                echo "${it.name} THROWS: ${it.logs}"
        }
    }
    res.each{ echo "${it.name} : ${it.result}" }
    if (!test_result)
        error("Test failed")
}
