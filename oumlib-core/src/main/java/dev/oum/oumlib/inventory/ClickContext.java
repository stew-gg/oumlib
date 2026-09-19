package dev.oum.oumlib.inventory;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public record ClickContext(@NonNull Player player, @NonNull ClickAction action, int slot, @NonNull Menu menu) {

    public @NonNull ClickType bukkitClickType() {
        if (action instanceof ClickAction.LeftClick) return ClickType.LEFT;
        if (action instanceof ClickAction.RightClick) return ClickType.RIGHT;
        if (action instanceof ClickAction.ShiftLeftClick) return ClickType.SHIFT_LEFT;
        if (action instanceof ClickAction.ShiftRightClick) return ClickType.SHIFT_RIGHT;
        if (action instanceof ClickAction.MiddleClick) return ClickType.MIDDLE;
        if (action instanceof ClickAction.Other other) return other.type();
        return ClickType.UNKNOWN;
    }

    public @Nullable ItemStack item() {
        try {
            var view = player.getOpenInventory();
            return view.getItem(slot);
        } catch (Throwable ignored) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public <T> @Nullable T state(@NonNull String key) {
        if (menu instanceof ChestMenu chestMenu) {
            return (T) chestMenu.getState(player, key);
        }
        return null;
    }

    public void updateState(@NonNull String key, @Nullable Object value) {
        if (menu instanceof ChestMenu chestMenu) {
            chestMenu.updateState(player, key, value);
        }
    }
}