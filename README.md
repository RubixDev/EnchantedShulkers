# EnchantedShulkers

---

**Note: This mod is for [Fabric](https://fabricmc.net/) only. If you are looking for a Forge mod with similar functionality, have a look at [Shulker Enchantments](https://modrinth.com/mod/shulker-enchantments)**

---

Adds the "Siphon" and "Refill" enchantments to Shulker Boxes

[![Requires Fabric API](https://raw.githubusercontent.com/RubixDev/Rug/1.17/fabric-api.png)](https://modrinth.com/mod/fabric-api)

## Inspiration
I was inspired to create this mod after watching [Xisumavoid's video about inventory improvements](https://www.youtube.com/watch?v=zJtJ90Vl03M&t=491s).
In that video a Forge mod called [Shulker Enchantments](https://modrinth.com/mod/shulker-enchantments) was presented.
This mod now tries to provide similar functionality for Fabric, as the only other Fabric mod with these enchantments that I could find (i.e. [BetterShulkers](https://www.curseforge.com/minecraft/mc-mods/fabric-bettershulkers)) only supports Minecraft 1.16.

## Features
This mod adds the "Siphon" and "Refill" enchantments to the game. They can be obtained just like Mending and other treasure enchantments.
The enchantments can be added to Shulker Boxes and optionally to Ender Chests (see [configuration](#configuration)) on an anvil.

### Siphon
With the Siphon enchantment, any item that you pick up will be immediately stored in an enchanted container as long as that container already has a non-full stack of that exact item inside.
The container must be inside your inventory.

### Refill
With the Refill enchantment, any item or item stack you use up will be refilled from an enchanted container as long as the used stack was held in one of the hands (see [configuration](#configuration)).
Again, the container must be inside your inventory.

## Server/Client
This mod does actually work as a server-only mod, but it is not recommended.
Players without the mod on their client cannot see these enchantments on enchanted books and containers.

## Configuration
### Client
To configure client side settings, you must have [Cloth Config API](https://modrinth.com/mod/cloth-config) and [Mod Menu](https://modrinth.com/mod/modmenu) installed alongside EnchantedShulkers.
You can then open the settings by opening the mod menu, selecting EnchantedShulkers and clicking the settings button on the right side.

The following options are available:
| **Option**                               | **Default** | **Description**                                                                                                                                                                                                                                                                                                   |
|------------------------------------------|-------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Apply glint to placed containers         | Yes         | Applies the enchantment glint to enchanted shulker boxes and ender chests that are placed in the world                                                                                                                                                                                                            |
| Prevent glint overlap with custom models | Yes         | Uses different block models and textures for closed containers to prevent overlapping glint. This does not change the look of closed containers, but always uses the vanilla textures, so you should disable this when using a custom resource pack. This option only has effect when the above option is on |

### Server
The behavior of the mod can be tweaked per world in the `enchantedshulkers.toml` config file inside you world save folder, or with the `/enchantedshulkers` command in game. Below is a list of available options.

| **Option**              | **Possible Values** | **Default Value** | **Description**                                                                          |
|-------------------------|---------------------|-------------------|------------------------------------------------------------------------------------------|
| `refillOffhand`         | `true`, `false`     | `true`            | Allow refilling stacks in the offhand                                                    |
| `refillNonStackables`   | `true`, `false`     | `false`           | Allow refilling non-stackable items like Totems of Undying                               |
| `enchantableEnderChest` | `true`, `false`     | `false`           | Allows Ender Chests to also be enchanted                                                 |
| `coloredNames`          | `true`, `false`     | `false`           | Show the names of placed enchanted containers in aqua color. This applies to all players |

## For Mod Developers
If your Mod adds a new container that should support these enchantments, you must simply add the container to the `enchantedshulkers:portable_container` item tag.
To do that, create the file `portable_container.json` inside the `src/main/resources/data/enchantedshulkers/tags/items/` folder of your mod with following contents:

```json
{
  "replace": false,
  "values": [
    "your_mod_namespace:your_container_item"
  ]
}
```

If your container does not extend the vanilla Shulker Box class, you might have to manually implement the [`EnchantableBlockEntity`](https://github.com/RubixDev/EnchantedShulkers/blob/1.19/src/main/java/de/rubixdev/enchantedshulkers/EnchantableBlockEntity.java) interface and support rendering the enchantment glint and display name colorizer.
For more information have a look at [`ShulkerBoxBlockEntityRendererMixin.java`](https://github.com/RubixDev/EnchantedShulkers/blob/1.19/src/main/java/de/rubixdev/enchantedshulkers/mixin/client/ShulkerBoxBlockEntityRendererMixin.java) and [`ShulkerBoxBlockEntityMixin.java`](https://github.com/RubixDev/EnchantedShulkers/blob/1.19/src/main/java/de/rubixdev/enchantedshulkers/mixin/ShulkerBoxBlockEntityMixin.java).

On any further questions or issues, please open an issue on [GitHub](https://github.com/RubixDev/EnchantedShulkers/issues/new).
