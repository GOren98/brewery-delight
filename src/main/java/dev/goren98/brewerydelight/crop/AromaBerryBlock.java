package dev.goren98.brewerydelight.crop;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/** Berries & Cherries strawberry lifecycle: sapling -> empty bush -> fruiting bush -> empty bush after harvest. */
public final class AromaBerryBlock extends BushBlock implements BonemealableBlock, EntityBlock {
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 2);

    public AromaBerryBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(AGE, 0));
    }

    @Override protected MapCodec<? extends BushBlock> codec() { return null; }
    @Override public boolean isRandomlyTicking(BlockState state) { return state.getValue(AGE) < 2; }
    @Override protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(AGE) < 2 && random.nextInt(5) == 0) level.setBlock(pos, state.setValue(AGE, state.getValue(AGE) + 1), UPDATE_CLIENTS);
    }
    @Override public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) { return state.getValue(AGE) < 2; }
    @Override public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) { return true; }
    @Override public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) { level.setBlock(pos, state.setValue(AGE, Math.min(2, state.getValue(AGE) + 1)), UPDATE_CLIENTS); }

    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (state.getValue(AGE) != 2) return InteractionResult.PASS;
        if (!level.isClientSide) {
            String aroma = AromaPlantUtil.aromaAt(level, pos, "berry");
            level.setBlock(pos, state.setValue(AGE, 1), UPDATE_CLIENTS);
            AromaPlantUtil.setAroma(level, pos, aroma);
            popResource(level, pos, AromaPlantUtil.produce("berry", aroma));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) { super.createBlockStateDefinition(builder); builder.add(AGE); }
    @Nullable @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new AromaCropBlockEntity(pos, state); }
}
