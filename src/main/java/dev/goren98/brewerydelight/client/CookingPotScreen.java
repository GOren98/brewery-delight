package dev.goren98.brewerydelight.client;

import dev.goren98.brewerydelight.cooking.CookingPotMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class CookingPotScreen extends AbstractContainerScreen<CookingPotMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("brewerydelight", "textures/gui/cooking_pot.png");

    public CookingPotScreen(CookingPotMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        graphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
        int width = 24 * menu.progress() / menu.maxProgress();
        if (width > 0) graphics.fill(x + 103, y + 35, x + 103 + width, y + 51, 0xFFFFA000);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
