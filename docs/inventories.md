# Inventories

`dev.oum.oumlib.inventory` · Paper

---

## ChestMenu

Build a chest GUI with a pattern layout:

```java
ChestMenu.builder()
    .title("<dark_gray>Warps</dark_gray>")
    .rows(3)
    .pattern(
        "#########",
        "# A B C #",
        "#########"
    )
    .bind('#', ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).name(" ").build())
    .bind('A', ItemBuilder.of(Material.GRASS_BLOCK).name("<green>Spawn").build())
    .bind('B', ItemBuilder.of(Material.NETHERRACK).name("<red>Nether").build())
    .bind('C', ItemBuilder.of(Material.END_STONE).name("<dark_purple>End").build())
    .onClick('A', click -> click.player().performCommand("warp spawn"))
    .onClick('B', click -> click.player().performCommand("warp nether"))
    .onClick('C', click -> click.player().performCommand("warp end"))
    .build()
    .open(player);
```

### Click Handlers

Each bound character can have a click handler. The `ClickContext` gives you:

```java
.onClick('X', click -> {
    Player p = click.player();          // the player who clicked
    ClickAction action = click.action(); // LEFT, RIGHT, SHIFT_LEFT, etc.
    ItemStack item = click.item();      // the clicked item
})
```

### Close Handler

```java
.onClose(player -> {
    player.sendMessage("Menu closed!");
})
```

### Prevent Taking Items

By default, players can't take items from the menu. The whole inventory is locked.

---

## PaginatedMenu (Deprecated)

> [!NOTE]
> `PaginatedMenu` is deprecated as of 1.0.9 in favor of `GuiContainer` and `GuiScreen#setPaginatedSection`.

For simple legacy list pagination:

```java
List<ItemStack> items = getShopItems(); // your list

PaginatedMenu.builder()
    .title("<dark_gray>Shop - Page <page>/<pages></dark_gray>")
    .rows(6)
    .items(items)
    .contentSlots(Layout.rectangle(1, 1, 4, 7)) // rows 1-4, columns 1-7
    .previousButton(ItemBuilder.of(Material.ARROW).name("<yellow>Previous Page").build(), 45)
    .nextButton(ItemBuilder.of(Material.ARROW).name("<yellow>Next Page").build(), 53)
    .border(ItemBuilder.of(Material.BLACK_STAINED_GLASS_PANE).name(" ").build())
    .onItemClick((click, item) -> {
        click.player().sendMessage("You clicked: " + item.getType());
    })
    .build()
    .open(player);
```

The `<page>` and `<pages>` placeholders in the title get replaced automatically.

---

## ItemBuilder

Fluent builder for creating items:

```java
ItemStack sword = ItemBuilder.of(Material.DIAMOND_SWORD)
    .name("<gradient:aqua:blue>Frost Blade</gradient>")
    .lore(
        "<gray>A blade forged in ice.",
        "",
        "<blue>+15 Attack Damage"
    )
    .enchant(Enchantment.SHARPNESS, 5)
    .unbreakable(true)
    .modelData(1001)
    .amount(1)
    .glow(true)
    .build();
```

### ItemBuilder Methods

| Method                     | What it does                               |
|:---------------------------|:-------------------------------------------|
| `.name(miniMessage)`       | Display name                               |
| `.lore(lines...)`          | Lore lines (MiniMessage)                   |
| `.enchant(enchant, level)` | Add enchantment                            |
| `.unbreakable(bool)`       | Set unbreakable                            |
| `.modelData(int)`          | Custom model data                          |
| `.amount(int)`             | Stack size                                 |
| `.glow(bool)`              | Enchantment glint without visible enchants |
| `.flags(flags...)`         | Item flags                                 |
| `.rarity(ItemRarity)`      | Item rarity                                |
| `.maxStackSize(int)`       | Override max stack size                    |
| `.skull(player)`           | Player head                                |
| `.skullTexture(base64)`    | Custom skull texture                       |
| `.skullUrl(url)`           | Skull from URL                             |
| `.pdc(key, value)`         | Store persistent data                      |
| `.pdc(key, component)`     | Store a Component in PDC                   |
| `.meta(consumer)`          | Modify raw ItemMeta                        |
| `.build()`                 | Creates the ItemStack                      |

### Skull Heads

```java
// Player head
ItemBuilder.of(Material.PLAYER_HEAD).skull(player).build();

// Custom texture via base64
ItemBuilder.of(Material.PLAYER_HEAD)
    .skullTexture("eyJ0ZXh0dXJlcyI6ey...")
    .build();

// Custom texture via URL
ItemBuilder.of(Material.PLAYER_HEAD)
    .skullUrl("https://textures.minecraft.net/texture/abc123")
    .build();
```

---

## DataComponents

Paper 1.20.6+ data component access:

```java
DataComponents.maxStackSize(item, 99);
DataComponents.rarity(item, ItemRarity.EPIC);
DataComponents.enchantGlint(item, true);
DataComponents.fireResistant(item, true);
DataComponents.hideTooltip(item, true);
DataComponents.unbreakable(item, true);
```

---

## ItemSerializer

Convert items to/from Base64 for storage:

```java
String encoded = ItemSerializer.toBase64(itemStack);
ItemStack decoded = ItemSerializer.fromBase64(encoded);
```

Also works with arrays:

```java
String encoded = ItemSerializer.arrayToBase64(itemArray);
ItemStack[] decoded = ItemSerializer.arrayFromBase64(encoded);
```

---

## PotionSerializer

Serialize/deserialize potion effects:

```java
String json = PotionSerializer.serialize(potionEffect);
PotionEffect effect = PotionSerializer.deserialize(json);

// Lists
String json = PotionSerializer.serializeList(effects);
List<PotionEffect> effects = PotionSerializer.deserializeList(json);
```

## GuiContainer & GuiScreen (Modern Framework)

For advanced multi-screen interfaces, dynamic animations, paginated sections, and editable player inventories, use `GuiContainer`. Inspired by Rosewood's GuiFramework, it gives you a robust multi-screen container model with breadcrumbs, screen transitions, ticking, and component buttons.

### Creating a Multi-Screen Container

```java
GuiContainer container = GuiContainer.create()
    .setTickRate(1)         // ticks dynamic buttons/screens every 1 tick
    .setPersistent(false)   // automatically unregisters when all players close
    .preventItemDropping(true);

// Screen 0: Category Selector (3 rows)
GuiScreen categoryScreen = container.createScreen(GuiSize.ROWS_THREE)
    .setTitle("<gradient:gold:yellow>Select Category</gradient>");

GuiUtil.fillBorders(categoryScreen, ItemBuilder.of(Material.BLACK_STAINED_GLASS_PANE).name(" ").build());

// Screen 1: Item Browser (6 rows)
GuiScreen browseScreen = container.createScreen(GuiSize.ROWS_SIX)
    .setTitle("<yellow>Category Browser - Page <page>/<pages></yellow>");

GuiUtil.fillBorders(browseScreen, ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).name(" ").build());

// Button to transition forward to Screen 1
categoryScreen.addButtonAt(13, GuiButton.of(Material.DIAMOND)
    .name("<aqua>View Diamonds")
    .loreStrings("<gray>Click to browse diamond items")
    .onClick(ctx -> GuiAction.TRANSITION_FORWARDS));

// Back button on Screen 1 to return to Screen 0
browseScreen.addButtonAt(45, GuiButton.of(Material.ARROW)
    .name("<yellow>Go Back")
    .onClick(ctx -> GuiAction.TRANSITION_BACKWARDS));

// Open for player
container.open(player);
```

---

## GuiButton & Reactive Animations

`GuiButton` is a first-class component supporting MiniMessage text, click sounds, conditional visibility, and dynamic suppliers for live animations:

```java
AtomicInteger hue = new AtomicInteger(0);

GuiButton animatedButton = GuiButton.of(Material.LEATHER_CHESTPLATE)
    .nameStringSupplier(() -> "<gradient:red:gold>Animated Level: " + player.getLevel() + "</gradient>")
    .loreStringSupplier(() -> List.of(
        "<gray>Real-time player balance: <green>$" + getBalance(player),
        "<yellow>Updated every tick automatically!"
    ))
    .iconSupplier(() -> {
        int h = hue.addAndGet(10) % 360;
        return ItemBuilder.of(Material.LEATHER_CHESTPLATE)
            .leatherColor(Color.fromRGB(255, (h * 2) % 255, 100))
            .build();
    })
    .clickSound(Key.key("minecraft:ui.button.click"), 1.0f, 1.2f)
    .onClick(ctx -> {
        ctx.player().sendMessage("Button clicked!");
        return GuiAction.NOTHING; // or REFRESH, CLOSE, PAGE_FORWARDS, etc.
    });

screen.addButtonAt(GuiUtil.slot(2, 4), animatedButton);
```

### Action Results (`GuiAction`)
- `GuiAction.NOTHING`: Do nothing
- `GuiAction.REFRESH`: Rerender current screen
- `GuiAction.CLOSE`: Close the inventory
- `GuiAction.PAGE_FORWARDS` / `PAGE_BACKWARDS`: Change page in paginated section
- `GuiAction.PAGE_FIRST` / `PAGE_LAST`: Jump to first or last page
- `GuiAction.TRANSITION_FORWARDS` / `TRANSITION_BACKWARDS`: Move between screens

---

## Paginated Sections

Instead of locking an entire inventory into pagination, you can define a `GuiScreenSection` on any sub-region of a `GuiScreen`:

```java
GuiScreenSection contentSection = GuiScreenSection.rectangle(1, 1, 4, 7); // 28 slots

screen.setPaginatedSection(contentSection, items.size(), (pageNumber, startIndex, endIndex) -> {
    GuiPageContentsResult result = GuiPageContentsResult.of();
    for (int i = startIndex; i <= Math.min(endIndex, items.size() - 1); i++) {
        result.addPageContent(items.get(i));
    }
    return result;
});

// Auto-hiding navigation buttons with GuiButtonFlag
screen.addButtonAt(47, GuiButton.of(Material.PAPER)
    .name("<yellow>Previous (<prev_page>/<pages>)")
    .flags(GuiButtonFlag.HIDE_IF_FIRST_PAGE)
    .hiddenReplacement(ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).name(" ").build())
    .onClick(ctx -> GuiAction.PAGE_BACKWARDS));

screen.addButtonAt(51, GuiButton.of(Material.PAPER)
    .name("<yellow>Next (<next_page>/<pages>)")
    .flags(GuiButtonFlag.HIDE_IF_LAST_PAGE)
    .hiddenReplacement(ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).name(" ").build())
    .onClick(ctx -> GuiAction.PAGE_FORWARDS));
```

---

## Editable Sections

Designate safe zones where players can place, retrieve, and modify items (e.g. for enchanting tables, trash bins, auction deposits, or item upgraders):

```java
GuiScreenSection dropZone = GuiScreenSection.of(10, 11, 12, 13, 14, 15, 16);

screen.setEditableSection(dropZone, List.of(), (player, depositedItems) -> {
    player.sendMessage("You saved " + depositedItems.size() + " items!");
    giveReward(player, depositedItems);
});

// Restrict what items can be placed
screen.setEditFilters(GuiScreenEditFilters.create()
    .whitelist(Material.DIAMOND, Material.NETHERITE_INGOT)
    .maxItems(64));

// Listen to changes in real-time
screen.addSlotListener(13, newItem -> {
    player.sendMessage("Center slot changed to: " + (newItem != null ? newItem.getType() : "Empty"));
});
```

---

## GuiUtil & Layout Helpers

```java
// Fill patterns
GuiUtil.fillScreen(screen, borderItem);
GuiUtil.fillBorders(screen, borderItem);
GuiUtil.fillRectangle(screen, 1, 1, 4, 7, item);
GuiUtil.fillRow(screen, 0, headerItem);
GuiUtil.fillColumn(screen, 8, sidebarItem);

// Coordinates (0-indexed and 1-indexed)
int slot = GuiUtil.slot(row, col);      // row 0-5, col 0-8
int slot1 = GuiUtil.slot1(row, col);    // row 1-6, col 1-9
int row = GuiUtil.row(slot);
int col = GuiUtil.col(slot);

// Layout helpers
int[] rect = Layout.rectangle(startRow, startCol, endRow, endCol);
int[] border = Layout.border(rows);
List<Integer> slots = Layout.rectangleList(startRow, startCol, endRow, endCol);
```
