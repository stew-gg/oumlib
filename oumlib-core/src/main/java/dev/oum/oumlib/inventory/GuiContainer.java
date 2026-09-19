package dev.oum.oumlib.inventory;

import dev.oum.oumlib.math.FastMath;
import dev.oum.oumlib.scheduler.Scheduler;
import dev.oum.oumlib.scheduler.TaskHandle;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class GuiContainer implements Menu {

    private final Map<Integer, GuiScreen> screens = new LinkedHashMap<>();
    private final Map<UUID, GuiView> currentViewers = new ConcurrentHashMap<>();

    private int tickRate = -1;
    private int currentTick = 0;
    private boolean persistent = false;
    private boolean preventItemDropping = false;
    private Consumer<Player> onCloseCallback;

    private TaskHandle tickTask;

    public GuiContainer() {
    }

    @Contract(" -> new")
    @CheckReturnValue
    public static @NonNull GuiContainer create() {
        return new GuiContainer();
    }

    public @NonNull GuiContainer setTickRate(int tickRateInTicks) {
        this.tickRate = tickRateInTicks;
        return this;
    }

    public int getTickRate() {
        return tickRate;
    }

    public @NonNull GuiContainer setPersistent(boolean persistent) {
        this.persistent = persistent;
        return this;
    }

    public boolean isPersistent() {
        return persistent;
    }

    public @NonNull GuiContainer preventItemDropping(boolean prevent) {
        this.preventItemDropping = prevent;
        return this;
    }

    public boolean preventsItemDropping() {
        return preventItemDropping;
    }

    public @NonNull GuiContainer onClose(@Nullable Consumer<Player> callback) {
        this.onCloseCallback = callback;
        return this;
    }

    public @NonNull GuiContainer addScreen(@NonNull GuiScreen screen) {
        this.screens.put(this.screens.size(), screen);
        return this;
    }

    public @NonNull GuiScreen createScreen(@NonNull GuiSize size) {
        GuiScreen screen = new GuiScreen(this, size);
        addScreen(screen);
        return screen;
    }

    public @NonNull GuiScreen createScreen(int rows) {
        return createScreen(GuiSize.fromRows(rows));
    }

    public @Nullable GuiScreen getScreen(int index) {
        return screens.get(index);
    }

    public @NonNull Map<Integer, GuiScreen> getScreens() {
        return Collections.unmodifiableMap(screens);
    }

    @Override
    public void open(@NonNull Player player) {
        openFor(player, 0, 1);
    }

    public void openFor(@NonNull Player player, int screenIndex) {
        openFor(player, screenIndex, 1);
    }

    public void openFor(@NonNull Player player, int screenIndex, int page) {
        GuiScreen screen = screens.get(screenIndex);
        if (screen == null) {
            throw new IndexOutOfBoundsException("Screen index " + screenIndex + " does not exist in this container.");
        }

        Scheduler.runFor(player, () -> {
            MenuRegistry.register(this);
            startTickTaskIfNeeded();

            Inventory inv = screen.createInventory(player, page);
            GuiView view = new GuiView(player.getUniqueId(), screenIndex, page);
            view.setInventory(inv);
            currentViewers.put(player.getUniqueId(), view);

            player.openInventory(inv);
        });
    }

    @Override
    public void close(@NonNull Player player) {
        Scheduler.runFor(player, () -> {
            GuiView view = currentViewers.remove(player.getUniqueId());
            if (view != null) {
                finalizeEditableSection(player, view);
            }
            player.closeInventory();
            if (onCloseCallback != null) {
                onCloseCallback.accept(player);
            }
            checkCleanup();
        });
    }

    @Override
    public void closeAll() {
        for (UUID uuid : new ArrayList<>(currentViewers.keySet())) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                close(player);
            }
        }
        currentViewers.clear();
        checkCleanup();
    }

    public void transitionForwards(@NonNull Player player) {
        GuiView view = currentViewers.get(player.getUniqueId());
        if (view == null) return;
        int nextScreen = view.getCurrentScreenIndex() + 1;
        if (screens.containsKey(nextScreen)) {
            openFor(player, nextScreen, 1);
        }
    }

    public void transitionBackwards(@NonNull Player player) {
        GuiView view = currentViewers.get(player.getUniqueId());
        if (view == null) return;
        int prevScreen = view.getCurrentScreenIndex() - 1;
        if (screens.containsKey(prevScreen)) {
            openFor(player, prevScreen, 1);
        }
    }

    public void changePage(@NonNull Player player, int newPage) {
        GuiView view = currentViewers.get(player.getUniqueId());
        if (view == null) return;

        GuiScreen screen = screens.get(view.getCurrentScreenIndex());
        if (screen == null) return;

        int maxPages = screen.getMaximumPageNumber();
        int clampedPage = FastMath.clamp(newPage, 1, maxPages);
        view.setCurrentPage(clampedPage);

        Scheduler.runFor(player, () -> {
            Inventory inv = view.getInventory();
            if (inv != null) {
                screen.updateInventory(player, inv, clampedPage);
                player.updateInventory();
            }
        });
    }

    public void refresh(@NonNull Player player) {
        GuiView view = currentViewers.get(player.getUniqueId());
        if (view == null) return;

        GuiScreen screen = screens.get(view.getCurrentScreenIndex());
        if (screen == null) return;

        Scheduler.runFor(player, () -> {
            Inventory inv = view.getInventory();
            if (inv != null) {
                screen.updateInventory(player, inv, view.getCurrentPage());
                player.updateInventory();
            }
        });
    }

    public void refreshAll() {
        for (UUID uuid : currentViewers.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                refresh(player);
            }
        }
    }

    public void rebuildScreen(@NonNull GuiScreen screen) {
        for (Map.Entry<UUID, GuiView> entry : currentViewers.entrySet()) {
            GuiView view = entry.getValue();
            if (screens.get(view.getCurrentScreenIndex()) == screen) {
                Player player = Bukkit.getPlayer(entry.getKey());
                if (player != null && player.isOnline()) {
                    openFor(player, view.getCurrentScreenIndex(), view.getCurrentPage());
                }
            }
        }
    }

    public void tick() {
        if (currentViewers.isEmpty()) return;
        currentTick++;

        for (Map.Entry<UUID, GuiView> entry : currentViewers.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null || !player.isOnline()) continue;

            GuiView view = entry.getValue();
            GuiScreen screen = screens.get(view.getCurrentScreenIndex());
            if (screen == null) continue;

            Inventory inv = view.getInventory();
            if (inv == null) continue;

            Scheduler.runFor(player, () -> {
                Runnable tickHandler = screen.getTickHandler();
                if (tickHandler != null) {
                    tickHandler.run();
                }
                screen.updateInventory(player, inv, view.getCurrentPage());
            });
        }
    }

    private synchronized void startTickTaskIfNeeded() {
        if (tickRate > 0 && (tickTask == null || !tickTask.isRunning())) {
            tickTask = Scheduler.runRepeating(Duration.ofMillis(tickRate * 50L), Duration.ofMillis(tickRate * 50L), this::tick);
        }
    }

    private synchronized void stopTickTask() {
        if (tickTask != null) {
            tickTask.cancel();
            tickTask = null;
        }
    }

    private void checkCleanup() {
        if (currentViewers.isEmpty()) {
            stopTickTask();
            if (!persistent) {
                MenuRegistry.unregister(this);
            }
        }
    }

    public void handleClick(@NonNull InventoryClickEvent event, @NonNull Player player) {
        GuiView view = currentViewers.get(player.getUniqueId());
        if (view == null) return;

        GuiScreen screen = screens.get(view.getCurrentScreenIndex());
        if (screen == null) return;

        Inventory topInv = view.getInventory();
        if (topInv == null || !event.getView().getTopInventory().equals(topInv)) return;

        int rawSlot = event.getRawSlot();
        boolean isTopInventory = rawSlot < topInv.getSize();

        GuiScreenSection editable = screen.getEditableSection();
        boolean isEditableSlot = isTopInventory && editable != null && editable.contains(rawSlot);

        if (isTopInventory) {
            if (isEditableSlot) {
                GuiScreenEditFilters filters = screen.getEditFilters();
                if (filters != null) {
                    ItemStack cursor = event.getCursor();
                    if (cursor != null && !cursor.getType().isAir() && !filters.canInsert(cursor)) {
                        event.setCancelled(true);
                        return;
                    }
                }
                return;
            }

            event.setCancelled(true);

            GuiButton button = screen.getButtonAt(rawSlot, view.getCurrentPage());
            if (button != null) {
                if (button.getClickSound() != null) {
                    player.playSound(button.getClickSound());
                }

                ClickContext context = new ClickContext(player, ClickAction.from(event.getClick()), rawSlot, this);
                GuiAction action = button.handleClick(context);

                switch (action) {
                    case NOTHING -> {}
                    case REFRESH -> refresh(player);
                    case CLOSE -> close(player);
                    case PAGE_FIRST -> changePage(player, 1);
                    case PAGE_BACKWARDS -> changePage(player, view.getCurrentPage() - 1);
                    case PAGE_FORWARDS -> changePage(player, view.getCurrentPage() + 1);
                    case PAGE_LAST -> changePage(player, screen.getMaximumPageNumber());
                    case TRANSITION_FORWARDS -> transitionForwards(player);
                    case TRANSITION_BACKWARDS -> transitionBackwards(player);
                }
            }
        } else {
            if (event.isShiftClick()) {
                if (editable == null || editable.isEmpty()) {
                    event.setCancelled(true);
                    return;
                }

                ItemStack clickedItem = event.getCurrentItem();
                GuiScreenEditFilters filters = screen.getEditFilters();
                if (filters != null && clickedItem != null && !filters.canInsert(clickedItem)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    public void handleDrag(@NonNull InventoryDragEvent event, @NonNull Player player) {
        GuiView view = currentViewers.get(player.getUniqueId());
        if (view == null) return;

        GuiScreen screen = screens.get(view.getCurrentScreenIndex());
        if (screen == null) return;

        Inventory topInv = view.getInventory();
        if (topInv == null || !event.getView().getTopInventory().equals(topInv)) return;

        GuiScreenSection editable = screen.getEditableSection();
        GuiScreenEditFilters filters = screen.getEditFilters();

        for (int slot : event.getRawSlots()) {
            if (slot < topInv.getSize()) {
                if (editable == null || !editable.contains(slot)) {
                    event.setCancelled(true);
                    return;
                }
                if (filters != null && !filters.canInsert(event.getOldCursor())) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    public void handleClose(@NonNull InventoryCloseEvent event, @NonNull Player player) {
        GuiView view = currentViewers.remove(player.getUniqueId());
        if (view != null) {
            finalizeEditableSection(player, view);
            if (onCloseCallback != null) {
                onCloseCallback.accept(player);
            }
            checkCleanup();
        }
    }

    private void finalizeEditableSection(Player player, GuiView view) {
        GuiScreen screen = screens.get(view.getCurrentScreenIndex());
        if (screen == null) return;

        GuiScreenSection editable = screen.getEditableSection();
        var callback = screen.getEditFinalizationCallback();
        Inventory inv = view.getInventory();

        if (editable != null && callback != null && inv != null) {
            List<ItemStack> items = new ArrayList<>();
            for (int slot : editable.getSlots()) {
                if (slot < inv.getSize()) {
                    ItemStack item = inv.getItem(slot);
                    if (item != null && !item.getType().isAir()) {
                        items.add(item.clone());
                    }
                }
            }
            callback.accept(player, items);
        }
    }

    public @NonNull Map<UUID, GuiView> getCurrentViewers() {
        return Collections.unmodifiableMap(currentViewers);
    }

    public boolean hasViewers() {
        return !currentViewers.isEmpty();
    }

    public int getCurrentTick() {
        return currentTick;
    }

    public void setCurrentTick(int currentTick) {
        this.currentTick = currentTick;
    }
}
