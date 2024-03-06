# EnchantedShulkers

---

**Note: This mod is for [Fabric](https://fabricmc.net/) only. If you are looking
for a Forge mod with similar functionality, have a look at
[Shulker Enchantments](https://modrinth.com/mod/shulker-enchantments)**

---

Adds some helpful enchantments for Shulker Boxes

The project setup is based off of
[Fallen-Breath's template](https://github.com/Fallen-Breath/fabric-mod-template/tree/b2352030438790d71d586f6654a6ea31a394906a).

## Inspiration

I was inspired to create this mod after watching
[Xisumavoid's video about inventory improvements](https://www.youtube.com/watch?v=zJtJ90Vl03M&t=491s).
In that video a Forge mod called
[Shulker Enchantments](https://modrinth.com/mod/shulker-enchantments) was
presented. This mod now tries to provide similar functionality for Fabric, as
the only other Fabric mod with these enchantments that I could find (i.e.
[BetterShulkers](https://www.curseforge.com/minecraft/mc-mods/fabric-bettershulkers))
only supports Minecraft 1.16.

## Features

This mod adds the "Siphon" and "Refill" enchantments to the game. They can be
obtained just like Mending and other treasure enchantments. The enchantments can
be added to Shulker Boxes and optionally to Ender Chests (see
[configuration](#configuration)) on an anvil.

### Siphon

With the Siphon enchantment, any item that you pick up will be immediately
stored in an enchanted container as long as that container already has a
non-full stack of that exact item inside. The container must be inside your
inventory.

### Refill

With the Refill enchantment, any item or item stack you use up will be refilled
from an enchanted container as long as the used stack was held in one of the
hands (see [configuration](#configuration)). Again, the container must be inside
your inventory.

### Vacuum

The Vacuum enchantment doesn't generate by default making it unobtainable in
survival. To change that, [set the `generateVacuum` option to `true`](#server)
(and optionally disable Siphon by setting `generateSiphon` to `false`). Its
behavior is very similar to that of the Siphon enchantment, with the key
difference that it doesn't require the same item to already be present in the
enchanted container. This means a container enchanted with Vacuum will take all
items you pick up as long as it has space to do so.

### Void

Same as [the Vacuum enchantment](#vacuum), the Void enchantment also doesn't
generate by default. To make it obtainable in survival mode,
[enable the `generateVoid` option](#server). When you pick up items of the same
type as a container with the Void enchantment contains, the picked up items will
be deleted. The enchanted container does not require space to put the deleted
items, and unlike Siphon, the stacks in the enchanted container can be full.

### Augment

The Augment enchantment expands the size of a Shulker Box. It is available in
different levels where each level adds nine more slots to the Shulker's
inventory. Just like [Vacuum](#vacuum) and [Void](#void) it is unobtainable in
survival when using the default settings, but that can be configured with the
[`generateAugment` option](#server). The maximum level can also be changed with
the [`maxAugmentLevel` option](#server).

## A note on Resource Packs

When using EnchantedShulkers with a resource pack that alters the look of
Shulker Boxes and/or Ender Chests, the enchanted versions of those items and
blocks will still use the vanilla texture while closed. This had to be done to
fix overlapping glint effects on said blocks.

You can patch your resource pack to include the necessary textures for this mod
at <https://enchantedshulkers.rubixdev.de>.

## Server/Client

This mod does actually work as a server-only mod, but it is recommended to be
used on both client and server. Players without the mod on their client do not
see an enchantment glint on enchanted containers.

## I just wanna quickly try 'em out

If you just want to quickly test the enchantments in a creative world, there are
multiple options to do so.

**Option one**: You can search for the enchanted books in the creative inventory
and apply them with an anvil.

**Option two**: You can give yourself the enchanted books with a command and
apply them with an anvil. Note that the IDs begin with `enchantedshulkers:` and
not `minecraft:`.

- Command for Siphon:
  `/give @s minecraft:enchanted_book{StoredEnchantments:[{id:"enchantedshulkers:siphon",lvl:1}]}`
- Command for Refill:
  `/give @s minecraft:enchanted_book{StoredEnchantments:[{id:"enchantedshulkers:refill",lvl:1}]}`

**Option three**: You can enchant the item you're holding with the `/enchant`
command.

- Command for Siphon: `/enchant @s enchantedshulkers:siphon`
- Command for Refill: `/enchant @s enchantedshulkers:refill`

## Configuration

### Client

To configure client side settings, you must have
[Cloth Config API](https://modrinth.com/mod/cloth-config) and
[Mod Menu](https://modrinth.com/mod/modmenu) installed alongside
EnchantedShulkers. You can then open the settings by opening the mod menu,
selecting EnchantedShulkers and clicking the settings button on the right side.

The following options are available:

| **Option**                               | **Default** | **Description**                                                                                                                                                                                                                                                                                              |
| ---------------------------------------- | ----------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| Apply glint to placed containers         | Yes         | Applies the enchantment glint to enchanted shulker boxes and ender chests that are placed in the world                                                                                                                                                                                                       |
| Prevent glint overlap with custom models | Yes         | Uses different block models and textures for closed containers to prevent overlapping glint. This does not change the look of closed containers, but always uses the vanilla textures, so you should disable this when using a custom resource pack. This option only has effect when the above option is on |
| Refill while inventory is open           | No          |                                                                                                                                                                                                                                                                                                              |

### Server

The behavior of the mod can be tweaked per world in the `enchantedshulkers.toml`
config file inside you world save folder, or with the `/enchantedshulkers`
command in game. Below is a list of available options.

| **Option**              | **Possible Values**                 | **Default Value** | **Description**                                                                                                                                 |
| ----------------------- | ----------------------------------- | ----------------- | ----------------------------------------------------------------------------------------------------------------------------------------------- |
| `refillOffhand`         | `true`, `false`                     | `true`            | Allow refilling stacks in the offhand                                                                                                           |
| `refillNonStackables`   | `true`, `false`                     | `false`           | Allow refilling non-stackable items like Totems of Undying                                                                                      |
| `refillProjectiles`     | `true`, `false`                     | `true`            | Allow refilling projectiles in other inventory slots                                                                                            |
| `enchantableEnderChest` | `true`, `false`                     | `false`           | Allows Ender Chests to also be enchanted                                                                                                        |
| `coloredNames`          | `true`, `false`                     | `false`           | Show the names of placed enchanted containers in aqua color. This applies to all players                                                        |
| `creativeSiphon`        | `true`, `false`                     | `false`           | Enable the Siphon enchantment for creative players                                                                                              |
| `creativeRefill`        | `true`, `false`                     | `false`           | Enable the Refill enchantment for creative players                                                                                              |
| `creativeVacuum`        | `true`, `false`                     | `false`           | Enable the Vacuum enchantment for creative players                                                                                              |
| `creativeVoid`          | `true`, `false`                     | `false`           | Enable the Void enchantment for creative players                                                                                                |
| `generateSiphon`        | `true`, `false`                     | `true`            | Make the Siphon enchantment obtainable in survival by allowing enchanted books to generate with it                                              |
| `generateRefill`        | `true`, `false`                     | `true`            | Make the Refill enchantment obtainable in survival by allowing enchanted books to generate with it                                              |
| `generateVacuum`        | `true`, `false`                     | `false`           | Make the Vacuum enchantment obtainable in survival by allowing enchanted books to generate with it                                              |
| `generateVoid`          | `true`, `false`                     | `false`           | Make the Void enchantment obtainable in survival by allowing enchanted books to generate with it                                                |
| `generateAugment`       | `true`, `false`                     | `false`           | Make the Augment enchantment obtainable in survival by allowing enchanted books to generate with it                                             |
| `nestedContainers`      | any integer between `0` and `32767` | `255`             | Search containers recursively up to the specified number of levels deep (e.g, search through Shulker Boxes in an Ender Chest)                   |
| `strongerSiphon`        | `true`, `false`                     | `false`           | Allow the Siphon enchantment to fill empty slots. The same behavior can be enabled for the Vacuum enchantment with `weakerVacuum`               |
| `weakerVacuum`          | `true`, `false`                     | `false`           | Require the same item to already be present in the container. The same behavior can be enabled for the Siphon enchantment with `strongerSiphon` |
| `maxAugmentLevel`       | any integer between `1` and `10`    | `3`               | The maximum level for the Augment enchantment. For the best experience execute `/reload` after changing this value                              |

## For Mod Developers

If your Mod adds a new container that should support these enchantments, you
must simply add the container to the `enchantedshulkers:portable_container` item
tag. To do that, create the file `portable_container.json` inside the
`src/main/resources/data/enchantedshulkers/tags/items/` folder of your mod with
following contents:

```json
{
  "replace": false,
  "values": ["your_mod_namespace:your_container_item"]
}
```

If your container does not extend the vanilla Shulker Box class, you might have
to manually implement the
[`EnchantableBlockEntity`](https://github.com/RubixDev/EnchantedShulkers/blob/1.19/src/main/java/de/rubixdev/enchantedshulkers/EnchantableBlockEntity.java)
interface and support rendering the enchantment glint and display name
colorizer. For more information have a look at
[`ShulkerBoxBlockEntityRendererMixin.java`](https://github.com/RubixDev/EnchantedShulkers/blob/1.19/src/main/java/de/rubixdev/enchantedshulkers/mixin/client/ShulkerBoxBlockEntityRendererMixin.java)
and
[`ShulkerBoxBlockEntityMixin.java`](https://github.com/RubixDev/EnchantedShulkers/blob/1.19/src/main/java/de/rubixdev/enchantedshulkers/mixin/ShulkerBoxBlockEntityMixin.java).

For any further questions or issues, please open an issue on
[GitHub](https://github.com/RubixDev/EnchantedShulkers/issues/new).
