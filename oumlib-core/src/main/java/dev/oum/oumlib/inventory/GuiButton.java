package dev.oum.oumlib.inventory;

import dev.oum.oumlib.math.FastMath;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class GuiButton {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private ItemStack iconItem;
    private Material iconMaterial = Material.STONE;
    private Consumer<ItemMeta> itemMetaApplier;
    private int amount = 1;
    private Component name;
    private List<Component> lore = new ArrayList<>();
    private boolean glowing = false;
    private final Set<ItemFlag> itemFlags = new HashSet<>();
    private final Set<GuiButtonFlag> flags = new HashSet<>();
    private Sound clickSound;
    private boolean visible = true;
    private ItemStack hiddenReplacement;

    private Supplier<ItemStack> iconSupplier;
    private Supplier<Component> nameSupplier;
    private Supplier<List<Component>> loreSupplier;
    private Supplier<Integer> amountSupplier;
    private Supplier<Boolean> glowingSupplier;
    private Supplier<Boolean> visibilitySupplier;
    private Supplier<ItemStack> hiddenReplacementSupplier;

    private Function<ClickContext, GuiAction> clickAction;
    private final Set<ClickType> allowedClickTypes = new HashSet<>();

    public GuiButton() {
    }

    public GuiButton(@NonNull ItemStack icon) {
        this.iconItem = icon.clone();
        this.iconMaterial = icon.getType();
        this.amount = icon.getAmount();
    }

    public GuiButton(@NonNull Material material) {
        this.iconMaterial = material;
    }

    @Contract(" -> new")
    @CheckReturnValue
    public static @NonNull GuiButton of() {
        return new GuiButton();
    }

    @Contract("_ -> new")
    @CheckReturnValue
    public static @NonNull GuiButton of(@NonNull Material material) {
        return new GuiButton(material);
    }

    @Contract("_ -> new")
    @CheckReturnValue
    public static @NonNull GuiButton of(@NonNull ItemStack item) {
        return new GuiButton(item);
    }

    @Contract("_ -> new")
    @CheckReturnValue
    public static @NonNull GuiButton of(@NonNull ItemBuilder builder) {
        return new GuiButton(builder.build());
    }

    public @NonNull GuiButton icon(@NonNull Material material) {
        this.iconMaterial = material;
        this.iconItem = null;
        return this;
    }

    public @NonNull GuiButton icon(@NonNull Material material, @NonNull Consumer<ItemMeta> metaApplier) {
        this.iconMaterial = material;
        this.itemMetaApplier = metaApplier;
        this.iconItem = null;
        return this;
    }

    public @NonNull GuiButton icon(@NonNull ItemStack item) {
        this.iconItem = item.clone();
        this.iconMaterial = item.getType();
        return this;
    }

    public @NonNull GuiButton iconSupplier(@NonNull Supplier<ItemStack> supplier) {
        this.iconSupplier = supplier;
        return this;
    }

    public @NonNull GuiButton amount(int amount) {
        this.amount = FastMath.max(1, amount);
        return this;
    }

    public @NonNull GuiButton amountSupplier(@NonNull Supplier<Integer> supplier) {
        this.amountSupplier = supplier;
        return this;
    }

    public @NonNull GuiButton name(@NonNull Component name) {
        this.name = name;
        return this;
    }

    public @NonNull GuiButton name(@NonNull String miniMessageString) {
        this.name = MM.deserialize(miniMessageString);
        return this;
    }

    public @NonNull GuiButton nameSupplier(@NonNull Supplier<Component> supplier) {
        this.nameSupplier = supplier;
        return this;
    }

    public @NonNull GuiButton nameStringSupplier(@NonNull Supplier<String> supplier) {
        this.nameSupplier = () -> MM.deserialize(supplier.get());
        return this;
    }

    public @NonNull GuiButton lore(@NonNull Component @NonNull ... lines) {
        this.lore = new ArrayList<>(Arrays.asList(lines));
        return this;
    }

    public @NonNull GuiButton lore(@NonNull List<Component> lines) {
        this.lore = new ArrayList<>(lines);
        return this;
    }

    public @NonNull GuiButton loreStrings(@NonNull String @NonNull ... lines) {
        this.lore = Arrays.stream(lines).map(MM::deserialize).toList();
        return this;
    }

    public @NonNull GuiButton loreStrings(@NonNull List<String> lines) {
        this.lore = lines.stream().map(MM::deserialize).toList();
        return this;
    }

    public @NonNull GuiButton loreSupplier(@NonNull Supplier<List<Component>> supplier) {
        this.loreSupplier = supplier;
        return this;
    }

    public @NonNull GuiButton loreStringSupplier(@NonNull Supplier<List<String>> supplier) {
        this.loreSupplier = () -> supplier.get().stream().map(MM::deserialize).toList();
        return this;
    }

    public @NonNull GuiButton glowing(boolean glowing) {
        this.glowing = glowing;
        return this;
    }

    public @NonNull GuiButton glowingSupplier(@NonNull Supplier<Boolean> supplier) {
        this.glowingSupplier = supplier;
        return this;
    }

    public @NonNull GuiButton itemFlags(ItemFlag @NonNull ... flags) {
        this.itemFlags.addAll(Arrays.asList(flags));
        return this;
    }

    public @NonNull GuiButton flags(GuiButtonFlag @NonNull ... flags) {
        this.flags.addAll(Arrays.asList(flags));
        return this;
    }

    public @NonNull GuiButton visible(boolean visible) {
        this.visible = visible;
        return this;
    }

    public @NonNull GuiButton visibilitySupplier(@NonNull Supplier<Boolean> supplier) {
        this.visibilitySupplier = supplier;
        return this;
    }

    public @NonNull GuiButton hiddenReplacement(@Nullable ItemStack item) {
        this.hiddenReplacement = item != null ? item.clone() : null;
        return this;
    }

    public @NonNull GuiButton hiddenReplacementSupplier(@NonNull Supplier<ItemStack> supplier) {
        this.hiddenReplacementSupplier = supplier;
        return this;
    }

    public @NonNull GuiButton clickSound(@Nullable Sound sound) {
        this.clickSound = sound;
        return this;
    }

    public @NonNull GuiButton clickSound(@NonNull Key soundKey, float volume, float pitch) {
        this.clickSound = Sound.sound(soundKey, Sound.Source.PLAYER, volume, pitch);
        return this;
    }

    public @NonNull GuiButton clickSound(@NonNull Key soundKey) {
        return clickSound(soundKey, 1.0f, 1.0f);
    }

    public @NonNull GuiButton clickSound(Sound.@NonNull Type soundType, float volume, float pitch) {
        this.clickSound = Sound.sound(soundType, Sound.Source.PLAYER, volume, pitch);
        return this;
    }

    public @NonNull GuiButton clickSound(Sound.@NonNull Type soundType) {
        return clickSound(soundType, 1.0f, 1.0f);
    }

    public @NonNull GuiButton onClick(@NonNull Function<ClickContext, GuiAction> action) {
        this.clickAction = action;
        return this;
    }

    public @NonNull GuiButton onClick(@NonNull Function<ClickContext, GuiAction> action, ClickType @NonNull ... types) {
        this.clickAction = action;
        this.allowedClickTypes.addAll(Arrays.asList(types));
        return this;
    }

    public @NonNull GuiButton onClick(@NonNull Consumer<ClickContext> action) {
        this.clickAction = ctx -> {
            action.accept(ctx);
            return GuiAction.NOTHING;
        };
        return this;
    }

    public @NonNull GuiButton onClick(@NonNull Consumer<ClickContext> action, ClickType @NonNull ... types) {
        this.clickAction = ctx -> {
            action.accept(ctx);
            return GuiAction.NOTHING;
        };
        this.allowedClickTypes.addAll(Arrays.asList(types));
        return this;
    }

    public @Nullable Sound getClickSound() {
        return clickSound;
    }

    public boolean allowsClickType(@NonNull ClickType type) {
        return allowedClickTypes.isEmpty() || allowedClickTypes.contains(type);
    }

    public @NonNull GuiAction handleClick(@NonNull ClickContext context) {
        if (!allowsClickType(context.bukkitClickType())) {
            return GuiAction.NOTHING;
        }
        if (clickAction != null) {
            return clickAction.apply(context);
        }
        return GuiAction.NOTHING;
    }

    public boolean isHidden(int currentPage, int totalPages, boolean isContainerEmpty) {
        if (!visible) return true;
        if (visibilitySupplier != null && !visibilitySupplier.get()) return true;

        if (flags.contains(GuiButtonFlag.HIDE_IF_FIRST_PAGE) && currentPage <= 1) return true;
        if (flags.contains(GuiButtonFlag.HIDE_IF_LAST_PAGE) && currentPage >= totalPages) return true;
        if (flags.contains(GuiButtonFlag.HIDE_IF_SINGLE_PAGE) && totalPages <= 1) return true;
        if (flags.contains(GuiButtonFlag.HIDE_IF_EMPTY) && isContainerEmpty) return true;

        return false;
    }

    public @Nullable ItemStack renderHidden() {
        if (hiddenReplacementSupplier != null) {
            return hiddenReplacementSupplier.get();
        }
        return hiddenReplacement != null ? hiddenReplacement.clone() : null;
    }

    public @Nullable ItemStack render(int currentPage, int totalPages, boolean isContainerEmpty) {
        if (isHidden(currentPage, totalPages, isContainerEmpty)) {
            return renderHidden();
        }

        ItemStack base;
        if (iconSupplier != null) {
            base = iconSupplier.get();
            if (base == null) return null;
            base = base.clone();
        } else if (iconItem != null) {
            base = iconItem.clone();
        } else {
            base = new ItemStack(iconMaterial);
        }

        int finalAmount = amountSupplier != null ? amountSupplier.get() : amount;
        base.setAmount(FastMath.clamp(finalAmount, 1, 64));

        ItemMeta meta = base.getItemMeta();
        if (meta != null) {
            Component finalName = nameSupplier != null ? nameSupplier.get() : name;
            if (finalName != null) {
                meta.displayName(replacePlaceholders(finalName, currentPage, totalPages));
            }

            List<Component> finalLore = loreSupplier != null ? loreSupplier.get() : lore;
            if (finalLore != null && !finalLore.isEmpty()) {
                List<Component> processedLore = finalLore.stream()
                        .map(line -> replacePlaceholders(line, currentPage, totalPages))
                        .toList();
                meta.lore(processedLore);
            }

            if (!itemFlags.isEmpty()) {
                itemFlags.forEach(meta::addItemFlags);
            }

            if (itemMetaApplier != null) {
                itemMetaApplier.accept(meta);
            }

            base.setItemMeta(meta);
        }

        boolean finalGlowing = glowingSupplier != null ? glowingSupplier.get() : glowing;
        if (finalGlowing) {
            base = ItemBuilder.of(base).glow(true).build();
        }

        return base;
    }

    private static Component replacePlaceholders(Component comp, int currentPage, int totalPages) {
        String plain = PlainTextComponentSerializer.plainText().serialize(comp);
        if (!plain.contains("<page>") && !plain.contains("<pages>") &&
                !plain.contains("<prev_page>") && !plain.contains("<next_page>") &&
                !plain.contains("<total>")) {
            return comp;
        }

        String serialized = MM.serialize(comp);
        serialized = serialized
                .replace("<page>", String.valueOf(currentPage))
                .replace("<pages>", String.valueOf(totalPages))
                .replace("<total>", String.valueOf(totalPages))
                .replace("<prev_page>", String.valueOf(FastMath.max(1, currentPage - 1)))
                .replace("<next_page>", String.valueOf(FastMath.min(totalPages, currentPage + 1)));

        return MM.deserialize(serialized);
    }
}
