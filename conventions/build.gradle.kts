plugins {
    `kotlin-dsl`
    `maven-publish`
    signing
}

group = "org.cubeengine.gradle"
version = "1.0.0-SNAPSHOT"

// repos **used by** this convention
repositories {
    mavenCentral()
    gradlePluginPortal()
    maven("https://repo.cubeengine.org")
    mavenLocal()
}

// pull plugins as implementation dependencies here:
dependencies {
    api(plugin("org.jetbrains.kotlin.jvm", "1.7.20"))
    api(plugin("io.github.gradle-nexus.publish-plugin", "1.1.0"))
    api(plugin("org.spongepowered.gradle.plugin", "2.0.2"))
    api(plugin("org.cadixdev.licenser", "0.6.1"))
}

fun plugin(id: String, version: String) = "$id:$id.gradle.plugin:$version"

publishing {
    repositories {
        maven {
            name = "cubyte"
            val repoName = if (project.version.toString().endsWith("-SNAPSHOT")) "snapshots" else "releases"
            url = uri("https://maven.cubyte.org/repository/$repoName/")
            credentials(PasswordCredentials::class)
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications)
}

tasks.publish {
    dependsOn(tasks.check)
}
