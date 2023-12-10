def b = utils.normalizeBranchName("${params.tested_branch}")
def kid = build propagate: false, job: "lib-tests/${b}-child", parameters: [
    string(name: 'message', value: "Hello"),
]

if ("${kid.result}" != "FAILURE")
    error("Expected error in child not found")
