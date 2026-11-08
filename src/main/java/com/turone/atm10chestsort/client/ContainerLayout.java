package com.turone.atm10chestsort.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Identifies the player inventory and the largest regular storage grid in a menu. */
final class ContainerLayout {
    private ContainerLayout() {}

    static Layout find(AbstractContainerMenu menu) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return Layout.EMPTY;
        }

        Inventory inventory = minecraft.player.getInventory();
        List<Integer> player = new ArrayList<>();
        List<Integer> mainInventory = new ArrayList<>();
        List<Integer> hotbar = new ArrayList<>();
        List<IndexedSlot> external = new ArrayList<>();

        for (int menuIndex = 0; menuIndex < menu.slots.size(); menuIndex++) {
            Slot slot = menu.slots.get(menuIndex);
            if (slot.container == inventory && slot.getContainerSlot() >= 0 && slot.getContainerSlot() < 36) {
                player.add(menuIndex);
                if (slot.getContainerSlot() >= 9) {
                    mainInventory.add(menuIndex);
                } else {
                    hotbar.add(menuIndex);
                }
            } else if (slot.container != inventory && slot.isActive()) {
                external.add(new IndexedSlot(menuIndex, slot.x, slot.y));
            }
        }

        if (player.size() < 36) {
            return Layout.EMPTY;
        }

        List<Integer> storage = largestRegularGrid(external);
        if (storage.size() < 9) {
            return Layout.EMPTY;
        }
        return new Layout(List.copyOf(storage), List.copyOf(player), List.copyOf(mainInventory), List.copyOf(hotbar));
    }

    private static List<Integer> largestRegularGrid(List<IndexedSlot> slots) {
        Map<Integer, List<IndexedSlot>> rows = new HashMap<>();
        for (IndexedSlot slot : slots) {
            rows.computeIfAbsent(slot.y(), ignored -> new ArrayList<>()).add(slot);
        }

        List<List<IndexedSlot>> regularRows = rows.values().stream()
                .filter(ContainerLayout::isRegularRow)
                .sorted(Comparator.comparingInt(row -> row.getFirst().y()))
                .toList();

        List<Integer> best = List.of();
        for (int start = 0; start < regularRows.size(); start++) {
            List<IndexedSlot> first = regularRows.get(start);
            List<Integer> candidate = new ArrayList<>();
            int previousY = Integer.MIN_VALUE;

            for (int rowIndex = start; rowIndex < regularRows.size(); rowIndex++) {
                List<IndexedSlot> row = regularRows.get(rowIndex);
                if (!sameColumns(first, row) || (previousY != Integer.MIN_VALUE && row.getFirst().y() - previousY != 18)) {
                    break;
                }
                row.stream()
                        .sorted(Comparator.comparingInt(IndexedSlot::x))
                        .map(IndexedSlot::menuIndex)
                        .forEach(candidate::add);
                previousY = row.getFirst().y();
            }

            if (candidate.size() > best.size()) {
                best = List.copyOf(candidate);
            }
        }
        return best;
    }

    private static boolean isRegularRow(List<IndexedSlot> row) {
        // ATM10 chest and backpack storage uses chest-width rows. Requiring nine
        // columns keeps crafting tables and most machine input grids untouched.
        if (row.size() < 9) {
            return false;
        }
        List<IndexedSlot> sorted = row.stream().sorted(Comparator.comparingInt(IndexedSlot::x)).toList();
        for (int index = 1; index < sorted.size(); index++) {
            if (sorted.get(index).x() - sorted.get(index - 1).x() != 18) {
                return false;
            }
        }
        return true;
    }

    private static boolean sameColumns(List<IndexedSlot> first, List<IndexedSlot> other) {
        if (first.size() != other.size()) {
            return false;
        }
        List<Integer> firstX = first.stream().map(IndexedSlot::x).sorted().toList();
        List<Integer> otherX = other.stream().map(IndexedSlot::x).sorted().toList();
        return firstX.equals(otherX);
    }

    record Layout(List<Integer> storageSlots, List<Integer> playerSlots, List<Integer> mainInventorySlots,
                  List<Integer> hotbarSlots) {
        private static final Layout EMPTY = new Layout(List.of(), List.of(), List.of(), List.of());

        boolean supported() {
            return !storageSlots.isEmpty()
                    && playerSlots.size() == 36
                    && mainInventorySlots.size() == 27
                    && hotbarSlots.size() == 9;
        }
    }

    private record IndexedSlot(int menuIndex, int x, int y) {}
}
