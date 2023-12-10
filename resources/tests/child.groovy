// if called not by test engine, return success
if (params.run_in_lib_test)
    return
 
// put here your logic to test child job
echo "${params.message}"
error("ChooOoOW")
