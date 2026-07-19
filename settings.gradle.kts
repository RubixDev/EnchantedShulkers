import groovy.json.JsonSlurper

pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        maven("https://jitpack.io")
        mavenCentral()
        gradlePluginPortal()
    }
    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "com.replaymod.preprocess" -> {
                    useModule("com.github.Fallen-Breath:preprocessor:${requested.version}")
                }
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
val settings = JsonSlurper().parseText(rootDir.resolve("settings.json").readText()) as Map<String, List<String>>
for (version in settings["versions"]!!) {
    include(":$version")
    project(":$version").apply {
        projectDir = file("versions/$version")
        buildFileName = "../../common.gradle.kts"
    }
}

// MC 26.2 port: lives at versions/26.2/ but is deliberately NOT `include()`d in this build, and
// is its OWN standalone Gradle project (own settings.gradle.kts + own gradlew wrapper inside that
// directory). Two independent, verified reasons this can't be a subproject of THIS build:
//
// 1. Yarn mappings do not exist for 26.x (only Mojang official mappings are published), and the
//    replaymod-preprocess plugin's cross-version linking (`Node.link()`) requires either (a)
//    matching mapping *types* between linked nodes bridged by a hand-authored
//    intermediary<->intermediary crosswalk file (as used for all the 1.19.4-1.20.4 links above),
//    or (b) a same-MC-version yarn<->official bridge via the game's own obfuscation ("notch")
//    mappings (see PreprocessPlugin.kt afterEvaluate: `inheritedNode.mcVersion ==
//    projectNode.mcVersion` check) - neither applies to 1.20.4(yarn) -> 26.2(official), which are
//    both a different mapping type AND 6 major MC versions apart with no such crosswalk available.
//
// 2. Independently of (1): a single Gradle build cannot apply two different versions of the same
//    plugin ID across its subprojects. This repo's other subprojects apply `fabric-loom` version
//    `1.5-SNAPSHOT` (via common.gradle.kts); 26.2 needs `1.17-SNAPSHOT` (the version whose 26.2
//    support was verified against FabricMC/fabric-example-mod's `26.2` branch). Actually
//    `include()`-ing `:26.2` here and running `./gradlew :26.2:build` was tried and fails with:
//    `Error resolving plugin [id: 'fabric-loom', version: '1.17-SNAPSHOT'] > The request for this
//    plugin could not be satisfied because the plugin is already on the classpath with a
//    different version (1.5-SNAPSHOT).` This is a hard Gradle limitation, not a config mistake.
//
// See versions/26.2/build.gradle.kts and versions/26.2/settings.gradle.kts for the standalone
// project, and the port-26.2 commit message for the full report.
