pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
    }
}

rootProject.name = "resourceGeneratorPlugin"
include(":plugin", ":example")

