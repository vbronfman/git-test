# Unit tests for jenkins shared library

## Motivation

Tests are serving 2 main goals:
* Code quality: keep our library robust
* Readability: test are serving as usage examples

## Usage

```groovy
@Library("devops") _
test(<BRANCH-NAME>, clean)
```

The **BRANCH-NAME** parameter is the name of the shared library branch that is to be tested.
Set **clean** parameter to `true` to remove the created jobs when test is finished

## How it works

Each file in this folder with **.groovy** externsion is a single test job
1. Started test creates jenkins folder named [lib-tests](https://jenkins-seiv.gilat.tech/job/lib-tests/)
1. In the *lib-test* folder each file is created as a job with name *branch-name*-*file-name*
    1. The code is prepended with `@Library("devops@<BRANCH-NAME>") _`
1. Each job is started and expected to finish with SUCCESS.
    1. Failed job fails all the test and its log is printed.
1. All the created jobs are removed from the folder *lib-test* if `clean` param eq `true`

## Runtime env

Each created job is called with two parameters:
* string `tested_branch` = name of the tester branch 
* bool `run_in_lib_test` = true
Feel free to add more if required

## Examples

Feel free to look at **.groovy** files in this folder :)

Happy debuging
