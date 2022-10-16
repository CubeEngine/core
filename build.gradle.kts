plugins {
    id("org.cubeengine.parent.module")
}

dependencies {
    // Configurations
    implementation("org.cubeengine:reflect-yaml")
    // Translations
    implementation("org.cubeengine:i18n")
    // Message formatting
    implementation("org.cubeengine:dirigent")
    // plugin generator for annotations
    implementation("org.cubeengine:plugin-gen")
    // Other stuff
    implementation("org.ocpsoft.prettytime:prettytime")
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
