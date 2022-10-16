plugins {
    id("org.cubeengine.parent.module")
}

dependencies {
    // Configurations
    api("org.cubeengine:reflect-yaml:3.0.1")
    // Translations
    api("org.cubeengine:i18n:1.0.4")
    // Message formatting
    api("org.cubeengine:dirigent:5.0.2")
    // plugin generator for annotations
    api("org.cubeengine:plugin-gen")
    // Other stuff
    api("org.ocpsoft.prettytime:prettytime:5.0.4.Final")
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
