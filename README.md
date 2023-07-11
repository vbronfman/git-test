# Introduction 
jenkins shared library of devops projects

# Examples

## jfrog

### publishArtifacts

```
@Library("devops") _

node('linux')
{
    stage('test-with-prop'){
        sh "rm *.txt; echo 123 > file-prop-${env.BUILD_ID}.txt"

        /* 
        Input: 
            files: array = is an array of desciptors in form:
                {
                    pattern: what to publish (files mask),
                    target: where to publish (repo)
                }
            opt: object = optional set of additional options
                props: object = set of props to be set on published files,
                keepLast: integer = to keep only last N builds and remove others (including artifacts)
                sync: bool = if to wait for retention policy to be applied
                all options are optional and can be omitted
                parameter opt is also optional and can be omitted
        Output: returns buildInfo object or throws an error
        */
        jfrog("AWS").publishArtifacts(
            files=[[pattern: "file-*.txt", target: "vtselm-test"]],
            opt=[
                props: [
                    BUILD_USER: 'Overridden user name',
                    "some_extra_prop": "some-prop-value"
                ],
                keepLast: 2,
                sync: true
            ]
        )
    }
}
```
