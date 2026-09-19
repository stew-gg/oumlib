package dev.oum.oumlib.inventory;

import dev.oum.oumlib.math.FastMath;
import org.jspecify.annotations.NonNull;

import java.util.*;

public final class GuiScreenSection {

    private final Set<Integer> slots = new LinkedHashSet<>();

    public GuiScreenSection() {
    }

    public GuiScreenSection(int startSlot, int endSlot) {
        addSlotRange(startSlot, endSlot);
    }

    public GuiScreenSection(int @NonNull ... slots) {
        addSlots(slots);
    }

    public GuiScreenSection(@NonNull Collection<Integer> slots) {
        this.slots.addAll(slots);
    }

    public static @NonNull GuiScreenSection of(int startSlot, int endSlot) {
        return new GuiScreenSection(startSlot, endSlot);
    }

    public static @NonNull GuiScreenSection of(int @NonNull ... slots) {
        return new GuiScreenSection(slots);
    }

    public static @NonNull GuiScreenSection of(@NonNull Collection<Integer> slots) {
        return new GuiScreenSection(slots);
    }

    public static @NonNull GuiScreenSection rectangle(int startRow, int startCol, int endRow, int endCol) {
        GuiScreenSection section = new GuiScreenSection();
        section.addRectangle(startRow, startCol, endRow, endCol);
        return section;
    }

    public @NonNull GuiScreenSection addSlot(int slot) {
        this.slots.add(slot);
        return this;
    }

    public @NonNull GuiScreenSection addSlots(int @NonNull ... slots) {
        for (int slot : slots) {
            this.slots.add(slot);
        }
        return this;
    }

    public @NonNull GuiScreenSection addSlots(@NonNull Collection<Integer> slots) {
        this.slots.addAll(slots);
        return this;
    }

    public @NonNull GuiScreenSection addSlotRange(int startSlot, int endSlot) {
        int min = FastMath.min(startSlot, endSlot);
        int max = FastMath.max(startSlot, endSlot);
        for (int i = min; i <= max; i++) {
            this.slots.add(i);
        }
        return this;
    }

    public @NonNull GuiScreenSection addRectangle(int startRow, int startCol, int endRow, int endCol) {
        int minRow = FastMath.min(startRow, endRow);
        int maxRow = FastMath.max(startRow, endRow);
        int minCol = FastMath.min(startCol, endCol);
        int maxCol = FastMath.max(startCol, endCol);

        for (int r = minRow; r <= maxRow; r++) {
            for (int c = minCol; c <= maxCol; c++) {
                this.slots.add(r * 9 + c);
            }
        }
        return this;
    }

    public boolean contains(int slot) {
        return slots.contains(slot);
    }

    public @NonNull List<Integer> getSlots() {
        return new ArrayList<>(slots);
    }

    public int size() {
        return slots.size();
    }

    public boolean isEmpty() {
        return slots.isEmpty();
    }

    public int[] toArray() {
        return slots.stream().mapToInt(Integer::intValue).toArray();
    }
}
