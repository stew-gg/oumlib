package dev.oum.oumlib.inventory;

import dev.oum.oumlib.math.FastMath;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class GuiScreen {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private final GuiContainer parentContainer;
    private GuiSize size;
    private Component title;

    private final Map<Integer, Object> permanentSlots = new LinkedHashMap<>();

    private GuiScreenSection paginatedSection;
    private int totalPaginatedItems;
    private PageContentsRequester pageContentsRequester;
    private final Map<Integer, GuiPageContentsResult> pageCache = new ConcurrentHashMap<>();

    private GuiScreenSection editableSection;
    private final List<ItemStack> editableInitialItems = new ArrayList<>();
    private BiConsumer<Player, List<ItemStack>> editFinalizationCallback;
    private GuiScreenEditFilters editFilters;

    private final Map<Integer, Consumer<ItemStack>> slotListeners = new HashMap<>();
    private final Map<Integer, ItemStack> lastKnownSlotContents = new HashMap<>();
    private Runnable tickHandler;

    public GuiScreen(@NonNull GuiContainer parentContainer, @NonNull GuiSize size) {
        this.parentContainer = parentContainer;
        this.size = size;
        this.title = Component.text("Inventory");
    }

    public GuiScreen(@NonNull GuiContainer parentContainer, int rows) {
        this(parentContainer, GuiSize.fromRows(rows));
    }

    public @NonNull GuiContainer getParentContainer() {
        return parentContainer;
    }

    public @NonNull GuiSize getSize() {
        return size;
    }

    public @NonNull GuiScreen setSize(@NonNull GuiSize size) {
        this.size = size;
        return this;
    }

    public @NonNull GuiScreen setSize(int rows) {
        return setSize(GuiSize.fromRows(rows));
    }

    public @NonNull Component getTitle() {
        return title;
    }

    public @NonNull GuiScreen setTitle(@NonNull Component title) {
        this.title = title;
        return this;
    }

    public @NonNull GuiScreen setTitle(@NonNull String miniMessageTitle) {
        this.title = MM.deserialize(miniMessageTitle);
        return this;
    }

    public @NonNull GuiScreen addButtonAt(int slot, @NonNull GuiButton button) {
        this.permanentSlots.put(slot, button);
        return this;
    }

    public @NonNull GuiScreen addButton(@NonNull GuiButton button) {
        int nextSlot = getNextAvailableSlot();
        if (nextSlot != -1) {
            addButtonAt(nextSlot, button);
        }
        return this;
    }

    public @NonNull GuiScreen addItemStackAt(int slot, @NonNull ItemStack item) {
        this.permanentSlots.put(slot, item.clone());
        return this;
    }

    public @NonNull GuiScreen addItemStack(@NonNull ItemStack item) {
        int nextSlot = getNextAvailableSlot();
        if (nextSlot != -1) {
            addItemStackAt(nextSlot, item);
        }
        return this;
    }

    public @Nullable Object getSlot(int slot) {
        return permanentSlots.get(slot);
    }

    public @NonNull Map<Integer, Object> getPermanentSlots() {
        return Collections.unmodifiableMap(permanentSlots);
    }

    private int getNextAvailableSlot() {
        for (int i = 0; i < size.slots(); i++) {
            if (!permanentSlots.containsKey(i) && (paginatedSection == null || !paginatedSection.contains(i))
                    && (editableSection == null || !editableSection.contains(i))) {
                return i;
            }
        }
        return -1;
    }

    public @NonNull GuiScreen applyLayout(@NonNull Layout layout, @NonNull Map<Character, Object> bindings) {
        for (Map.Entry<Character, Object> entry : bindings.entrySet()) {
            char key = entry.getKey();
            Object itemOrButton = entry.getValue();
            for (int slot : layout.slotsFor(key)) {
                if (itemOrButton instanceof GuiButton button) {
                    addButtonAt(slot, button);
                } else if (itemOrButton instanceof ItemStack item) {
                    addItemStackAt(slot, item);
                }
            }
        }
        return this;
    }

    public @NonNull GuiScreen setPaginatedSection(@NonNull GuiScreenSection section, int totalItems, @NonNull PageContentsRequester requester) {
        this.paginatedSection = section;
        this.totalPaginatedItems = totalItems;
        this.pageContentsRequester = requester;
        this.pageCache.clear();
        return this;
    }

    public @NonNull GuiScreen setPaginatedSection(int startSlot, int endSlot, int totalItems, @NonNull PageContentsRequester requester) {
        return setPaginatedSection(GuiScreenSection.of(startSlot, endSlot), totalItems, requester);
    }

    public @NonNull GuiScreen setPaginatedItems(@NonNull GuiScreenSection section, @NonNull List<ItemStack> items) {
        return setPaginatedSection(section, items.size(), (page, start, end) -> {
            GuiPageContentsResult result = GuiPageContentsResult.of();
            for (int i = start; i <= FastMath.min(end, items.size() - 1); i++) {
                result.addPageContent(items.get(i));
            }
            return result;
        });
    }

    public @NonNull GuiScreen setPaginatedButtons(@NonNull GuiScreenSection section, @NonNull List<GuiButton> buttons) {
        return setPaginatedSection(section, buttons.size(), (page, start, end) -> {
            GuiPageContentsResult result = GuiPageContentsResult.of();
            for (int i = start; i <= FastMath.min(end, buttons.size() - 1); i++) {
                result.addPageContent(buttons.get(i));
            }
            return result;
        });
    }

    public @Nullable GuiScreenSection getPaginatedSection() {
        return paginatedSection;
    }

    public int getMaximumPageNumber() {
        if (paginatedSection == null || paginatedSection.isEmpty() || totalPaginatedItems <= 0) {
            return 1;
        }
        return FastMath.max(1, FastMath.ceilToInt((double) totalPaginatedItems / paginatedSection.size()));
    }

    public void clearPageCache() {
        pageCache.clear();
    }

    public @NonNull GuiScreen setEditableSection(@NonNull GuiScreenSection section,
                                                 @NonNull Collection<ItemStack> initialItems,
                                                 @Nullable BiConsumer<Player, List<ItemStack>> onFinalize) {
        this.editableSection = section;
        this.editableInitialItems.clear();
        this.editableInitialItems.addAll(initialItems);
        this.editFinalizationCallback = onFinalize;
        return this;
    }

    public @NonNull GuiScreen setEditableSection(int startSlot, int endSlot,
                                                 @NonNull Collection<ItemStack> initialItems,
                                                 @Nullable BiConsumer<Player, List<ItemStack>> onFinalize) {
        return setEditableSection(GuiScreenSection.of(startSlot, endSlot), initialItems, onFinalize);
    }

    public @NonNull GuiScreen setEditFilters(@Nullable GuiScreenEditFilters filters) {
        this.editFilters = filters;
        return this;
    }

    public @Nullable GuiScreenSection getEditableSection() {
        return editableSection;
    }

    public @Nullable GuiScreenEditFilters getEditFilters() {
        return editFilters;
    }

    public @Nullable BiConsumer<Player, List<ItemStack>> getEditFinalizationCallback() {
        return editFinalizationCallback;
    }

    public @NonNull List<ItemStack> getEditableInitialItems() {
        return new ArrayList<>(editableInitialItems);
    }

    public @NonNull GuiScreen addSlotListener(int slot, @NonNull Consumer<ItemStack> callback) {
        this.slotListeners.put(slot, callback);
        return this;
    }

    public @NonNull GuiScreen setTickHandler(@Nullable Runnable handler) {
        this.tickHandler = handler;
        return this;
    }

    public @Nullable Runnable getTickHandler() {
        return tickHandler;
    }

    public @NonNull Inventory createInventory(@NonNull Player player, int page) {
        int maxPages = getMaximumPageNumber();
        Component resolvedTitle = resolveTitle(title, page, maxPages);
        Inventory inv = Bukkit.createInventory(null, size.slots(), resolvedTitle);

        renderPermanentSlots(player, inv, page, maxPages);
        renderPaginatedSlots(inv, page);
        renderEditableSlots(inv);

        for (Integer slot : slotListeners.keySet()) {
            ItemStack item = inv.getItem(slot);
            lastKnownSlotContents.put(slot, item != null ? item.clone() : null);
        }

        return inv;
    }

    public void updateInventory(@NonNull Player player, @NonNull Inventory inv, int page) {
        int maxPages = getMaximumPageNumber();
        renderPermanentSlots(player, inv, page, maxPages);
        renderPaginatedSlots(inv, page);

        for (Map.Entry<Integer, Consumer<ItemStack>> entry : slotListeners.entrySet()) {
            int slot = entry.getKey();
            ItemStack current = inv.getItem(slot);
            ItemStack prev = lastKnownSlotContents.get(slot);

            boolean changed = false;
            if (prev == null && current != null) {
                changed = true;
            } else if (prev != null && (current == null || !prev.isSimilar(current) || prev.getAmount() != current.getAmount())) {
                changed = true;
            }

            if (changed) {
                lastKnownSlotContents.put(slot, current != null ? current.clone() : null);
                entry.getValue().accept(current);
            }
        }
    }

    private void renderPermanentSlots(Player player, Inventory inv, int page, int maxPages) {
        boolean isEmpty = totalPaginatedItems <= 0;
        for (Map.Entry<Integer, Object> entry : permanentSlots.entrySet()) {
            int slot = entry.getKey();
            if (slot >= inv.getSize()) continue;

            Object val = entry.getValue();
            if (val instanceof GuiButton button) {
                ItemStack item = button.render(page, maxPages, isEmpty);
                inv.setItem(slot, item);
            } else if (val instanceof ItemStack stack) {
                inv.setItem(slot, stack.clone());
            }
        }
    }

    private void renderPaginatedSlots(Inventory inv, int page) {
        if (paginatedSection == null || pageContentsRequester == null) return;

        List<Integer> slots = paginatedSection.getSlots();
        int pageSize = slots.size();
        if (pageSize == 0) return;

        int startIndex = (page - 1) * pageSize;
        int endIndex = FastMath.min(startIndex + pageSize - 1, totalPaginatedItems - 1);

        GuiPageContentsResult result = pageCache.computeIfAbsent(page, p ->
                pageContentsRequester.requestPageContents(p, startIndex, endIndex)
        );

        List<Object> contents = result.getContents();
        for (int i = 0; i < pageSize; i++) {
            int slot = slots.get(i);
            if (slot >= inv.getSize()) continue;

            if (i < contents.size()) {
                Object obj = contents.get(i);
                if (obj instanceof GuiButton button) {
                    inv.setItem(slot, button.render(page, getMaximumPageNumber(), totalPaginatedItems <= 0));
                } else if (obj instanceof ItemStack stack) {
                    inv.setItem(slot, stack.clone());
                }
            } else {
                inv.setItem(slot, null);
            }
        }
    }

    private void renderEditableSlots(Inventory inv) {
        if (editableSection == null) return;
        List<Integer> slots = editableSection.getSlots();
        for (int i = 0; i < slots.size(); i++) {
            int slot = slots.get(i);
            if (slot >= inv.getSize()) continue;

            if (i < editableInitialItems.size()) {
                ItemStack stack = editableInitialItems.get(i);
                inv.setItem(slot, stack != null ? stack.clone() : null);
            }
        }
    }

    public @Nullable GuiButton getButtonAt(int slot, int page) {
        Object perm = permanentSlots.get(slot);
        if (perm instanceof GuiButton button) {
            return button;
        }

        if (paginatedSection != null && paginatedSection.contains(slot)) {
            List<Integer> slots = paginatedSection.getSlots();
            int indexOnPage = slots.indexOf(slot);
            if (indexOnPage != -1) {
                GuiPageContentsResult result = pageCache.get(page);
                if (result != null && indexOnPage < result.getContents().size()) {
                    Object content = result.getContents().get(indexOnPage);
                    if (content instanceof GuiButton button) {
                        return button;
                    }
                }
            }
        }

        return null;
    }

    private Component resolveTitle(Component rawTitle, int page, int maxPages) {
        String plain = PlainTextComponentSerializer.plainText().serialize(rawTitle);
        if (!plain.contains("<page>") && !plain.contains("<pages>") &&
                !plain.contains("<prev_page>") && !plain.contains("<next_page>") &&
                !plain.contains("<total>")) {
            return rawTitle;
        }

        String serialized = MM.serialize(rawTitle);
        serialized = serialized
                .replace("<page>", String.valueOf(page))
                .replace("<pages>", String.valueOf(maxPages))
                .replace("<total>", String.valueOf(maxPages))
                .replace("<prev_page>", String.valueOf(FastMath.max(1, page - 1)))
                .replace("<next_page>", String.valueOf(FastMath.min(maxPages, page + 1)));

        return MM.deserialize(serialized);
    }

    public void rebuild() {
        parentContainer.rebuildScreen(this);
    }
}
