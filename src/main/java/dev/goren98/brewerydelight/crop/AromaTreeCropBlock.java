package dev.goren98.brewerydelight.crop;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/** Croptopia LeafCropBlock semantics adapted only to carry Brewery Delight Aroma. */
public final class AromaTreeCropBlock extends LeavesBlock implements BonemealableBlock, EntityBlock {
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 3);
    private final String cropId;

    public AromaTreeCropBlock(BlockBehaviour.Properties properties, String cropId) {
        super(properties);
        this.cropId = cropId;
        registerDefaultState(defaultBlockState().setValue(AGE, 0));
    }

    public String cropId() { return cropId; }

    @Override public boolean isRandomlyTicking(BlockState state) {
        return super.isRandomlyTicking(state) || (!state.getValue(PERSISTENT) && state.getValue(AGE) < 3);
    }

    @Override protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(state, level, pos, random);
        BlockState current = level.getBlockState(pos);
        if (!current.is(this)) return;
        if (!current.getValue(PERSISTENT) && current.getValue(AGE) < 3 && level.getRawBrightness(pos, 0) >= 9 && random.nextInt(20) == 0) {
            level.setBlock(pos, current.setValue(AGE, current.getValue(AGE) + 1), UPDATE_CLIENTS);
        }
    }

    @Override public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return !state.getValue(PERSISTENT) && state.getValue(AGE) < 3;
    }
    @Override public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) { return true; }
    @Override public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        if (!state.getValue(PERSISTENT) && state.getValue(AGE) < 3)
            level.setBlock(pos, state.setValue(AGE, state.getValue(AGE) + 1), UPDATE_CLIENTS);
    }

    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (state.getValue(AGE) != 3 || state.getValue(PERSISTENT)) return InteractionResult.PASS;
        if (!level.isClientSide) {
            String aroma = AromaPlantUtil.aromaAt(level, pos, cropId);
            popResource(level, pos, AromaPlantUtil.produce(cropId, aroma));
            level.setBlock(pos, state.setValue(AGE, 0), UPDATE_CLIENTS);
            AromaPlantUtil.setAroma(level, pos, aroma);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(AGE);
    }

    @Nullable @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new AromaCropBlockEntity(pos, state); }
}
