# EnchantedShulkers
Adds the "Siphon" and "Refill" enchantments to Shulker Boxes

[![Requires Fabric API](https://raw.githubusercontent.com/RubixDev/Rug/1.17/fabric-api.png)](https://modrinth.com/mod/fabric-api)

## Inspiration
I was inspired to create this mod after watching [Xisumavoid's video about inventory improvements](https://www.youtube.com/watch?v=zJtJ90Vl03M&t=491s).
In that video a Forge mod called [Shulker Enchantments](https://www.curseforge.com/minecraft/mc-mods/shulker-enchantments) was presented.
This mod now tries to provide similar functionality for Fabric, as the only other Fabric mod with these enchantments that I could find (i.e. [BetterShulkers](https://www.curseforge.com/minecraft/mc-mods/fabric-bettershulkers)) only supports Minecraft 1.16.

## Features
This mod adds the "Siphon" and "Refill" enchantments to the game. They can be obtained just like Mending and other treasure enchantments.
The enchantments can be added to Shulker Boxes <!-- TODO: and Ender Chests --> on an anvil.

### Siphon
With the Siphon enchantment, any item that you pick up will be immediately stored in an enchanted container as long as that container already has a non-full stack of that exact item inside.
The container must be inside your inventory.

### Refill
With the Refill enchantment, any item or item stack you use up will be refilled from an enchanted container as long as the used stack was held in one of the hands (see [configuration](#configuration)).
Again, the container must be inside your inventory.

## Server/Client
This mod does actually work as a server-only mod, but it is not recommended.
Players without the mod on their client cannot see these enchantments on enchanted books and enchanted containers.

## Configuration
*coming soon...*
