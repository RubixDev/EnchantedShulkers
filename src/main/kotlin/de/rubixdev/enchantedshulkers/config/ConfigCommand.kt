package de.rubixdev.enchantedshulkers.config

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.builder.RequiredArgumentBuilder.argument
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import de.rubixdev.enchantedshulkers.Mod.MOD_ID
import net.minecraft.command.CommandSource.suggestMatching
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.ClickEvent
import net.minecraft.text.HoverEvent
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting

object ConfigCommand {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        val builder = literal<ServerCommandSource>(MOD_ID).requires { it.hasPermissionLevel(2) }
        builder.executes(::listAllOptions).then(
            argument<ServerCommandSource?, String?>(
                "option",
                StringArgumentType.word(),
            ).suggests { _, b -> suggestMatching(WorldConfig.options.sorted(), b) }.executes(::displayOptionMenu)
                .then(
                    argument<ServerCommandSource?, String?>(
                        "value",
                        StringArgumentType.greedyString(),
                    ).suggests { ctx, b -> suggestMatching(WorldConfig.suggestions(optionFromContext(ctx)), b) }
                        .executes(::setOption),
                ),
        )
        dispatcher.register(builder)
    }

    private fun sendFeedback(source: ServerCommandSource, text: Text) {
        source.sendFeedback(
            //#if MC >= 12001
            { text },
            //#else
            //$$ text,
            //#endif
            false,
        )
    }

    private fun optionFromContext(context: CommandContext<ServerCommandSource>): String {
        val optionName = StringArgumentType.getString(context, "option")
        try {
            WorldConfig.getOption(optionName)
        } catch (e: NoSuchElementException) {
            throw SimpleCommandExceptionType(
                Text.translatable("commands.$MOD_ID.unknown_option", optionName).setStyle(
                    Style.EMPTY.withColor(Formatting.RED).withBold(true),
                ),
            ).create()
        }
        return optionName
    }

    private fun listAllOptions(context: CommandContext<ServerCommandSource>): Int {
        sendFeedback(context.source, Text.empty())
        sendFeedback(
            context.source,
            Text.translatable("commands.$MOD_ID.all_options_title")
                .setStyle(Style.EMPTY.withColor(Formatting.WHITE).withBold(true)),
        )

        WorldConfig.options.forEach { option ->
            val text = Text.empty()
            text.append(
                Text.literal("- $option").setStyle(
                    Style.EMPTY.withClickEvent(
                        ClickEvent(
                            ClickEvent.Action.RUN_COMMAND,
                            "/$MOD_ID $option",
                        ),
                    ).withHoverEvent(
                        HoverEvent(
                            HoverEvent.Action.SHOW_TEXT,
                            Text.translatable("$MOD_ID.options.$option.desc")
                                .setStyle(Style.EMPTY.withColor(Formatting.YELLOW)),
                        ),
                    ),
                ),
            )
            for (suggestion in WorldConfig.suggestions(option)) {
                text.append(makeSetOptionButton(option, suggestion, true))
            }
            sendFeedback(context.source, text)
        }
        return 1
    }

    private fun makeSetOptionButton(option: String, value: String, brackets: Boolean): Text {
        val text = Text.literal((if (brackets) "[" else "") + value + (if (brackets) "]" else ""))
        val optionIsDefault = WorldConfig.getOption(option) == WorldConfig.getOptionDefault(option)
        val valueIsOptionDefault = WorldConfig.getOptionDefault(option).toString().equals(value, ignoreCase = true)
        val valueIsOptionCurrent = WorldConfig.getOption(option).toString().equals(value, ignoreCase = true)

        var style = when {
            // all gray if option has its default value
            optionIsDefault -> Style.EMPTY.withColor(Formatting.GRAY)
            // else the default value green
            valueIsOptionDefault -> Style.EMPTY.withColor(Formatting.GREEN)
            // and all others yellow
            else -> Style.EMPTY.withColor(Formatting.YELLOW)
        }

        if (valueIsOptionCurrent) {
            // mark the selected option with an underline
            style = style.withUnderline(true)
            if (valueIsOptionDefault) {
                // and if it's also the default, make it bold
                style = style.withBold(true)
            }
        }

        // make the button clickable unless it's for the current value
        if (!valueIsOptionCurrent) {
            style = style.withClickEvent(ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/$MOD_ID $option $value"))
            style = style.withHoverEvent(
                HoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    Text.translatable(
                        "commands.$MOD_ID.switch_to",
                        value + (if (valueIsOptionDefault) " (default)" else ""),
                    ).setStyle(Style.EMPTY.withColor(Formatting.GRAY)),
                ),
            )
        }

        return Text.literal(" ").append(text.setStyle(style))
    }

    private fun displayOptionMenu(context: CommandContext<ServerCommandSource>): Int {
        val option = optionFromContext(context)
        sendFeedback(context.source, Text.empty())
        sendFeedback(
            context.source,
            Text.literal(option).setStyle(
                Style.EMPTY.withBold(true).withColor(Formatting.WHITE)
                    .withClickEvent(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/$MOD_ID $option")).withHoverEvent(
                        HoverEvent(
                            HoverEvent.Action.SHOW_TEXT,
                            Text.translatable("commands.$MOD_ID.refresh").setStyle(Style.EMPTY.withColor(Formatting.GRAY)),
                        ),
                    ),
            ),
        )
        sendFeedback(
            context.source,
            Text.translatable("$MOD_ID.options.$option.desc").setStyle(Style.EMPTY.withColor(Formatting.GRAY)),
        )
        sendFeedback(
            context.source,
            Text.translatable("commands.$MOD_ID.current_value").append(
                Text.literal(
                    WorldConfig.getOption(option)
                        .toString() + (
                        if (WorldConfig.getOption(option) == WorldConfig.getOptionDefault(
                                option,
                            )
                        ) {
                            " (default)"
                        } else {
                            " (modified)"
                        }
                        ),
                ).setStyle(
                    Style.EMPTY.withColor(if (WorldConfig.getBooleanValue(option)) Formatting.GREEN else Formatting.DARK_RED)
                        .withBold(true),
                ),
            ),
        )

        val optionsText = Text.literal("[").setStyle(Style.EMPTY.withColor(Formatting.YELLOW))
        for (suggestion in WorldConfig.suggestions(option)) {
            optionsText.append(makeSetOptionButton(option, suggestion, brackets = false))
        }
        optionsText.append(Text.literal(" ]"))
        sendFeedback(context.source, Text.translatable("commands.$MOD_ID.options").append(optionsText))

        return 1
    }

    private fun setOption(context: CommandContext<ServerCommandSource>): Int {
        val option = optionFromContext(context)
        val value = StringArgumentType.getString(context, "value")

        try {
            WorldConfig.setOption(option, value)
            sendFeedback(context.source, Text.translatable("commands.$MOD_ID.set", option, value))
        } catch (e: InvalidOptionValueException) {
            context.source.sendError(e.text)
        }

        return 1
    }
}
