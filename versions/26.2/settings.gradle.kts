// This is a standalone Gradle project (own wrapper, own settings) - see the comment block in
// ../../settings.gradle.kts for why it is NOT `include()`d into the main repo's multi-project
// build (two independent, verified reasons: the replaymod-preprocess mapping-type/version-gap
// limitation, and Gradle's hard "one plugin version per build" rule colliding with the other
// subprojects' `fabric-loom` 1.5-SNAPSHOT vs. this project's required 1.17-SNAPSHOT).
pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "enchantedshulkers-26.2"
