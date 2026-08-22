package dev.goren98.brewerydelight.client;

import dev.goren98.brewerydelight.brewing.BrewingStationMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Fuel-less furnace-style UI: Base input -> progress -> Core Alcohol output. */
public class BrewingStationScreen extends AbstractContainerScreen<BrewingStationMenu> {
    public BrewingStationScreen(BrewingStationMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFFC6C6C6);
        graphics.fill(leftPos + 7, topPos + 83, leftPos + 169, topPos + 84, 0xFF555555);

        slot(graphics, leftPos + 55, topPos + 34);
        slot(graphics, leftPos + 115, topPos + 34);

        int arrowX = leftPos + 79;
        int arrowY = topPos + 39;
        graphics.fill(arrowX, arrowY + 4, arrowX + 28, arrowY + 10, 0xFF777777);
        graphics.fill(arrowX + 22, arrowY, arrowX + 28, arrowY + 14, 0xFF777777);
        int filled = menu.progressScaled(28);
        if (filled > 0) graphics.fill(arrowX, arrowY + 4, arrowX + filled, arrowY + 10, 0xFFFFFFFF);
    }

    private static void slot(GuiGraphics g, int x, int y) {
        g.fill(x, y, x + 18, y + 18, 0xFF555555);
        g.fill(x + 1, y + 1, x + 17, y + 17, 0xFFEEEEEE);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
