rootProject.name = "libcube"

pluginManagement {
    includeBuild("conventions")
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://repo.spongepowered.org/repository/maven-public")
        mavenLocal()
    }
}


