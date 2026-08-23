package dev.goren98.brewerydelight.crop;

import dev.goren98.brewerydelight.registry.ModComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.block.CropGrowEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Bridges CROP_AROMA onto vanilla Potato, Melon and Cocoa cultivation. */
public final class VanillaCropAromaEvents {
    private record PendingPlant(Item item, String aroma) {}
    private static final Map<UUID, PendingPlant> PENDING = new HashMap<>();

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide) return;
        ItemStack stack = event.getItemStack();
        String defaultAroma = plantingDefault(stack);
        if (defaultAroma == null) {
            PENDING.remove(event.getEntity().getUUID());
            return;
        }
        String aroma = stack.getOrDefault(ModComponents.CROP_AROMA.get(), defaultAroma);
        PENDING.put(event.getEntity().getUUID(), new PendingPlant(stack.getItem(), aroma));
    }

    public static void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !(event.getEntity() instanceof Player player)) return;
        PendingPlant pending = PENDING.remove(player.getUUID());
        if (pending == null || !matchesPlacedBlock(pending.item(), event.getPlacedBlock())) return;
        String defaultAroma = defaultForState(event.getPlacedBlock());
        if (defaultAroma != null) VanillaCropAromaSavedData.get(level).put(event.getPos(), pending.aroma(), defaultAroma);
    }

    public static void onCropGrow(CropGrowEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        BlockPos stemPos = event.getPos();
        BlockState state = level.getBlockState(stemPos);
        if (!state.is(Blocks.MELON_STEM) && !state.is(Blocks.ATTACHED_MELON_STEM)) return;
        VanillaCropAromaSavedData data = VanillaCropAromaSavedData.get(level);
        data.get(stemPos).ifPresent(aroma -> {
            for (BlockPos target : horizontalNeighbors(stemPos)) {
                if (level.getBlockState(target).is(Blocks.MELON)) data.put(target, aroma, "melon");
            }
        });
    }

    public static void onBlockDrops(BlockDropsEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        BlockState state = event.getState();
        String defaultAroma = defaultForState(state);
        if (defaultAroma == null) return;
        VanillaCropAromaSavedData data = VanillaCropAromaSavedData.get(level);
        data.get(event.getPos()).ifPresent(aroma -> {
            for (ItemEntity entity : event.getDrops()) {
                ItemStack drop = entity.getItem();
                if (isProduceDrop(state, drop)) drop.set(ModComponents.CROP_AROMA.get(), aroma);
            }
        });
        data.remove(event.getPos());
    }

    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        BlockState state = event.getState();
        // Produce positions are removed after BlockDropsEvent so their aroma is still
        // available while loot is built. Stems never produce aroma-bearing loot.
        if (state.is(Blocks.MELON_STEM) || state.is(Blocks.ATTACHED_MELON_STEM)) {
            VanillaCropAromaSavedData.get(level).remove(event.getPos());
        }
    }

    private static String plantingDefault(ItemStack stack) {
        if (stack.is(Items.POTATO)) return "potato";
        if (stack.is(Items.MELON_SEEDS)) return "melon";
        if (stack.is(Items.COCOA_BEANS)) return "cacao";
        return null;
    }

    private static String defaultForState(BlockState state) {
        if (state.is(Blocks.POTATOES)) return "potato";
        if (state.is(Blocks.MELON_STEM) || state.is(Blocks.ATTACHED_MELON_STEM)) return "melon";
        if (state.is(Blocks.COCOA)) return "cacao";
        if (state.is(Blocks.MELON)) return "melon";
        return null;
    }

    private static boolean matchesPlacedBlock(Item item, BlockState state) {
        return item == Items.POTATO && state.is(Blocks.POTATOES)
                || item == Items.MELON_SEEDS && state.is(Blocks.MELON_STEM)
                || item == Items.COCOA_BEANS && state.is(Blocks.COCOA);
    }

    private static boolean isProduceDrop(BlockState source, ItemStack drop) {
        if (source.is(Blocks.POTATOES)) return drop.is(Items.POTATO);
        if (source.is(Blocks.COCOA)) return drop.is(Items.COCOA_BEANS);
        if (source.is(Blocks.MELON)) return drop.is(Items.MELON_SLICE) || drop.is(Items.MELON);
        return false;
    }

    private static BlockPos[] horizontalNeighbors(BlockPos pos) {
        return new BlockPos[]{pos.north(), pos.south(), pos.east(), pos.west()};
    }

    private VanillaCropAromaEvents() {}
}
