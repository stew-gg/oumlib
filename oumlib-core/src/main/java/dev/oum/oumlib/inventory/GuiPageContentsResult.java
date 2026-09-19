package dev.oum.oumlib.inventory;

import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class GuiPageContentsResult {

    private final List<Object> contents = new ArrayList<>();

    public GuiPageContentsResult() {
    }

    public static @NonNull GuiPageContentsResult of() {
        return new GuiPageContentsResult();
    }

    public @NonNull GuiPageContentsResult addPageContent(@NonNull GuiButton button) {
        this.contents.add(button);
        return this;
    }

    public @NonNull GuiPageContentsResult addPageContent(@NonNull ItemStack item) {
        this.contents.add(item.clone());
        return this;
    }

    public @NonNull GuiPageContentsResult addPageContents(@NonNull Iterable<?> itemsOrButtons) {
        for (Object obj : itemsOrButtons) {
            if (obj instanceof GuiButton button) {
                addPageContent(button);
            } else if (obj instanceof ItemStack item) {
                addPageContent(item);
            } else {
                throw new IllegalArgumentException("Page content must be ItemStack or GuiButton, got: " + obj);
            }
        }
        return this;
    }

    public @NonNull List<Object> getContents() {
        return Collections.unmodifiableList(contents);
    }

    public int size() {
        return contents.size();
    }

    public boolean isEmpty() {
        return contents.isEmpty();
    }
}
