package dev.oum.oumlib.inventory;

import dev.oum.oumlib.math.FastMath;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public final class Layout {

    private final String[] pattern;
    private final Map<Character, Function<Player, ItemStack>> bindings = new HashMap<>();
    private final Map<Character, List<Integer>> slotMap = new HashMap<>();

    public Layout(String @NonNull ... rows) {
        if (rows.length > 6) throw new IllegalArgumentException("Max 6 rows.");
        this.pattern = rows;
        buildSlotMap();
    }

    public Layout bind(char key, @Nullable ItemStack item) {
        bindings.put(key, player -> item);
        return this;
    }

    public Layout bind(char key, @NonNull GuiButton button) {
        bindings.put(key, player -> button.render(1, 1, false));
        return this;
    }

    public Layout bind(char key, @NonNull Supplier<@Nullable ItemStack> supplier) {
        bindings.put(key, player -> supplier.get());
        return this;
    }

    public Layout bind(char key, @NonNull Function<@NonNull Player, @Nullable ItemStack> function) {
        bindings.put(key, function);
        return this;
    }

    public @NonNull String[] pattern() {
        return pattern;
    }

    public @NonNull Set<Character> characters() {
        return slotMap.keySet();
    }

    public List<Integer> slotsFor(char key) {
        return slotMap.getOrDefault(key, List.of());
    }

    public void apply(@NonNull Inventory inventory, @NonNull Player player) {
        bindings.forEach((key, function) ->
                slotsFor(key).forEach(slot -> inventory.setItem(slot, function.apply(player)))
        );
    }

    /**
     * @deprecated Use {@link #apply(Inventory, Player)} to pass the viewer context.
     */
    @Deprecated(since = "1.0.4", forRemoval = false)
    @ApiStatus.Obsolete
    public void apply(@NonNull Inventory inventory) {
        bindings.forEach((key, function) ->
                slotsFor(key).forEach(slot -> inventory.setItem(slot, function.apply(null)))
        );
    }

    public static @NonNull Layout of(String @NonNull ... rows) {
        return new Layout(rows);
    }

    public static int[] rectangle(int startRow, int startCol, int endRow, int endCol) {
        return rectangleList(startRow, startCol, endRow, endCol).stream().mapToInt(Integer::intValue).toArray();
    }

    public static @NonNull List<Integer> rectangleList(int startRow, int startCol, int endRow, int endCol) {
        List<Integer> slots = new ArrayList<>();
        int minRow = FastMath.min(startRow, endRow);
        int maxRow = FastMath.max(startRow, endRow);
        int minCol = FastMath.min(startCol, endCol);
        int maxCol = FastMath.max(startCol, endCol);

        for (int r = minRow; r <= maxRow; r++) {
            for (int c = minCol; c <= maxCol; c++) {
                slots.add(r * 9 + c);
            }
        }
        return slots;
    }

    public static int[] border(int rows) {
        return borderList(rows).stream().mapToInt(Integer::intValue).toArray();
    }

    public static @NonNull List<Integer> borderList(int rows) {
        List<Integer> slots = new ArrayList<>();
        if (rows <= 0) return slots;

        for (int col = 0; col < 9; col++) {
            slots.add(col); // top row
            if (rows > 1) {
                slots.add((rows - 1) * 9 + col); // bottom row
            }
        }

        for (int row = 1; row < rows - 1; row++) {
            slots.add(row * 9); // left col
            slots.add(row * 9 + 8); // right col
        }

        Collections.sort(slots);
        return slots;
    }

    private void buildSlotMap() {
        for (int row = 0; row < pattern.length; row++) {
            String line = pattern[row];
            for (int col = 0; col < FastMath.min(line.length(), 9); col++) {
                char c = line.charAt(col);
                int slot = row * 9 + col;
                slotMap.computeIfAbsent(c, k -> new ArrayList<>()).add(slot);
            }
        }
    }
}