package dev.goren98.brewerydelight;

import dev.goren98.brewerydelight.barrel.BarrelInventory;
import dev.goren98.brewerydelight.barrel.BarrelLogic;
import dev.goren98.brewerydelight.barrel.BarrelMenu;
import dev.goren98.brewerydelight.barrel.BarrelSavedData;
import dev.goren98.brewerydelight.barrel.SmallBarrelPattern;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

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
        inv.configureWood(match.woodId());
        BarrelLogic.update(level, inv);

        StringBuilder title = new StringBuilder(match.displayName())
                .append(" [").append(pretty(inv.getAroma()));

        if (!inv.getSeasoningTarget().isEmpty() && inv.getSeasoningCount() < BarrelInventory.SEASONING_REQUIRED) {
            String target = seasoningAroma(inv.getSeasoningTarget());
            title.append(" → ").append(pretty(target))
                    .append(' ').append(inv.getSeasoningCount())
                    .append('/').append(BarrelInventory.SEASONING_REQUIRED);
        }
        title.append(']');

        player.openMenu(new SimpleMenuProvider(
                (id, playerInv, p) -> new BarrelMenu(id, playerInv, inv),
                Component.literal(title.toString())));
        player.displayClientMessage(Component.literal(match.displayName() + " ready"), true);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel() instanceof ServerLevel level) {
            BarrelSavedData.get(level).resetEmptyBarrelAt(event.getPos());
        }
    }

    private static String seasoningAroma(String target) {
        int split = target.indexOf('|');
        return split >= 0 && split + 1 < target.length() ? target.substring(split + 1) : target;
    }

    private static String pretty(String value) {
        String[] parts = value.split("_");
        StringBuilder out = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) continue;
            if (!out.isEmpty()) out.append(' ');
            out.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return out.toString();
    }
}
