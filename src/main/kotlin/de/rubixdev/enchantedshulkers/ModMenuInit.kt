package de.rubixdev.enchantedshulkers

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import de.rubixdev.enchantedshulkers.config.ClientConfigImpl
import me.shedaniel.autoconfig.AutoConfig
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment

@Environment(EnvType.CLIENT)
class ModMenuInit : ModMenuApi {
    override fun getModConfigScreenFactory() = ConfigScreenFactory { parent ->
        AutoConfig.getConfigScreen(ClientConfigImpl.Inner::class.java, parent).get()
    }
}
