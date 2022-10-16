plugins {
    id("org.cubeengine.parent.module")
}

dependencies {
    // Configurations
    implementation("org.cubeengine:reflect-yaml:3.0.0")
    // Translations
    implementation("org.cubeengine:i18n:1.0.4")
    // Message formatting
    implementation("org.cubeengine:dirigent:5.0.2")
    // plugin generator for annotations
    implementation("org.cubeengine:plugin-gen")

    // Other stuff
    implementation("org.ocpsoft.prettytime:prettytime:5.0.4.Final")
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
