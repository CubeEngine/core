plugins {
    `java-library`
    id("org.cubeengine.parent.module")
    signing
}

signing {
    useGpgCmd()
    sign(publishing.publications)
}

tasks.publish {
    dependsOn(tasks.check)
}

tasks.register("cleanAll") {
    group = "build"
    description = "Clean both libcube and conventions."

    dependsOn(tasks.clean)
    dependsOn(gradle.includedBuild("conventions").task(":clean"))
}

tasks.register("publishAll") {
    group = "publishing"
    description = "Publish both libcube and conventions."

    dependsOn(tasks.publish)
    dependsOn(gradle.includedBuild("conventions").task(":publish"))
}
