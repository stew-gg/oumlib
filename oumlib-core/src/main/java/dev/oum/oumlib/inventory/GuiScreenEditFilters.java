package dev.oum.oumlib.inventory;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public final class GuiScreenEditFilters {

    private final Set<Material> whitelist = new HashSet<>();
    private final Set<Material> blacklist = new HashSet<>();
    private int maxItems = -1;
    private Predicate<ItemStack> customFilter;

    public GuiScreenEditFilters() {
    }

    public static @NonNull GuiScreenEditFilters create() {
        return new GuiScreenEditFilters();
    }

    public @NonNull GuiScreenEditFilters whitelist(@NonNull Material @NonNull ... materials) {
        for (Material m : materials) {
            if (m != null) this.whitelist.add(m);
        }
        return this;
    }

    public @NonNull GuiScreenEditFilters whitelist(@NonNull Collection<Material> materials) {
        this.whitelist.addAll(materials);
        return this;
    }

    public @NonNull GuiScreenEditFilters blacklist(@NonNull Material @NonNull ... materials) {
        for (Material m : materials) {
            if (m != null) this.blacklist.add(m);
        }
        return this;
    }

    public @NonNull GuiScreenEditFilters blacklist(@NonNull Collection<Material> materials) {
        this.blacklist.addAll(materials);
        return this;
    }

    public @NonNull GuiScreenEditFilters maxItems(int maxItems) {
        this.maxItems = maxItems;
        return this;
    }

    public @NonNull GuiScreenEditFilters customFilter(@Nullable Predicate<ItemStack> filter) {
        this.customFilter = filter;
        return this;
    }

    public boolean canInsert(@Nullable ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return true;
        }

        if (!whitelist.isEmpty() && !whitelist.contains(item.getType())) {
            return false;
        }

        if (blacklist.contains(item.getType())) {
            return false;
        }

        if (customFilter != null && !customFilter.test(item)) {
            return false;
        }

        return true;
    }

    public int getMaxItems() {
        return maxItems;
    }

    public @NonNull Set<Material> getWhitelist() {
        return new HashSet<>(whitelist);
    }

    public @NonNull Set<Material> getBlacklist() {
        return new HashSet<>(blacklist);
    }
}
