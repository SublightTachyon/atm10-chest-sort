package com.turone.atm10chestsort.client;

import com.turone.atm10chestsort.Atm10ChestSort;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

import java.util.function.Consumer;

@EventBusSubscriber(modid = Atm10ChestSort.MOD_ID, value = Dist.CLIENT)
public final class ClientEvents {
    private static final int BUTTON_WIDTH = 104;
    private static final int BUTTON_HEIGHT = 20;
    private static final int GAP = 2;
    private static final int GROUP_GAP = 6;

    private ClientEvents() {}

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen)
                || screen instanceof InventoryScreen
                || screen instanceof CreativeModeInventoryScreen) {
            return;
        }

        AbstractContainerMenu menu = screen.getMenu();
        if (!ContainerLayout.find(menu).supported()) {
            return;
        }

        int x = choosePanelX(screen);
        int y = Math.max(6, screen.getGuiTop());

        event.addListener(button("sort_chest", x, y, ignored -> ContainerActions.sortStorage(menu)));
        y += BUTTON_HEIGHT + GAP;
        event.addListener(button("merge_chest", x, y, ignored -> ContainerActions.mergeToStorage(menu)));
        y += BUTTON_HEIGHT + GAP;
        event.addListener(button("all_chest", x, y, ignored -> ContainerActions.moveAllToStorage(menu)));
        y += BUTTON_HEIGHT + GROUP_GAP;
        event.addListener(button("sort_inventory", x, y, ignored -> ContainerActions.sortInventory(menu)));
        y += BUTTON_HEIGHT + GAP;
        event.addListener(button("merge_inventory", x, y, ignored -> ContainerActions.mergeToInventory(menu)));
        y += BUTTON_HEIGHT + GAP;
        event.addListener(button("all_inventory", x, y, ignored -> ContainerActions.moveAllToInventory(menu)));
    }

    private static int choosePanelX(AbstractContainerScreen<?> screen) {
        int left = screen.getGuiLeft() - BUTTON_WIDTH - 4;
        if (left >= 4) {
            return left;
        }

        int right = screen.getGuiLeft() + screen.getXSize() + 4;
        if (right + BUTTON_WIDTH <= screen.width - 4) {
            return right;
        }

        return Math.max(4, screen.width - BUTTON_WIDTH - 4);
    }

    private static Button button(String name, int x, int y, Consumer<Button> action) {
        Button button = Button.builder(
                        Component.translatable("atm10_chest_sort.button." + name),
                        action::accept)
                .bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();
        button.setTooltip(Tooltip.create(Component.translatable("atm10_chest_sort.tooltip." + name)));
        return button;
    }
}
