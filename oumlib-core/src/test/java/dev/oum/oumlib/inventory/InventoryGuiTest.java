package dev.oum.oumlib.inventory;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InventoryGuiTest {

    @Test
    public void testGuiSize() {
        assertEquals(9, GuiSize.ROWS_ONE.slots());
        assertEquals(1, GuiSize.ROWS_ONE.rows());
        assertEquals(18, GuiSize.ROWS_TWO.slots());
        assertEquals(27, GuiSize.ROWS_THREE.slots());
        assertEquals(36, GuiSize.ROWS_FOUR.slots());
        assertEquals(45, GuiSize.ROWS_FIVE.slots());
        assertEquals(54, GuiSize.ROWS_SIX.slots());
        assertEquals(6, GuiSize.ROWS_SIX.rows());

        assertEquals(GuiSize.ROWS_THREE, GuiSize.fromRows(3));
        assertEquals(GuiSize.ROWS_SIX, GuiSize.fromSlots(54));

        assertThrows(IllegalArgumentException.class, () -> GuiSize.fromRows(0));
        assertThrows(IllegalArgumentException.class, () -> GuiSize.fromRows(7));
        assertThrows(IllegalArgumentException.class, () -> GuiSize.fromSlots(15));
    }

    @Test
    public void testGuiUtilCoordinates() {
        assertEquals(0, GuiUtil.slot(0, 0));
        assertEquals(11, GuiUtil.slot(1, 2));
        assertEquals(53, GuiUtil.slot(5, 8));

        assertEquals(0, GuiUtil.slot1(1, 1));
        assertEquals(11, GuiUtil.slot1(2, 3));
        assertEquals(53, GuiUtil.slot1(6, 9));

        assertEquals(1, GuiUtil.row(11));
        assertEquals(2, GuiUtil.col(11));
        assertEquals(5, GuiUtil.row(53));
        assertEquals(8, GuiUtil.col(53));

        assertEquals("Diamond Sword", GuiUtil.formatName("DIAMOND_SWORD"));
        assertEquals("Golden Apple", GuiUtil.formatName("GOLDEN_APPLE"));
    }

    @Test
    public void testLayoutRectangleAndBorder() {
        int[] rect = Layout.rectangle(1, 1, 4, 7);
        assertEquals(28, rect.length);
        assertEquals(10, rect[0]); // row 1, col 1 = 10
        assertEquals(43, rect[rect.length - 1]); // row 4, col 7 = 43

        int[] border = Layout.border(3);
        assertEquals(20, border.length);
        // Top row 0..8
        for (int i = 0; i < 9; i++) {
            assertTrue(contains(border, i));
        }
        // Middle left 9, middle right 17
        assertTrue(contains(border, 9));
        assertTrue(contains(border, 17));
        assertFalse(contains(border, 10)); // inside slot
        // Bottom row 18..26
        for (int i = 18; i <= 26; i++) {
            assertTrue(contains(border, i));
        }
    }

    @Test
    public void testGuiScreenSection() {
        GuiScreenSection section = GuiScreenSection.rectangle(0, 0, 1, 1);
        assertEquals(4, section.size());
        assertTrue(section.contains(0));
        assertTrue(section.contains(1));
        assertTrue(section.contains(9));
        assertTrue(section.contains(10));
        assertFalse(section.contains(2));

        section.addSlotRange(20, 22);
        assertEquals(7, section.size());
        assertTrue(section.contains(20));
        assertTrue(section.contains(21));
        assertTrue(section.contains(22));

        List<Integer> slots = section.getSlots();
        assertEquals(7, slots.size());
    }

    @Test
    public void testGuiButtonFlags() {
        GuiButton button = GuiButton.of()
                .flags(GuiButtonFlag.HIDE_IF_FIRST_PAGE);

        assertTrue(button.isHidden(1, 5, false));
        assertFalse(button.isHidden(2, 5, false));

        GuiButton lastButton = GuiButton.of()
                .flags(GuiButtonFlag.HIDE_IF_LAST_PAGE);

        assertFalse(lastButton.isHidden(2, 5, false));
        assertTrue(lastButton.isHidden(5, 5, false));

        GuiButton singleButton = GuiButton.of()
                .flags(GuiButtonFlag.HIDE_IF_SINGLE_PAGE);

        assertTrue(singleButton.isHidden(1, 1, false));
        assertFalse(singleButton.isHidden(1, 2, false));

        GuiButton emptyButton = GuiButton.of()
                .flags(GuiButtonFlag.HIDE_IF_EMPTY);

        assertTrue(emptyButton.isHidden(1, 1, true));
        assertFalse(emptyButton.isHidden(1, 1, false));
    }

    @Test
    public void testPageContentsResult() {
        GuiPageContentsResult result = GuiPageContentsResult.of();
        assertTrue(result.isEmpty());

        GuiButton b1 = GuiButton.of();
        GuiButton b2 = GuiButton.of();
        result.addPageContent(b1).addPageContent(b2);

        assertEquals(2, result.size());
        assertFalse(result.isEmpty());
        assertSame(b1, result.getContents().get(0));
        assertSame(b2, result.getContents().get(1));
    }

    @Test
    public void testGuiContainerCreationAndNavigation() {
        GuiContainer container = GuiContainer.create()
                .setTickRate(1)
                .setPersistent(true)
                .preventItemDropping(true);

        assertEquals(1, container.getTickRate());
        assertTrue(container.isPersistent());
        assertTrue(container.preventsItemDropping());

        GuiScreen screen1 = container.createScreen(GuiSize.ROWS_THREE)
                .setTitle("Screen 1");
        GuiScreen screen2 = container.createScreen(GuiSize.ROWS_SIX)
                .setTitle("Screen 2");

        assertEquals(2, container.getScreens().size());
        assertSame(screen1, container.getScreen(0));
        assertSame(screen2, container.getScreen(1));
    }

    private boolean contains(int[] array, int val) {
        for (int v : array) {
            if (v == val) return true;
        }
        return false;
    }
}
