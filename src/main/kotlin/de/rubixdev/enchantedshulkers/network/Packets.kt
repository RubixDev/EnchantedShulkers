package de.rubixdev.enchantedshulkers.network

//#if MC >= 12006
import de.rubixdev.enchantedshulkers.Utils.id
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
//#endif

//#if MC >= 12006
data class ConfigSyncS2CPacket(val config: NbtCompound) : CustomPayload {
    companion object {
        val ID = CustomPayload.Id<ConfigSyncS2CPacket>("config_sync".id)
        val CODEC: PacketCodec<RegistryByteBuf, ConfigSyncS2CPacket> =
            PacketCodecs.NBT_COMPOUND.xmap(::ConfigSyncS2CPacket) { it.config }.cast()
    }

    override fun getId(): CustomPayload.Id<out CustomPayload> = ID
}

object InventoryOpenC2SPacket : CustomPayload {
    val ID = CustomPayload.Id<InventoryOpenC2SPacket>("inventory_open".id)
    val CODEC: PacketCodec<RegistryByteBuf, InventoryOpenC2SPacket> =
        PacketCodec.unit(InventoryOpenC2SPacket)

    override fun getId(): CustomPayload.Id<out CustomPayload> = ID
}

object InventoryCloseC2SPacket : CustomPayload {
    val ID = CustomPayload.Id<InventoryCloseC2SPacket>("inventory_close".id)
    val CODEC: PacketCodec<RegistryByteBuf, InventoryCloseC2SPacket> =
        PacketCodec.unit(InventoryCloseC2SPacket)

    override fun getId(): CustomPayload.Id<out CustomPayload> = ID
}
//#endif
