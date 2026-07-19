// MC 26.2 port of EnchantedShulkers.
//
// This is a plain, standalone fabric-loom build script - deliberately NOT wired through
// `common.gradle.kts` / the `com.replaymod.preprocess` plugin used by the other `versions/*`
// subprojects. See the comment in `settings.gradle.kts` for why (Yarn does not exist for 26.x,
// and the preprocessor's cross-version node linking cannot bridge a different mapping *type*
// across a 6-major-version gap without a hand-authored intermediary crosswalk, which does not
// exist here and is not something we can generate in this session).
//
// STATUS: toolchain/scaffold only. Real source has NOT been ported yet - see the port-26.2
// commit message / final report for the two verified architectural blockers (Enchantment is now
// a non-subclassable `final record`, and ItemStack has no NBT API anymore, both core to this
// mod's design) that need a design decision before porting the actual gameplay code.
plugins {
    // MUST be the fully-qualified plugin ID. The short form `id("fabric-loom")` resolves to
    // something else that loads and even logs "Fabric Loom: 1.17.16" but never wires up Loom's
    // Minecraft/mappings setup at all, failing later with a confusing
    // `Configuration 'mappings' has no dependencies` (or, with an explicit
    // `mappings(loom.officialMojangMappings())` workaround attempt, `Failed to find official
    // mojang mappings for 26.2`). Root-caused empirically by diffing against
    // FabricMC/fabric-example-mod's `26.2` branch, which uses the fully-qualified ID. Confirmed
    // fixed in an isolated (`-g`), no-daemon, from-scratch Gradle run (no shared-cache
    // involvement) - see the port-26.2 commit message.
    id("net.fabricmc.fabric-loom") version "1.17-SNAPSHOT"
    id("maven-publish")
    kotlin("jvm") version "2.4.0"
}

val modId = property("mod_id") as String
val modVersion = property("mod_version") as String
val mavenGroup = property("maven_group") as String

version = modVersion
group = mavenGroup

repositories {
    mavenCentral()
}

dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    // No `mappings(...)` call: Yarn does not exist for 26.x, and Loom defaults to official Mojang
    // mappings automatically once the plugin is applied under its correct, fully-qualified ID
    // (see the `plugins` block comment above for why an explicit call was tried first and made
    // things worse).
    //
    // Plain `implementation`, NOT `modImplementation`: official-mappings-only projects have no
    // intermediary remap layer, so Loom 1.17 doesn't register the `modXxx` configuration
    // variants at all (`modImplementation` fails with "Could not find method modImplementation()"
    // here) - confirmed against fabric-example-mod's `26.2` branch, which also uses plain
    // `implementation`.
    implementation("net.fabricmc:fabric-loader:${property("loader_version")}")
    implementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_version")}")
    implementation("net.fabricmc:fabric-language-kotlin:${property("fabric_kotlin_version")}")
}

loom {
    accessWidenerPath = file("$modId.accesswidener")
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 25
    options.encoding = "UTF-8"
}

kotlin {
    jvmToolchain(25)
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
    withSourcesJar()
}

base {
    archivesName = "$modId-mc${property("minecraft_version")}"
}

val modName = property("mod_name") as String
val loaderVersion = property("loader_version") as String
val fabricKotlinVersion = property("fabric_kotlin_version") as String

tasks.processResources {
    inputs.property("id", modId)
    inputs.property("name", modName)
    inputs.property("version", modVersion)

    filesMatching("fabric.mod.json") {
        // Values captured into local vals above and referenced here: inside this nested
        // `filesMatching` lambda, Kotlin resolves an unqualified `property(...)` call against the
        // CopySpec receiver, not the Project, so `property("loader_version")` here fails with
        // "Could not get unknown property 'loader_version'".
        expand(
            mapOf(
                "id" to modId,
                "name" to modName,
                "version" to modVersion,
                "loader_version" to loaderVersion,
                "fabric_kotlin_version" to fabricKotlinVersion,
            ),
        )
    }
}

tasks.jar {
    // This is a standalone Gradle project (no parent), so `rootProject` == this project itself.
    // The repo's LICENSE lives two directories up (versions/26.2/../../LICENSE).
    from(file("../../LICENSE"))
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = base.archivesName.get()
            from(components["java"])
        }
    }
    repositories {
        // intentionally empty - local build/inspection only
    }
}
