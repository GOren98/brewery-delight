package dev.goren98.brewerydelight.crop;

import dev.goren98.brewerydelight.registry.ModComponents;
import dev.goren98.brewerydelight.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class AromaPlantUtil {
    public static String aromaAt(LevelReader level, BlockPos pos, String fallback) {
        BlockEntity be = level.getBlockEntity(pos);
        return be instanceof AromaCropBlockEntity crop ? crop.getAroma(fallback) : fallback;
    }

    public static void setAroma(net.minecraft.world.level.Level level, BlockPos pos, String aroma) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof AromaCropBlockEntity crop) crop.setAroma(aroma);
    }

    public static ItemStack produce(String cropId, String aroma) {
        ItemStack out = new ItemStack(ModItems.CROP_ITEMS.get(cropId).get());
        if (!cropId.equals(aroma)) out.set(ModComponents.CROP_AROMA.get(), aroma);
        return out;
    }

    private AromaPlantUtil() {}
}
