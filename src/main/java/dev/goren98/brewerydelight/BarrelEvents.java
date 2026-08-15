package dev.goren98.brewerydelight;

import dev.goren98.brewerydelight.barrel.BarrelInventory;
import dev.goren98.brewerydelight.barrel.BarrelLogic;
import dev.goren98.brewerydelight.barrel.BarrelSavedData;
import dev.goren98.brewerydelight.barrel.SmallBarrelPattern;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = BreweryDelight.MOD_ID)
public final class BarrelEvents {
    @SubscribeEvent
    public static void rightClick(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !(event.getEntity() instanceof ServerPlayer player)) return;
        BlockPos clicked = event.getPos();
        if (!(level.getBlockEntity(clicked) instanceof SignBlockEntity sign)) return;

        String front = sign.getFrontText().getMessage(0, false).getString().trim();
        String back = sign.getBackText().getMessage(0, false).getString().trim();
        if (!front.equalsIgnoreCase("Barrel") && !back.equalsIgnoreCase("Barrel")) return;

        SmallBarrelPattern.Match match = SmallBarrelPattern.find(level, clicked);
        if (match == null) {
            player.displayClientMessage(Component.literal("Invalid Small Barrel structure"), true);
            event.setCanceled(true);
            return;
        }

        BarrelSavedData data = BarrelSavedData.get(level);
        BarrelInventory inv = data.getOrCreate(match.controller());
        BarrelLogic.update(level, inv);
        player.openMenu(new SimpleMenuProvider(
                (id, playerInv, p) -> new ChestMenu(MenuType.GENERIC_9x1, id, playerInv, inv, 1),
                Component.literal(match.displayName())));
        player.displayClientMessage(Component.literal(match.displayName() + " created"), true);
        event.setCanceled(true);
    }
}
