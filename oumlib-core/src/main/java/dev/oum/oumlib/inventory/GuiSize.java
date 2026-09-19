package dev.oum.oumlib.inventory;

import org.jspecify.annotations.NonNull;

public enum GuiSize {
    ROWS_ONE(1, 9),
    ROWS_TWO(2, 18),
    ROWS_THREE(3, 27),
    ROWS_FOUR(4, 36),
    ROWS_FIVE(5, 45),
    ROWS_SIX(6, 54);

    private final int rows;
    private final int slots;

    GuiSize(int rows, int slots) {
        this.rows = rows;
        this.slots = slots;
    }

    public int rows() {
        return rows;
    }

    public int slots() {
        return slots;
    }

    public int getRows() {
        return rows;
    }

    public int getSlots() {
        return slots;
    }

    public static @NonNull GuiSize fromRows(int rows) {
        return switch (rows) {
            case 1 -> ROWS_ONE;
            case 2 -> ROWS_TWO;
            case 3 -> ROWS_THREE;
            case 4 -> ROWS_FOUR;
            case 5 -> ROWS_FIVE;
            case 6 -> ROWS_SIX;
            default -> throw new IllegalArgumentException("Rows must be between 1 and 6, got: " + rows);
        };
    }

    public static @NonNull GuiSize fromSlots(int slots) {
        return switch (slots) {
            case 9 -> ROWS_ONE;
            case 18 -> ROWS_TWO;
            case 27 -> ROWS_THREE;
            case 36 -> ROWS_FOUR;
            case 45 -> ROWS_FIVE;
            case 54 -> ROWS_SIX;
            default -> throw new IllegalArgumentException("Slots must be a multiple of 9 between 9 and 54, got: " + slots);
        };
    }
}
