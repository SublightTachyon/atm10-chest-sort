package com.turone.atm10chestsort.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;
import java.util.List;

final class ContainerActions {
    private static final Comparator<ItemStack> STACK_ORDER = Comparator
            .comparing((ItemStack stack) -> stack.getHoverName().getString(), String.CASE_INSENSITIVE_ORDER)
            .thenComparing(stack -> BuiltInRegistries.ITEM.getKey(stack.getItem()).toString())
            .thenComparing(stack -> stack.getComponents().toString());

    private ContainerActions() {}

    static void sortStorage(AbstractContainerMenu menu) {
        runIfSupported(menu, layout -> sort(menu, layout.storageSlots()));
    }

    static void sortInventory(AbstractContainerMenu menu) {
        runIfSupported(menu, layout -> sort(menu, layout.playerSlots()));
    }

    static void moveAllToStorage(AbstractContainerMenu menu) {
        runIfSupported(menu, layout -> quickMoveAll(menu, layout.playerSlots()));
    }

    static void moveAllToInventory(AbstractContainerMenu menu) {
        runIfSupported(menu, layout -> quickMoveAll(menu, layout.storageSlots()));
    }

    static void mergeToStorage(AbstractContainerMenu menu) {
        runIfSupported(menu, layout -> quickMoveMatching(menu, layout.playerSlots(), layout.storageSlots()));
    }

    static void mergeToInventory(AbstractContainerMenu menu) {
        runIfSupported(menu, layout -> quickMoveMatching(menu, layout.storageSlots(), layout.playerSlots()));
    }

    private static void runIfSupported(AbstractContainerMenu menu, java.util.function.Consumer<ContainerLayout.Layout> action) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.gameMode == null) {
            return;
        }
        if (!menu.getCarried().isEmpty()) {
            minecraft.player.displayClientMessage(Component.translatable("atm10_chest_sort.message.cursor_not_empty"), true);
            return;
        }

        ContainerLayout.Layout layout = ContainerLayout.find(menu);
        if (!layout.supported()) {
            minecraft.player.displayClientMessage(Component.translatable("atm10_chest_sort.message.unsupported_layout"), true);
            return;
        }
        action.accept(layout);
    }

    private static void quickMoveAll(AbstractContainerMenu menu, List<Integer> sourceSlots) {
        for (int slotId : sourceSlots) {
            if (!menu.getSlot(slotId).getItem().isEmpty()) {
                click(menu, slotId, ClickType.QUICK_MOVE);
            }
        }
    }

    private static void quickMoveMatching(AbstractContainerMenu menu, List<Integer> sourceSlots, List<Integer> destinationSlots) {
        for (int sourceId : sourceSlots) {
            ItemStack source = menu.getSlot(sourceId).getItem();
            if (source.isEmpty()) {
                continue;
            }
            boolean destinationContainsType = destinationSlots.stream()
                    .map(menu::getSlot)
                    .map(Slot::getItem)
                    .anyMatch(destination -> !destination.isEmpty() && ItemStack.isSameItemSameComponents(source, destination));
            if (destinationContainsType) {
                click(menu, sourceId, ClickType.QUICK_MOVE);
            }
        }
    }

    private static void sort(AbstractContainerMenu menu, List<Integer> slotIds) {
        consolidate(menu, slotIds);

        for (int targetPosition = 0; targetPosition < slotIds.size(); targetPosition++) {
            int bestPosition = targetPosition;
            for (int candidatePosition = targetPosition + 1; candidatePosition < slotIds.size(); candidatePosition++) {
                ItemStack candidate = menu.getSlot(slotIds.get(candidatePosition)).getItem();
                ItemStack best = menu.getSlot(slotIds.get(bestPosition)).getItem();
                if (compareWithEmptyLast(candidate, best) < 0) {
                    bestPosition = candidatePosition;
                }
            }
            if (bestPosition != targetPosition) {
                swap(menu, slotIds.get(targetPosition), slotIds.get(bestPosition));
            }
        }
    }

    private static void consolidate(AbstractContainerMenu menu, List<Integer> slotIds) {
        for (int targetPosition = 0; targetPosition < slotIds.size(); targetPosition++) {
            int targetId = slotIds.get(targetPosition);
            ItemStack target = menu.getSlot(targetId).getItem();
            if (target.isEmpty()) {
                continue;
            }

            for (int sourcePosition = targetPosition + 1; sourcePosition < slotIds.size(); sourcePosition++) {
                int sourceId = slotIds.get(sourcePosition);
                ItemStack source = menu.getSlot(sourceId).getItem();
                target = menu.getSlot(targetId).getItem();
                if (source.isEmpty() || !ItemStack.isSameItemSameComponents(target, source)) {
                    continue;
                }
                int maximum = Math.min(target.getMaxStackSize(), menu.getSlot(targetId).getMaxStackSize(target));
                if (target.getCount() >= maximum) {
                    break;
                }

                click(menu, sourceId, ClickType.PICKUP);
                click(menu, targetId, ClickType.PICKUP);
                if (!menu.getCarried().isEmpty()) {
                    click(menu, sourceId, ClickType.PICKUP);
                }
            }
        }
    }

    private static int compareWithEmptyLast(ItemStack left, ItemStack right) {
        if (left.isEmpty()) {
            return right.isEmpty() ? 0 : 1;
        }
        if (right.isEmpty()) {
            return -1;
        }
        return STACK_ORDER.compare(left, right);
    }

    private static void swap(AbstractContainerMenu menu, int firstId, int secondId) {
        boolean firstWasEmpty = menu.getSlot(firstId).getItem().isEmpty();
        if (!firstWasEmpty) {
            click(menu, firstId, ClickType.PICKUP);
        }
        click(menu, secondId, ClickType.PICKUP);
        click(menu, firstId, ClickType.PICKUP);
        if (!menu.getCarried().isEmpty()) {
            click(menu, secondId, ClickType.PICKUP);
        }
    }

    private static void click(AbstractContainerMenu menu, int slotId, ClickType type) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryMouseClick(menu.containerId, slotId, 0, type, minecraft.player);
        }
    }
}
