import com.diffplug.gradle.spotless.BaseKotlinExtension

plugins {
    id("maven-publish")
    id("fabric-loom") version "1.5-SNAPSHOT" apply false
    // TODO: the preprocessor doesn't yet work with Kotlin 1.9
    // https://github.com/ReplayMod/remap/pull/17
    kotlin("jvm") version "1.8.22" apply false

    // https://github.com/ReplayMod/preprocessor
    // https://github.com/Fallen-Breath/preprocessor
    id("com.replaymod.preprocess") version "ce1aeb2b"

    // https://github.com/Fallen-Breath/yamlang
    id("me.fallenbreath.yamlang") version "1.3.1" apply false

    id("com.diffplug.spotless") version "6.25.0"
}

preprocess {
    val mc119 = createNode("1.19.4", 1_19_04, "yarn")
    val mc120 = createNode("1.20.1", 1_20_01, "yarn")
    val mc1202 = createNode("1.20.2", 1_20_02, "yarn")
    val mc1204 = createNode("1.20.4", 1_20_04, "yarn")

    mc1204.link(mc1202, file("versions/mapping-1.20.4-1.20.2.txt"))
    mc1202.link(mc120, file("versions/mapping-1.20.2-1.20.1.txt"))
    mc120.link(mc119, file("versions/mapping-1.20.1-1.19.4.txt"))
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

spotless {
    fun BaseKotlinExtension.customKtlint() = ktlint("1.2.1").editorConfigOverride(
        mapOf(
            "ktlint_standard_no-wildcard-imports" to "disabled",
            "ktlint_standard_blank-line-before-declaration" to "disabled",
            "ktlint_standard_spacing-between-declarations-with-annotations" to "disabled",
            // these don't play well with preprocessing
            "ktlint_standard_import-ordering" to "disabled",
            // these are replaced by the custom rule set
            "ktlint_standard_comment-spacing" to "disabled",
            "ktlint_standard_chain-wrapping" to "disabled",
        ),
    ).customRuleSets(listOf("com.github.RubixDev:ktlint-ruleset-mc-preprocessor:01fbcf09be"))

    kotlinGradle {
        target("**/*.gradle.kts")
        customKtlint()
    }
    kotlin {
        target("**/src/*/kotlin/**/*.kt")
        toggleOffOn("//#if", "//#endif")
        customKtlint()
    }
    java {
        target("**/src/*/java/**/*.java")
        toggleOffOn("//#if", "//#endif")
        // TODO: importOrder()
        removeUnusedImports()
        eclipse("4.30").configFile("eclipse-prefs.xml")
        formatAnnotations()
    }
}

tasks.register("buildAndGather") {
    subprojects {
        dependsOn(project.tasks.named("build").get())
    }
    doFirst {
        println("Gathering builds")

        fun buildLibs(p: Project) = p.buildDir.toPath().resolve("libs")
        delete(
            fileTree(buildLibs(rootProject)) {
                include("*")
            },
        )
        subprojects {
            copy {
                from(buildLibs(project)) {
                    include("*.jar")
                    exclude("*-dev.jar", "*-sources.jar")
                }
                into(buildLibs(rootProject))
                duplicatesStrategy = DuplicatesStrategy.INCLUDE
            }
        }
    }
}
