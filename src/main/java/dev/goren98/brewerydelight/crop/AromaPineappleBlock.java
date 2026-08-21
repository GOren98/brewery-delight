package dev.goren98.brewerydelight.crop;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Fruits Delight PineappleBlock behavior: 5 growth states, sand-compatible planting, plant consumed on harvest. */
public final class AromaPineappleBlock extends AromaBushCropBlock {
    public AromaPineappleBlock(BlockBehaviour.Properties properties) { super(properties, "pineapple"); }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return super.mayPlaceOn(state, level, pos) || state.is(BlockTags.SAND);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (state.getValue(AGE) < 4) return InteractionResult.PASS;
        if (!level.isClientSide) {
            String aroma = AromaPlantUtil.aromaAt(level, pos, "pineapple");
            popResource(level, pos, AromaPlantUtil.produce("pineapple", aroma));
            level.removeBlock(pos, false);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
