package dev.goren98.brewerydelight.crop;

import dev.goren98.brewerydelight.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/** CroptopiaCropBlock semantics: vanilla 0..7 crop, dirt/sand/farmland placement, mature right-click reset. */
public class AromaFieldCropBlock extends CropBlock implements EntityBlock {
    private final String cropId;

    public AromaFieldCropBlock(BlockBehaviour.Properties properties, String cropId) {
        super(properties);
        this.cropId = cropId;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return super.mayPlaceOn(state, level, pos) || state.is(BlockTags.DIRT) || state.is(BlockTags.SAND);
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return ModItems.SEEDS.get(cropId).get();
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!isMaxAge(state)) return InteractionResult.PASS;
        if (!level.isClientSide) {
            String aroma = AromaPlantUtil.aromaAt(level, pos, cropId);
            level.setBlock(pos, getStateForAge(0), UPDATE_CLIENTS);
            AromaPlantUtil.setAroma(level, pos, aroma);
            popResource(level, pos, AromaPlantUtil.produce(cropId, aroma));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AromaCropBlockEntity(pos, state);
    }
}
