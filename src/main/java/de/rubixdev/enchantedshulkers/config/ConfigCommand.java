package de.rubixdev.enchantedshulkers.config;

import static net.minecraft.command.CommandSource.suggestMatching;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import de.rubixdev.enchantedshulkers.Mod;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.lang.reflect.Field;

public class ConfigCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> literalArgumentBuilder =
                literal(Mod.MOD_ID).requires(source -> source.hasPermissionLevel(2));
        literalArgumentBuilder.executes(ConfigCommand::listAllOptions)
                .then(argument("option", StringArgumentType.word())
                        .suggests((context, builder) -> suggestMatching(WorldConfig.getOptions().sorted(), builder))
                        .executes(ConfigCommand::displayOptionMenu)
                        .then(argument("value", StringArgumentType.greedyString())
                                .suggests((context, builder) -> suggestMatching(suggestions(context), builder))
                                .executes(ConfigCommand::setOption)));
        dispatcher.register(literalArgumentBuilder);
    }

    private static void sendFeedback(ServerCommandSource source, Text text) {
        source.sendFeedback(
                //#if MC >= 12000
                () ->
                //#endif
                text,
                false
        );
    }

    private static String optionFromContext(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String optionName = StringArgumentType.getString(context, "option");
        try {
            WorldConfig.getOption(optionName);
        } catch (Exception e) {
            throw new SimpleCommandExceptionType(Text.translatable("commands." + Mod.MOD_ID + ".unknown_option",  optionName).setStyle(Style.EMPTY.withColor(Formatting.RED).withBold(true))).create();
        }
        return optionName;
    }

    private static String[] suggestions(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return suggestions(optionFromContext(context));
    }

    private static String[] suggestions(String option) {
        Field field = WorldConfig.getField(option);
        if (field.getType() == boolean.class || Boolean.class.isAssignableFrom(field.getType())) return new String[]{"true", "false"};
        IntOption intOption = field.getAnnotation(IntOption.class);
        if (intOption != null) return intOption.suggestions();
        return new String[]{String.valueOf(WorldConfig.getOptionDefault(option))};
    }

    private static int listAllOptions(CommandContext<ServerCommandSource> context) {
        sendFeedback(context.getSource(), Text.empty());
        sendFeedback(
                context.getSource(),
                Text.translatable("commands." + Mod.MOD_ID + ".all_options_title")
                        .setStyle(Style.EMPTY.withColor(Formatting.WHITE).withBold(true))
        );

        WorldConfig.getOptions().forEach(option -> {
            MutableText text = Text.empty();
            text.append(Text.literal("- " + option)
                    .setStyle(Style.EMPTY
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + Mod.MOD_ID + " " + option))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable(Mod.MOD_ID + ".options." + option + ".desc").setStyle(Style.EMPTY.withColor(Formatting.YELLOW))))));
            for (String suggestion : suggestions(option)) {
                text.append(makeSetOptionButton(option, suggestion, true));
            }
            sendFeedback(
                    context.getSource(),
                    text
            );
        });

        return 1;
    }

    private static Text makeSetOptionButton(String option, String value, boolean brackets) {
        MutableText text = Text.literal((brackets ? "[" : "") + value + (brackets ? "]" : ""));
        Style style = Style.EMPTY;
        boolean optionIsDefault = WorldConfig.getOption(option).equals(WorldConfig.getOptionDefault(option));
        boolean valueIsOptionDefault = String.valueOf(WorldConfig.getOptionDefault(option)).equalsIgnoreCase(value);
        boolean valueIsOptionCurrent = String.valueOf(WorldConfig.getOption(option)).equalsIgnoreCase(value);
        if (optionIsDefault) {
            // all gray if option has its default value
            style = style.withColor(Formatting.GRAY);
        } else if (valueIsOptionDefault) {
            // else the default value green
            style = style.withColor(Formatting.GREEN);
        } else {
            // and all others yellow
            style = style.withColor(Formatting.YELLOW);
        }
        if (valueIsOptionCurrent) {
            // mark the selected option with an underline
            style = style.withUnderline(true);
            if (valueIsOptionDefault) {
                // and if it's also the default, make it bold
                style = style.withBold(true);
            }
        }

        // if this button is for the current value, don't make it clickable
        if (!valueIsOptionCurrent) {
            style = style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + Mod.MOD_ID + " " + option + " " + value));
            style = style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable("commands." + Mod.MOD_ID + ".switch_to", value + (valueIsOptionDefault ? " (default)" : "")).setStyle(Style.EMPTY.withColor(Formatting.GRAY))));
        }

        return Text.literal(" ").append(text.setStyle(style));
    }

    private static int displayOptionMenu(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String option = optionFromContext(context);
        sendFeedback(context.getSource(), Text.empty());
        sendFeedback(context.getSource(), Text.literal(option).setStyle(Style.EMPTY.withBold(true).withColor(Formatting.WHITE).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + Mod.MOD_ID + " " + option)).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable("commands." + Mod.MOD_ID + ".refresh").setStyle(Style.EMPTY.withColor(Formatting.GRAY))))));
        sendFeedback(context.getSource(), Text.translatable(Mod.MOD_ID + ".options." + option + ".desc").setStyle(Style.EMPTY.withColor(Formatting.GRAY)));
        sendFeedback(context.getSource(), Text.translatable("commands." + Mod.MOD_ID + ".current_value").append(Text.literal(WorldConfig.getOption(option) + (WorldConfig.getOption(option).equals(WorldConfig.getOptionDefault(option)) ? " (default)" : " (modified)")).setStyle(Style.EMPTY.withColor(WorldConfig.getBooleanValue(option) ? Formatting.GREEN : Formatting.DARK_RED).withBold(true))));

        MutableText optionsText = Text.literal("[").setStyle(Style.EMPTY.withColor(Formatting.YELLOW));
        for (String suggestion : suggestions(option)) {
            optionsText.append(makeSetOptionButton(option, suggestion, false));
        }
        optionsText.append(Text.literal(" ]"));
        sendFeedback(context.getSource(), Text.translatable("commands." + Mod.MOD_ID + ".options").append(optionsText));

        return 1;
    }

    private static int setOption(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String option = optionFromContext(context);
        String value = StringArgumentType.getString(context, "value");

        try {
            WorldConfig.setOption(option, value);
            sendFeedback(context.getSource(), Text.translatable("commands." + Mod.MOD_ID + ".set", option, String.valueOf(value)));
        } catch (InvalidOptionValueException e) {
            context.getSource().sendError(e.getText());
        }

        return 1;
    }
}
