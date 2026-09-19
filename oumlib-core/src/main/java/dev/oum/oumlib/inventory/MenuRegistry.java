package dev.oum.oumlib.inventory;

import dev.oum.oumlib.event.Events;
import dev.oum.oumlib.event.ListenerHandle;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class MenuRegistry {

    private static final Set<Menu> activeMenus = ConcurrentHashMap.newKeySet();

    private static ListenerHandle clickHandle;
    private static ListenerHandle dragHandle;
    private static ListenerHandle closeHandle;
    private static ListenerHandle dropHandle;
    private static ListenerHandle quitHandle;

    private MenuRegistry() {
    }

    public static void register(Menu menu) {
        activeMenus.add(menu);
        if (menu instanceof GuiContainer) {
            ensureContainerListeners();
        }
    }

    public static void unregister(Menu menu) {
        activeMenus.remove(menu);
        checkListenersCleanup();
    }

    private static synchronized void ensureContainerListeners() {
        if (clickHandle != null && clickHandle.isActive()) {
            return;
        }

        try {
            clickHandle = Events.listen(InventoryClickEvent.class, event -> {
                if (!(event.getWhoClicked() instanceof Player player)) return;
                for (Menu menu : activeMenus) {
                    if (menu instanceof GuiContainer container && container.getCurrentViewers().containsKey(player.getUniqueId())) {
                        container.handleClick(event, player);
                        break;
                    }
                }
            });

            dragHandle = Events.listen(InventoryDragEvent.class, event -> {
                if (!(event.getWhoClicked() instanceof Player player)) return;
                for (Menu menu : activeMenus) {
                    if (menu instanceof GuiContainer container && container.getCurrentViewers().containsKey(player.getUniqueId())) {
                        container.handleDrag(event, player);
                        break;
                    }
                }
            });

            closeHandle = Events.listen(InventoryCloseEvent.class, event -> {
                if (!(event.getPlayer() instanceof Player player)) return;
                for (Menu menu : activeMenus) {
                    if (menu instanceof GuiContainer container && container.getCurrentViewers().containsKey(player.getUniqueId())) {
                        container.handleClose(event, player);
                        break;
                    }
                }
            });

            dropHandle = Events.listen(PlayerDropItemEvent.class, event -> {
                Player player = event.getPlayer();
                for (Menu menu : activeMenus) {
                    if (menu instanceof GuiContainer container && container.preventsItemDropping()
                            && container.getCurrentViewers().containsKey(player.getUniqueId())) {
                        event.setCancelled(true);
                        break;
                    }
                }
            });

            quitHandle = Events.listen(PlayerQuitEvent.class, event -> {
                Player player = event.getPlayer();
                for (Menu menu : activeMenus) {
                    if (menu instanceof GuiContainer container && container.getCurrentViewers().containsKey(player.getUniqueId())) {
                        container.close(player);
                    }
                }
            });
        } catch (Throwable ignored) {
            // EventBus may not be initialized in non-Paper environments or unit tests
        }
    }

    private static synchronized void checkListenersCleanup() {
        boolean hasContainers = activeMenus.stream().anyMatch(m -> m instanceof GuiContainer);
        if (!hasContainers) {
            unregisterListeners();
        }
    }

    private static synchronized void unregisterListeners() {
        if (clickHandle != null) {
            clickHandle.unregister();
            clickHandle = null;
        }
        if (dragHandle != null) {
            dragHandle.unregister();
            dragHandle = null;
        }
        if (closeHandle != null) {
            closeHandle.unregister();
            closeHandle = null;
        }
        if (dropHandle != null) {
            dropHandle.unregister();
            dropHandle = null;
        }
        if (quitHandle != null) {
            quitHandle.unregister();
            quitHandle = null;
        }
    }

    public static void shutdown() {
        for (Menu menu : activeMenus) {
            menu.closeAll();
        }
        activeMenus.clear();
        unregisterListeners();
    }
}
