@Library("devops@artifactory") _

a = jfrog("AWS")
def builds = a.getAllBuilds()
echo "$builds"
a.promoteBuild("/DevOps :: packMultiImage", 10, "IPHO",
    "seiv-vsat-mi-pack-dev", "test", "testing promotion",
    [:], env.BUILD_USER, true)

