package dev.goren98.brewerydelight.client;

import dev.goren98.brewerydelight.aroma.table.AromaTableMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Donor(left) -> receiver(right) foundation UI. Transfer/upgrade execution is intentionally 6-2-5. */
public class AromaTableScreen extends AbstractContainerScreen<AromaTableMenu> {
    public AromaTableScreen(AromaTableMenu menu, Inventory inventory, Component title) { super(menu, inventory, title); imageWidth = 176; imageHeight = 166; }
    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFFC6C6C6);
        slot(graphics, leftPos + 55, topPos + 34); slot(graphics, leftPos + 103, topPos + 34);
        graphics.drawString(font, "Aroma  >", leftPos + 73, topPos + 39, 0xFF404040, false);
        graphics.fill(leftPos + 7, topPos + 83, leftPos + 169, topPos + 84, 0xFF555555);
    }
    private static void slot(GuiGraphics g, int x, int y) { g.fill(x, y, x + 18, y + 18, 0xFF555555); g.fill(x + 1, y + 1, x + 17, y + 17, 0xFFEEEEEE); }
    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) { super.render(graphics, mouseX, mouseY, partialTick); renderTooltip(graphics, mouseX, mouseY); }
}
