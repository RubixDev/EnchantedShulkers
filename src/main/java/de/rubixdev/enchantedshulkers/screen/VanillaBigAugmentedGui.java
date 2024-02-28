package de.rubixdev.enchantedshulkers.screen;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.ShulkerBoxSlot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

import java.util.List;

//#if MC < 12004
//$$ import eu.pb4.sgui.virtual.inventory.VirtualScreenHandler;
//$$ import net.minecraft.entity.player.PlayerEntity;
//$$ import net.minecraft.entity.player.PlayerInventory;
//$$ import net.minecraft.screen.ScreenHandler;
//#endif

public class VanillaBigAugmentedGui extends SimpleGui {
    private int scroll = 0;
    private final boolean maximized;
    private final int rows;
    private final Inventory shulkerInventory;

    // TODO: custom player heads
    private static final String DISABLED_ARROW_UP = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2Y0NmFiYWQ5MjRiMjIzNzJiYzk2NmE2ZDUxN2QyZjFiOGI1N2ZkZDI2MmI0ZTA0ZjQ4MzUyZTY4M2ZmZjkyIn19fQ==";
    private static final String DISABLED_ARROW_DOWN = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmU5YWU3YTRiZTY1ZmNiYWVlNjUxODEzODlhMmY3ZDQ3ZTJlMzI2ZGI1OWVhM2ViNzg5YTkyYzg1ZWE0NiJ9fX0=";
    private static final String GREEN_ARROW_UP = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWRhMDI3NDc3MTk3YzZmZDdhZDMzMDE0NTQ2ZGUzOTJiNGE1MWM2MzRlYTY4YzhiN2JjYzAxMzFjODNlM2YifX19";
    private static final String GREEN_ARROW_DOWN = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmY3NDE2Y2U5ZTgyNmU0ODk5YjI4NGJiMGFiOTQ4NDNhOGY3NTg2ZTUyYjcxZmMzMTI1ZTAyODZmOTI2YSJ9fX0=";
    private static final String YELLOW_ARROW_UP = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2U0ZjJmOTY5OGMzZjE4NmZlNDRjYzYzZDJmM2M0ZjlhMjQxMjIzYWNmMDU4MTc3NWQ5Y2VjZDcwNzUifX19";
    private static final String YELLOW_ARROW_DOWN = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmIyOWVjZWVmM2RkYjE0ZjkwNmRiZDRmYTQxZDYzZjNkN2Q0NTM3ODcxY2VlNDMxNWM1OWU3NmViYzVmODUifX19";
    private static final String ORANGE_ARROW_UP = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODgxYmRjZDU2MmZiNjFlZjY2YjhmZTk3NWE4NTRlZDE5ZjY1N2QxN2RhZGM2NDdkYTc5ODg4NTY2YThiMiJ9fX0=";
    private static final String ORANGE_ARROW_DOWN = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzQxOTQxZTdlN2U5MTRhMTE1YzM0MmQ2ZDM4YTIyOTMxZTEzOGIzZGExZWViNGU5OTg1NzFlOTBmODcxNTE3In19fQ==";
    private static final String RED_ARROW_UP = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmQ5Mjg3NjE2MzQzZDgzM2U5ZTczMTcxNTljYWEyY2IzZTU5NzQ1MTEzOTYyYzEzNzkwNTJjZTQ3ODg4NGZhIn19fQ==";
    private static final String RED_ARROW_DOWN = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTM4NTJiZjYxNmYzMWVkNjdjMzdkZTRiMGJhYTJjNWY4ZDhmY2E4MmU3MmRiY2FmY2JhNjY5NTZhODFjNCJ9fX0=";
    private static final String MAXIMIZE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjQ2MTUwOGI2NGVjYmQ4YjM3YmRhZTUzYTZiOTQwMDQ0MDViMWM5ZDBmODE1ZDBjNjUxNmQwMDQ5YWQ2NzM5YSJ9fX0=";
    private static final String MINIMIZE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzJlYjZmZWNlMTg1ZjE0OTAxODVmMzk4MGU4MGM2ODk5YmZkNDBjZTdiZTJhOTQ2NWIwMTAxNTFkODgyZmYifX19";

    VanillaBigAugmentedGui(ServerPlayerEntity player, Inventory shulkerInventory, int rows) {
        this(player, shulkerInventory, rows, false, true);
    }

    private VanillaBigAugmentedGui(ServerPlayerEntity player, Inventory shulkerInventory, int rows, boolean maximized, boolean onInventoryOpen) {
        super(ScreenHandlerType.GENERIC_9X6, player, maximized);
        this.rows = rows;
        this.shulkerInventory = shulkerInventory;
        this.maximized = maximized;
        updateSlots();
        if (onInventoryOpen) {
            shulkerInventory.onOpen(player);
        }
    }

    private int actionsRow() {
        return 9 * (maximized ? 9 : 5);
    }

    private int maxScroll() {
        return rows - (maximized ? 9 : 5);
    }

    @Override
    public boolean getLockPlayerInventory() {
        return lockPlayerInventory;
    }

    private void updateSlots() {
        for (int i = 0; i < actionsRow(); i++) {
            if (i < shulkerInventory.size()) {
                setSlotRedirect(i, new ShulkerBoxSlot(shulkerInventory, i + 9 * scroll, 0, 0));
            } else {
                setSlot(i, GuiElementBuilder.from(Items.GRAY_STAINED_GLASS_PANE.getDefaultStack()).setName(Text.empty()));
            }
        }

        addScrollButton(0, Text.literal("Scroll up one"), -1, scroll <= 0, GREEN_ARROW_UP, DISABLED_ARROW_UP);
        addScrollButton(1, Text.literal("Scroll down one"), 1, scroll >= maxScroll(), GREEN_ARROW_DOWN, DISABLED_ARROW_DOWN);
        addScrollButton(2, Text.literal("Scroll up two"), -2, scroll <= 0, YELLOW_ARROW_UP, DISABLED_ARROW_UP);
        addScrollButton(3, Text.literal("Scroll down two"), 2, scroll >= maxScroll(), YELLOW_ARROW_DOWN, DISABLED_ARROW_DOWN);
        addScrollButton(4, Text.literal("Scroll up three"), -3, scroll <= 0, ORANGE_ARROW_UP, DISABLED_ARROW_UP);
        addScrollButton(5, Text.literal("Scroll down three"), 3, scroll >= maxScroll(), ORANGE_ARROW_DOWN, DISABLED_ARROW_DOWN);
        addScrollButton(6, Text.literal("Scroll to top"), -rows, scroll <= 0, RED_ARROW_UP, DISABLED_ARROW_UP);
        addScrollButton(7, Text.literal("Scroll to bottom"), rows, scroll >= maxScroll(), RED_ARROW_DOWN, DISABLED_ARROW_DOWN);

        setSlot(actionsRow() + 8, GuiElementBuilder.from(Items.PLAYER_HEAD.getDefaultStack())
                .setSkullOwner(maximized ? MINIMIZE : MAXIMIZE)
                .setName(Text.literal(maximized ? "Minimize" : "Maximize").formatted(Formatting.GREEN))
                .setLore(maximized ? List.of() : List.of(Text.literal("Items cannot be interacted with when maximized").formatted(Formatting.GRAY)))
                .setCallback(() -> {
                    click();
                    new VanillaBigAugmentedGui(player, shulkerInventory, rows, !maximized, false).open();
                }));
    }

    private void addScrollButton(int slot, MutableText name, int scrollAmount, boolean disabled, String headTexture, String disabledTexture) {
        GuiElementBuilder button = GuiElementBuilder.from(Items.PLAYER_HEAD.getDefaultStack());
        if (disabled) {
            setSlot(actionsRow() + slot, button.setName(name.formatted(Formatting.DARK_GRAY)).setSkullOwner(disabledTexture));
        } else {
            setSlot(actionsRow() + slot, button.setName(name.formatted(Formatting.GREEN))
                    .setSkullOwner(headTexture)
                    .setCallback(() -> scroll(scrollAmount)));
        }
    }

    private void scroll(int amount) {
        scroll = MathHelper.clamp(scroll + amount, 0, maxScroll());
        click();
        updateSlots();
    }

    private void click() {
        player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.MASTER, 0.2f, 1);
    }

    @Override
    public void onClose() {
        super.onClose();
        shulkerInventory.onClose(player);
    }

    @Override
    public boolean onAnyClick(int index, ClickType type, SlotActionType action) {
        return !maximized;
    }

    //#if MC < 12004
    //$$ public ScreenHandler openAsScreenHandler(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
    //$$     if (this.player.isDisconnected() || player != this.player || this.isOpen()) {
    //$$         return null;
    //$$     } else {
    //$$         this.beforeOpen();
    //$$         this.onOpen();
    //$$         this.screenHandler = new VirtualScreenHandler(this.getType(), syncId, this, player);
    //$$         return this.screenHandler;
    //$$     }
    //$$ }
    //#endif
}
