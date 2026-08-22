package dev.goren98.brewerydelight.client;

import dev.goren98.brewerydelight.brewing.BrewingStationMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Functional 6-2 shell UI; production-specific artwork and recipe indicators come with 6-2-4. */
public class BrewingStationScreen extends AbstractContainerScreen<BrewingStationMenu> {
    public BrewingStationScreen(BrewingStationMenu menu, Inventory inventory, Component title) { super(menu, inventory, title); imageWidth = 176; imageHeight = 166; }
    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFFC6C6C6);
        graphics.fill(leftPos + 7, topPos + 83, leftPos + 169, topPos + 84, 0xFF555555);
        for (int row = 0; row < 2; row++) for (int col = 0; col < 3; col++) slot(graphics, leftPos + 61 + col * 18, topPos + 19 + row * 18);
    }
    private static void slot(GuiGraphics g, int x, int y) { g.fill(x, y, x + 18, y + 18, 0xFF555555); g.fill(x + 1, y + 1, x + 17, y + 17, 0xFFEEEEEE); }
    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) { super.render(graphics, mouseX, mouseY, partialTick); renderTooltip(graphics, mouseX, mouseY); }
}
