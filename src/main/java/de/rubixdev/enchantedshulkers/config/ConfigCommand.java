package de.rubixdev.enchantedshulkers.config;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import de.rubixdev.enchantedshulkers.Mod;
import net.minecraft.server.command.ServerCommandSource;
//#if MC >= 11900
import net.minecraft.text.Text;
//#else
//$$ import net.minecraft.text.TranslatableText;
//#endif

public class ConfigCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> literalArgumentBuilder =
                literal(Mod.MOD_ID).requires(source -> source.hasPermissionLevel(2));
        WorldConfig.getOptions().forEach(option -> literalArgumentBuilder.then(literal(option)
                .executes(context -> getOption(context, option))
                .then(argument("value", BoolArgumentType.bool()).executes(context -> setOption(context, option)))));
        dispatcher.register(literalArgumentBuilder);
    }

    private static void sendFeedback(ServerCommandSource source, String trKey, Object... args) {
        // TODO: send literal english if the receiver doesn't have this mod
        source.sendFeedback(
                //#if MC >= 12000
                () ->
                //#endif
                //#if MC >= 11900
                Text.translatable(
                //#else
                //$$ new TranslatableText(
                //#endif
                        trKey, args
                ),
                false
        );
    }

    private static int getOption(CommandContext<ServerCommandSource> context, String option) {
        boolean value;
        try {
            value = WorldConfig.getOption(option);
        } catch (NoSuchFieldException e) {
            // unable to fail, because `option` was provided by `getOptions()`
            throw new RuntimeException(e);
        }
        sendFeedback(context.getSource(), "commands.enchantedshulkers.get", option, value);
        return 1;
    }

    private static int setOption(CommandContext<ServerCommandSource> context, String option) {
        boolean value = BoolArgumentType.getBool(context, "value");

        try {
            WorldConfig.setOption(option, value);
        } catch (NoSuchFieldException e) {
            // unable to fail, because `option` was provided by `getOptions()`
            throw new RuntimeException(e);
        }
        sendFeedback(context.getSource(), "commands.enchantedshulkers.set", option, value);

        return 1;
    }
}
