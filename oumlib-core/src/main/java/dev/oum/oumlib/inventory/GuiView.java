package dev.oum.oumlib.inventory;

import org.bukkit.inventory.Inventory;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public final class GuiView {

    private final UUID playerUuid;
    private int currentScreenIndex;
    private int currentPage;
    private Inventory inventory;

    public GuiView(@NonNull UUID playerUuid, int currentScreenIndex, int currentPage) {
        this.playerUuid = playerUuid;
        this.currentScreenIndex = currentScreenIndex;
        this.currentPage = currentPage;
    }

    public @NonNull UUID getPlayerUuid() {
        return playerUuid;
    }

    public int getCurrentScreenIndex() {
        return currentScreenIndex;
    }

    public void setCurrentScreenIndex(int currentScreenIndex) {
        this.currentScreenIndex = currentScreenIndex;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public @Nullable Inventory getInventory() {
        return inventory;
    }

    public void setInventory(@Nullable Inventory inventory) {
        this.inventory = inventory;
    }
}
