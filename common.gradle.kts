import net.fabricmc.loom.task.RemapJarTask
import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import kotlin.reflect.KProperty
import kotlin.reflect.full.starProjectedType

plugins {
    id("maven-publish")
    id("fabric-loom")
    kotlin("jvm")
    id("com.replaymod.preprocess")
    id("me.fallenbreath.yamlang")
}

val mcVersion: Int by project.extra

@Suppress("PropertyName")
class Props {
    // automatically convert booleans and integers from the string properties
    private inner class Prop {
        @Suppress("UNCHECKED_CAST")
        operator fun <T> getValue(thisRef: Any?, property: KProperty<*>): T = when (property.returnType) {
            Boolean::class.starProjectedType -> project.extra[property.name].toString().toBoolean()
            Int::class.starProjectedType -> project.extra[property.name].toString().toInt()
            else -> project.extra[property.name]
        } as T
    }
    private val prop = Prop()

    //// Global Properties ////
    val loader_version: String by prop

    val mod_id: String by prop
    val mod_name: String by prop
    val mod_version: String by prop
    val maven_group: String by prop
    val archives_base_name: String by prop

    val conditionalmixin_version: String by prop
    val fabric_asm_version: String by prop
    val toml4j_version: String by prop
    val mixinsquared_version: String by prop
    val fabric_kotlin_version: String by prop

    val run_with_compat_mods: Boolean by prop

    //// Version Specific Properties ////
    val minecraft_version: String by prop
    val yarn_mappings: String by prop

    val minecraft_dependency: String by prop

    val fabric_version: String by prop
    val cloth_version: String by prop
    val modmenu_version: String by prop
    val polymer_version: String by prop
    val server_translations_version: String by prop
    val sgui_version: String by prop

    val reinfshulkers: Boolean by prop
    val reinfshulkers_version: String by prop
    val reinfcore_version: String by prop

    val shulker_plus: Boolean by prop
    val shulker_plus_version: String by prop

    val split_shulkers: Boolean by prop
    val split_shulkers_version: String by prop

    val quickshulker: Boolean by prop
    val quickshulker_version: String by prop
    val shulkerutils_version: String by prop
    val kyrptconfig_version: String by prop

    val shulker_slot: Boolean by prop
    val shulker_slot_version: String by prop
    val trinkets_version: String by prop
    val cca_version: String by prop
    val spectrelib: Boolean by prop
    val spectrelib_version: String by prop

    val clickopener: Boolean by prop
    val clickopener_version: String by prop

    val shulkerboxtooltip: Boolean by prop
    val shulkerboxtooltip_version: String by prop

    val peek: Boolean by prop
    val peek_version: String by prop
}
val props: Props = Props()

val mixinConfigPath = "${props.mod_id}.mixins.json"
val langDir = "assets/${props.mod_id}/lang"
val langDirData = "data/${props.mod_id}/lang"

repositories {
    // Fabric ASM and MixinSquared
    maven { url = uri("https://jitpack.io") }
    // Cloth Config
    maven { url = uri("https://maven.shedaniel.me/") }
    // Mod Menu and Trinkets
    maven { url = uri("https://maven.terraformersmc.com/releases/") }
    // Polymer and Server Translations
    maven { url = uri("https://maven.nucleoid.xyz") }
    // Reinforced Core and Shulkers
    maven { url = uri("https://raw.githubusercontent.com/Aton-Kish/mcmod/maven") }
    // Quick Shulker deps
    maven { url = uri("https://maven.kyrptonaught.dev") }
    // Cardinal Components API (required by Trinkets)
    maven { url = uri("https://maven.ladysnake.org/releases") }
    // Spectrelib (required by Shulker Box Slot)
    maven { url = uri("https://maven.theillusivec4.top/") }
    // Shulker Box Tooltip
    maven { url = uri("https://maven.misterpemodder.com/libs-release") }
    // Modrinth Maven for other compat tested mods
    maven { url = uri("https://api.modrinth.com/maven") }
}

// https://github.com/FabricMC/fabric-loader/issues/783
configurations {
    "modRuntimeOnly" {
        exclude(group = "net.fabricmc", module = "fabric-loader")
    }
}

dependencies {
    // loom
    "minecraft"("com.mojang:minecraft:${props.minecraft_version}")
    "mappings"("net.fabricmc:yarn:${props.yarn_mappings}:v2")
    "modImplementation"("net.fabricmc:fabric-loader:${props.loader_version}")

    // dependencies
    "modImplementation"("net.fabricmc.fabric-api:fabric-api:${props.fabric_version}")
    "modImplementation"("net.fabricmc:fabric-language-kotlin:${props.fabric_kotlin_version}")

    // optional dependencies
    "modApi"("me.shedaniel.cloth:cloth-config-fabric:${props.cloth_version}") {
        exclude(group = "net.fabricmc.fabric-api")
    }
    "modApi"("com.terraformersmc:modmenu:${props.modmenu_version}")

    // libraries
    "include"("modImplementation"("com.github.Fallen-Breath:conditional-mixin:${props.conditionalmixin_version}")!!)
    "include"("modImplementation"("com.github.Chocohead:Fabric-ASM:${props.fabric_asm_version}")!!)
    "include"(
        "modApi"("io.hotmoka:toml4j:${props.toml4j_version}") {
            exclude(module = "gson")
        },
    )
    "include"("implementation"("annotationProcessor"("com.github.bawnorton.mixinsquared:mixinsquared-fabric:${props.mixinsquared_version}")!!)!!)
    "include"("modImplementation"("eu.pb4:polymer-core:${props.polymer_version}")!!)
    "include"("modImplementation"("xyz.nucleoid:server-translations-api:${props.server_translations_version}")!!)
    "include"("modImplementation"("eu.pb4:sgui:${props.sgui_version}")!!)

    // mods tested for compatibility
    fun modCompat(dependencyNotation: String, dependencyConfiguration: ExternalModuleDependency.() -> Unit = {}) =
        if (props.run_with_compat_mods) {
            "modImplementation"(dependencyNotation, dependencyConfiguration)
        } else {
            "modCompileOnly"(dependencyNotation, dependencyConfiguration)
        }
    // - Reinforced Shulkers
    if (props.reinfshulkers) {
        modCompat("atonkish.reinfshulker:reinforced-shulker-boxes:${props.reinfshulkers_version}") {
            exclude(group = "net.fabricmc.fabric-api")
            exclude(group = "atonkish.reinfcore")
            exclude(group = "atonkish.reinfchest")
            exclude(group = "net.kyrptonaught")
            exclude(group = "com.misterpemodder")
        }
        modCompat("atonkish.reinfcore:reinforced-core:${props.reinfcore_version}") {
            exclude(group = "net.fabricmc.fabric-api")
            exclude(group = "com.terraformersmc")
            exclude(group = "me.shedaniel.cloth")
        }
    }
    // - Shulker+
    if (props.shulker_plus) {
        // TODO: change to `modCompat` when compatible with split shulkers (https://github.com/CursedFlames/MCTweaks/issues/3)
        "modCompileOnly"("maven.modrinth:shulker+:${props.shulker_plus_version}")
    }
    // - Split Shulker Boxes
    if (props.split_shulkers) {
        modCompat("maven.modrinth:split-shulker-boxes:${props.split_shulkers_version}")
    }
    // - Quick Shulker
    if (props.quickshulker) {
        modCompat("maven.modrinth:quickshulker:${props.quickshulker_version}")
        modCompat("net.kyrptonaught:shulkerutils:${props.shulkerutils_version}")
        modCompat("net.kyrptonaught:kyrptconfig:${props.kyrptconfig_version}")
    }
    // - Shulker Box Slot
    if (props.shulker_slot) {
        modCompat("maven.modrinth:shulker-box-slot:${props.shulker_slot_version}")
        modCompat("dev.emi:trinkets:${props.trinkets_version}") { exclude(group = "net.fabricmc.fabric-api") }
        modCompat("dev.onyxstudios.cardinal-components-api:cardinal-components-base:${props.cca_version}") { exclude(group = "net.fabricmc.fabric-api") }
        modCompat("dev.onyxstudios.cardinal-components-api:cardinal-components-entity:${props.cca_version}") { exclude(group = "net.fabricmc.fabric-api") }
        modCompat("dev.onyxstudios.cardinal-components-api:cardinal-components-item:${props.cca_version}") { exclude(group = "net.fabricmc.fabric-api") }
        if (props.spectrelib) {
            modCompat("com.illusivesoulworks.spectrelib:spectrelib-fabric:${props.spectrelib_version}") { exclude(group = "net.fabricmc.fabric-api") }
        }
    }
    // - Click Opener
    if (props.clickopener) {
        modCompat("maven.modrinth:clickopener:${props.clickopener_version}")
    }
    // - Shulker Box Tooltip
    if (props.shulkerboxtooltip) {
        modCompat("com.misterpemodder:shulkerboxtooltip-fabric:${props.shulkerboxtooltip_version}") {
            exclude(group = "net.fabricmc.fabric-api")
            exclude(group = "me.shedaniel.cloth")
            exclude(group = "com.terraformersmc")
        }
    }
    // - Peek
    if (props.peek) {
        // TODO: remapping(?) breaks this in a dev env for some reason, so we keep it compile only. (getItems() (mojmaps) is remapped to getHeldStacks() but should be method_11282() (yarn/intermediary))
        "modCompileOnly"("maven.modrinth:peek:${props.peek_version}")
    }
}

loom {
    accessWidenerPath = file("${props.mod_id}.accesswidener")
    runConfigs.all {
        // to make sure it generates all "Minecraft Client (:subproject_name)" applications
        isIdeConfigGenerated = true
        runDir = "../../run"
        vmArg("-Dmixin.debug.export=true")
    }
}

tasks.named<RemapJarTask>("remapJar") {
    remapperIsolation = true
}

val javaCompatibility = when {
    mcVersion >= 11800 -> JavaVersion.VERSION_17
    mcVersion >= 11700 -> JavaVersion.VERSION_16
    else -> JavaVersion.VERSION_1_8
}

tasks.withType<JavaCompile> {
    sourceCompatibility = javaCompatibility.toString()
    targetCompatibility = javaCompatibility.toString()
}

var versionSuffix = ""
// detect github action environment variables
// https://docs.github.com/en/actions/learn-github-actions/environment-variables#default-environment-variables
if (System.getenv("BUILD_RELEASE") != "true") {
    val buildNumber = System.getenv("BUILD_ID")
    versionSuffix += buildNumber?.let { "+build.$it" } ?: "-SNAPSHOT"
}
val fullModVersion = props.mod_version + versionSuffix

version = "v$fullModVersion"
group = props.maven_group

base {
    archivesName = "${props.archives_base_name}-mc${props.minecraft_version}"
}

// See https://youtrack.jetbrains.com/issue/IDEA-296490
// if IDEA complains about "Cannot resolve resource filtering of MatchingCopyAction" and you want to know why
tasks.named<ProcessResources>("processResources") {
    from("${props.mod_id}.accesswidener")

    inputs.property("id", props.mod_id)
    inputs.property("name", props.mod_name)
    inputs.property("version", fullModVersion)
    inputs.property("minecraft_dependency", props.minecraft_dependency)

    filesMatching("fabric.mod.json") {
        val valueMap = mapOf(
            "id" to props.mod_id,
            "name" to props.mod_name,
            "version" to fullModVersion,
            "minecraft_dependency" to props.minecraft_dependency,
            "fabric_loader" to props.loader_version,
            "fabric_kotlin_version" to props.fabric_kotlin_version,
        )
        expand(valueMap)
    }

    filesMatching(mixinConfigPath) {
        expand("compatibility_level" to "JAVA_${javaCompatibility.ordinal + 1}")
    }
}

// https://github.com/Fallen-Breath/yamlang
yamlang {
    targetSourceSets = listOf(sourceSets.main.get())
    inputDir = langDir
}

tasks.register<Copy>("copyLangFiles") {
    mustRunAfter("yamlangConvertMainResources")
    val basePath = sourceSets.main.get().output.resourcesDir!!.toPath()
    from(basePath.resolve(langDir))
    into(basePath.resolve(langDirData))
    include("*.json")
}

// ensure that the encoding is set to UTF-8, no matter what the system default is
// this fixes some edge cases with special characters not displaying correctly
// see http://yodaconditions.net/blog/fix-for-java-file-encoding-problems-with-gradle.html
tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.compilerArgs.apply {
        add("-Xlint:deprecation")
        add("-Xlint:unchecked")
    }
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = javaCompatibility.toString()
    kotlinOptions.freeCompilerArgs = listOf("-Xjvm-default=all")
}

java {
    // Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
    // if it is present.
    // If you remove this line, sources will not be generated.
    withSourcesJar()
}

tasks.named<Jar>("jar") {
    dependsOn("copyLangFiles")
    from(rootProject.file("LICENSE")) {
        rename { "${it}_${props.archives_base_name}" }
    }
}

// configure the maven publication
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = base.archivesName.get()
            from(components["java"])
        }
    }

    // select the repositories you want to publish to
    repositories {
        // uncomment to publish to the local maven
        // mavenLocal()
    }
}
