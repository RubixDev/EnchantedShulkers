package de.rubixdev.enchantedshulkers.config

import com.google.gson.Gson
import com.moandjiezana.toml.Toml
import com.moandjiezana.toml.TomlWriter
import de.rubixdev.enchantedshulkers.Mod
import de.rubixdev.enchantedshulkers.Mod.MOD_ID
import de.rubixdev.enchantedshulkers.Utils.clientModVersion
import java.io.File
import java.io.IOException
import java.util.function.Function
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.jvm.isAccessible
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.Version
import net.minecraft.nbt.NbtByte
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtInt
import net.minecraft.nbt.NbtString
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.WorldSavePath

object WorldConfig {
    private var server: MinecraftServer? = null
    private var inner = Inner()
    private val GSON = Gson()

    fun attachServer(server: MinecraftServer) {
        this.server = server
        read()
        updateResources()
    }

    fun detachServer() {
        server = null
        inner = Inner()
    }

    val options: List<String> get() = Inner::class.declaredMemberProperties.map { it.name }

    fun getOption(option: String): Any? {
        val field = getField(option)
        return field.get(inner)
    }

    fun getOptionDefault(option: String): Any? {
        val field = getField(option)
        return field.get(Inner())
    }

    fun getBooleanValue(option: String): Boolean {
        return when (val value = getOption(option)) {
            is Boolean -> value
            is Number -> value.toDouble() > 0
            else -> false
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun setOption(option: String, value: String) {
        val field = getField(option)
        when (field.returnType) {
            Boolean::class.starProjectedType -> {
                val boolField = field as KMutableProperty1<Inner, Boolean>
                when (value) {
                    "true" -> boolField.set(inner, true)
                    "false" -> boolField.set(inner, false)
                    else -> throw InvalidOptionValueException(
                        Text.translatable(
                            "commands.$MOD_ID.invalid_bool",
                            value,
                        ),
                    )
                }
            }

            Int::class.starProjectedType -> {
                val intField = field as KMutableProperty1<Inner, Int>
                val intValue = value.toIntOrNull()
                    ?: throw InvalidOptionValueException(Text.translatable("commands.$MOD_ID.invalid_int", value))
                val intOption = field.findAnnotation<IntOption>()
                if (intOption != null && (intValue < intOption.min || intValue > intOption.max)) {
                    throw InvalidOptionValueException(
                        Text.translatable(
                            "commands.$MOD_ID.invalid_int_range",
                            intOption.min,
                            intOption.max,
                            intValue,
                        ),
                    )
                }
                intField.set(inner, intValue)
            }

            else -> throw IllegalStateException("Option '$option' has an unsupported type: ${field.returnType}")
        }
        updateResources()
        write()
    }

    fun suggestions(option: String): Array<String> {
        val field = getField(option)
        if (field.returnType == Boolean::class.starProjectedType) return arrayOf("true", "false")
        val intOption = field.findAnnotation<IntOption>()
        if (intOption != null) return intOption.suggestions
        return arrayOf(getOptionDefault(option).toString())
    }

    private fun getField(name: String) =
        Inner::class.declaredMemberProperties.first { it.name == name }.apply { isAccessible = true }

    val OPTIONAL_PACKS = mapOf(
        "enchanted_ender_chest" to { inner.enchantableEnderChest },
        "enchanted_ender_pouch" to { inner.enchantableEnderChest && FabricLoader.getInstance().isModLoaded("things") },
        "augmented_chests_and_barrels" to { inner.augmentableChestsAndBarrels },
    )

    private val NEW_FABRIC_API = FabricLoader.getInstance().getModContainer("fabric-api")
        .orElseThrow(::RuntimeException).metadata.version >= Version.parse("0.95.4")
    private fun packId(name: String) = "enchantedshulkers:${if (NEW_FABRIC_API) "${name}_resourcepacks${File.separator}$name" else name}"

    private fun updateResources() {
        val manager = server?.dataPackManager ?: return
        val loadedPacks = manager.enabledProfiles.toMutableList()

        for ((pack, predicate) in OPTIONAL_PACKS) {
            val id = packId(pack)
            val isLoaded = manager.enabledNames.contains(id)
            val shouldBeLoaded = predicate()
            if (isLoaded == shouldBeLoaded) continue

            val packProfile = manager.getProfile(id)!!
            if (shouldBeLoaded) {
                packProfile.initialPosition.insert(loadedPacks, packProfile, Function.identity(), false)
            } else {
                loadedPacks.remove(packProfile)
            }
        }

        server!!.reloadResources(loadedPacks.map { it.name }).exceptionally {
            Mod.LOGGER.warn("Failed to execute reload", it)
            null
        }
    }

    private fun getConfigPath() = server!!.getSavePath(WorldSavePath.ROOT).resolve("$MOD_ID.toml")

    private fun read() {
        try {
            val toml = Toml().read(getConfigPath().toFile()).toMap()
            if (toml["generateBooks"] == false) {
                if ("generateSiphon" !in toml) toml["generateSiphon"] = false
                if ("generateRefill" !in toml) toml["generateRefill"] = false
            }
            toml.remove("generateBooks")
            if (toml["nestedContainers"] == true) toml["nestedContainers"] = 255
            if (toml["nestedContainers"] == false) toml["nestedContainers"] = 0
            inner = GSON.fromJson(GSON.toJsonTree(toml), Inner::class.java)
            Mod.LOGGER.info("Loaded settings from $MOD_ID.toml")
        } catch (e: Throwable) {
            Mod.LOGGER.warn("Could not read config, using default settings: $e")
        }
    }

    private fun write() {
        if (server == null) return
        try {
            TomlWriter().write(inner, getConfigPath().toFile())
            Mod.LOGGER.info("Sending updated config to clients")
        } catch (e: IOException) {
            Mod.LOGGER.error("Could not write config: $e")
        }
        server!!.playerManager.playerList.forEach(::sendConfigToClient)
    }

    fun sendConfigToClient(player: ServerPlayerEntity) {
        if (player.clientModVersion() > 0) {
            if (player.server.isHost(player.gameProfile)) {
                Mod.LOGGER.info("Not sending config to integrated server host ${player.nameForScoreboard}")
                return
            }
            Mod.LOGGER.info("Sending world config to ${player.nameForScoreboard}")
            val config = NbtCompound()
            options.forEach {
                val value = when (val value = getOption(it)) {
                    is Boolean -> NbtByte.of(value)
                    is Int -> NbtInt.of(value)
                    else -> NbtString.of(value.toString())
                }
                config.put(it, value)
            }
            ServerPlayNetworking.send(player, Mod.CONFIG_SYNC_PACKET_ID, PacketByteBufs.create().writeNbt(config))
        } else {
            Mod.LOGGER.info("Not sending config to ${player.nameForScoreboard}")
        }
    }

    @JvmStatic
    @get:JvmName("refillOffhand")
    val refillOffhand get() = inner.refillOffhand
    @JvmStatic
    @get:JvmName("refillNonStackables")
    val refillNonStackables get() = inner.refillNonStackables
    @JvmStatic
    @get:JvmName("refillProjectiles")
    val refillProjectiles get() = inner.refillProjectiles
    @JvmStatic
    @get:JvmName("coloredNames")
    val coloredNames get() = inner.coloredNames
    @JvmStatic
    @get:JvmName("creativeSiphon")
    val creativeSiphon get() = inner.creativeSiphon
    @JvmStatic
    @get:JvmName("creativeRefill")
    val creativeRefill get() = inner.creativeRefill
    @JvmStatic
    @get:JvmName("creativeVacuum")
    val creativeVacuum get() = inner.creativeVacuum
    @JvmStatic
    @get:JvmName("creativeVoid")
    val creativeVoid get() = inner.creativeVoid
    @JvmStatic
    @get:JvmName("generateRefill")
    val generateRefill get() = inner.generateRefill
    @JvmStatic
    @get:JvmName("generateSiphon")
    val generateSiphon get() = inner.generateSiphon
    @JvmStatic
    @get:JvmName("generateVacuum")
    val generateVacuum get() = inner.generateVacuum
    @JvmStatic
    @get:JvmName("generateVoid")
    val generateVoid get() = inner.generateVoid
    @JvmStatic
    @get:JvmName("generateAugment")
    val generateAugment get() = inner.generateAugment
    @JvmStatic
    @get:JvmName("nestedContainers")
    val nestedContainers get() = inner.nestedContainers
    @JvmStatic
    @get:JvmName("strongerSiphon")
    val strongerSiphon get() = inner.strongerSiphon
    @JvmStatic
    @get:JvmName("weakerVacuum")
    val weakerVacuum get() = inner.weakerVacuum
    @JvmStatic
    @get:JvmName("maxAugmentLevel")
    val maxAugmentLevel get() = inner.maxAugmentLevel
    @JvmStatic
    @get:JvmName("augmentableChestsAndBarrels")
    val augmentableChestsAndBarrels get() = inner.augmentableChestsAndBarrels

    private class Inner {
        var refillOffhand = true
        var refillNonStackables = false
        var refillProjectiles = true
        var enchantableEnderChest = false
        var coloredNames = false
        var creativeSiphon = false
        var creativeRefill = false
        var creativeVacuum = false
        var creativeVoid = false
        var generateRefill = true
        var generateSiphon = true
        var generateVacuum = false
        var generateVoid = false
        var generateAugment = false
        @IntOption(min = 0, max = Short.MAX_VALUE.toInt(), suggestions = ["0", "1", "127", "255"])
        var nestedContainers = 255
        var strongerSiphon = false
        var weakerVacuum = false
        @IntOption(min = 1, max = 10, suggestions = ["1", "2", "3", "6", "9", "10"])
        var maxAugmentLevel = 3
        var augmentableChestsAndBarrels = false
    }
}
