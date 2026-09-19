package dev.oum.oumlib.inventory;

import dev.oum.oumlib.math.FastMath;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;

import java.util.Locale;

public final class GuiUtil {

    public static final String PREVIOUS_PAGE_NUMBER_PLACEHOLDER = "<prev_page>";
    public static final String NEXT_PAGE_NUMBER_PLACEHOLDER = "<next_page>";
    public static final String PAGE_NUMBER_PLACEHOLDER = "<page>";
    public static final String MAX_PAGE_NUMBER_PLACEHOLDER = "<pages>";

    private GuiUtil() {
    }

    public static void fillScreen(@NonNull GuiScreen screen, @NonNull ItemStack item) {
        for (int i = 0; i < screen.getSize().slots(); i++) {
            screen.addItemStackAt(i, item);
        }
    }

    public static void fillScreen(@NonNull GuiScreen screen, @NonNull GuiButton button) {
        for (int i = 0; i < screen.getSize().slots(); i++) {
            screen.addButtonAt(i, button);
        }
    }

    public static void fillBorders(@NonNull GuiScreen screen, @NonNull ItemStack item) {
        int rows = screen.getSize().rows();

        for (int col = 0; col < 9; col++) {
            screen.addItemStackAt(col, item);
            if (rows > 1) {
                screen.addItemStackAt((rows - 1) * 9 + col, item);
            }
        }

        for (int row = 1; row < rows - 1; row++) {
            screen.addItemStackAt(row * 9, item);
            screen.addItemStackAt(row * 9 + 8, item);
        }
    }

    public static void fillBorders(@NonNull GuiScreen screen, @NonNull GuiButton button) {
        int rows = screen.getSize().rows();

        for (int col = 0; col < 9; col++) {
            screen.addButtonAt(col, button);
            if (rows > 1) {
                screen.addButtonAt((rows - 1) * 9 + col, button);
            }
        }

        for (int row = 1; row < rows - 1; row++) {
            screen.addButtonAt(row * 9, button);
            screen.addButtonAt(row * 9 + 8, button);
        }
    }

    public static void fillRectangle(@NonNull GuiScreen screen, int startRow, int startCol, int endRow, int endCol, @NonNull ItemStack item) {
        int minRow = FastMath.min(startRow, endRow);
        int maxRow = FastMath.max(startRow, endRow);
        int minCol = FastMath.min(startCol, endCol);
        int maxCol = FastMath.max(startCol, endCol);

        for (int r = minRow; r <= maxRow; r++) {
            for (int c = minCol; c <= maxCol; c++) {
                screen.addItemStackAt(r * 9 + c, item);
            }
        }
    }

    public static void fillRectangle(@NonNull GuiScreen screen, int startRow, int startCol, int endRow, int endCol, @NonNull GuiButton button) {
        int minRow = FastMath.min(startRow, endRow);
        int maxRow = FastMath.max(startRow, endRow);
        int minCol = FastMath.min(startCol, endCol);
        int maxCol = FastMath.max(startCol, endCol);

        for (int r = minRow; r <= maxRow; r++) {
            for (int c = minCol; c <= maxCol; c++) {
                screen.addButtonAt(r * 9 + c, button);
            }
        }
    }

    public static void fillRow(@NonNull GuiScreen screen, int row, @NonNull ItemStack item) {
        if (row < 0 || row >= screen.getSize().rows()) return;
        for (int c = 0; c < 9; c++) {
            screen.addItemStackAt(row * 9 + c, item);
        }
    }

    public static void fillRow(@NonNull GuiScreen screen, int row, @NonNull GuiButton button) {
        if (row < 0 || row >= screen.getSize().rows()) return;
        for (int c = 0; c < 9; c++) {
            screen.addButtonAt(row * 9 + c, button);
        }
    }

    public static void fillColumn(@NonNull GuiScreen screen, int col, @NonNull ItemStack item) {
        if (col < 0 || col >= 9) return;
        for (int r = 0; r < screen.getSize().rows(); r++) {
            screen.addItemStackAt(r * 9 + col, item);
        }
    }

    public static void fillColumn(@NonNull GuiScreen screen, int col, @NonNull GuiButton button) {
        if (col < 0 || col >= 9) return;
        for (int r = 0; r < screen.getSize().rows(); r++) {
            screen.addButtonAt(r * 9 + col, button);
        }
    }

    public static int slot(int row, int col) {
        return row * 9 + col;
    }

    public static int slot1(int row, int col) {
        return (row - 1) * 9 + (col - 1);
    }

    public static int row(int slot) {
        return slot / 9;
    }

    public static int col(int slot) {
        return slot % 9;
    }

    public static @NonNull String formatName(@NonNull String name) {
        String[] parts = name.toLowerCase(Locale.ROOT).split("_");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].isEmpty()) continue;
            sb.append(Character.toUpperCase(parts[i].charAt(0)))
              .append(parts[i].substring(1));
            if (i < parts.length - 1) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }
}
