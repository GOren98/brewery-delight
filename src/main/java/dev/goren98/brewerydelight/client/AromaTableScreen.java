package dev.goren98.brewerydelight.client;

import dev.goren98.brewerydelight.aroma.table.AromaTableMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Batch Aroma processing UI: donor + receiver -> output. */
public class AromaTableScreen extends AbstractContainerScreen<AromaTableMenu> {
    public AromaTableScreen(AromaTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFFC6C6C6);
        graphics.fill(leftPos + 7, topPos + 83, leftPos + 169, topPos + 84, 0xFF555555);

        slot(graphics, leftPos + 37, topPos + 34);
        slot(graphics, leftPos + 73, topPos + 34);
        slot(graphics, leftPos + 133, topPos + 34);

        graphics.drawString(font, "+", leftPos + 61, topPos + 39, 0xFF404040, false);

        int arrowX = leftPos + 98;
        int arrowY = topPos + 39;
        graphics.fill(arrowX, arrowY + 4, arrowX + 28, arrowY + 10, 0xFF777777);
        graphics.fill(arrowX + 22, arrowY, arrowX + 28, arrowY + 14, 0xFF777777);
        int filled = menu.progressScaled(28);
        if (filled > 0) {
            graphics.fill(arrowX, arrowY + 4, arrowX + filled, arrowY + 10, 0xFFFFFFFF);
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                slot(graphics, leftPos + 7 + col * 18, topPos + 83 + row * 18);
            }
        }
        for (int col = 0; col < 9; col++) {
            slot(graphics, leftPos + 7 + col * 18, topPos + 141);
        }
    }

    private static void slot(GuiGraphics graphics, int x, int y) {
        graphics.fill(x, y, x + 18, y + 18, 0xFF555555);
        graphics.fill(x + 1, y + 1, x + 17, y + 17, 0xFF8B8B8B);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
