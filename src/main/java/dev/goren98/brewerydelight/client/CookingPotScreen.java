package dev.goren98.brewerydelight.client;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.goren98.brewerydelight.cooking.CookingPotMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** Farmer's Delight Cooking Pot screen layout, adapted to Brewery Delight. */
public class CookingPotScreen extends AbstractContainerScreen<CookingPotMenu> {
    private static final ResourceLocation BACKGROUND_TEXTURE = ResourceLocation.fromNamespaceAndPath("brewerydelight", "textures/gui/cooking_pot.png");
    private static final int PROGRESS_X = 89;
    private static final int PROGRESS_Y = 25;
    private static final int PROGRESS_U = 176;
    private static final int PROGRESS_V = 15;
    private static final int PROGRESS_HEIGHT = 17;

    public CookingPotScreen(CookingPotMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = 28;
        inventoryLabelY = imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        graphics.blit(BACKGROUND_TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        int progress = menu.getCookProgressionScaled();
        if (progress > 0) {
            graphics.blit(BACKGROUND_TEXTURE, leftPos + PROGRESS_X, topPos + PROGRESS_Y,
                    PROGRESS_U, PROGRESS_V, progress + 1, PROGRESS_HEIGHT);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
