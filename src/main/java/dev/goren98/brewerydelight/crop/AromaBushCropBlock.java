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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.CommonHooks;
import org.jetbrains.annotations.Nullable;

/** Fruits Delight BaseBushBlock/FruitBushBlock lifecycle. */
public class AromaBushCropBlock extends BushBlock implements BonemealableBlock, EntityBlock {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_4;
    private final String cropId;

    public AromaBushCropBlock(BlockBehaviour.Properties properties, String cropId) {
        super(properties);
        this.cropId = cropId;
        registerDefaultState(defaultBlockState().setValue(AGE, 0));
    }

    @Override protected MapCodec<? extends BushBlock> codec() { return null; }
    @Override public boolean isRandomlyTicking(BlockState state) { return state.getValue(AGE) < 4; }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int age = state.getValue(AGE);
        if (age < 4 && level.getRawBrightness(pos.above(), 0) >= 9 && CommonHooks.canCropGrow(level, pos, state, random.nextInt(5) == 0)) {
            BlockState next = state.setValue(AGE, age + 1);
            level.setBlock(pos, next, UPDATE_CLIENTS);
            CommonHooks.fireCropGrowPost(level, pos, state);
        }
    }

    @Override public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) { return state.getValue(AGE) < 4; }
    @Override public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) { return true; }
    @Override public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) { level.setBlock(pos, state.setValue(AGE, Math.min(4, state.getValue(AGE) + 1)), UPDATE_CLIENTS); }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (state.getValue(AGE) < 4) return InteractionResult.PASS;
        if (!level.isClientSide) {
            String aroma = AromaPlantUtil.aromaAt(level, pos, cropId);
            level.setBlock(pos, state.setValue(AGE, 2), UPDATE_CLIENTS);
            AromaPlantUtil.setAroma(level, pos, aroma);
            popResource(level, pos, AromaPlantUtil.produce(cropId, aroma));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) { super.createBlockStateDefinition(builder); builder.add(AGE); }
    @Nullable @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new AromaCropBlockEntity(pos, state); }
}
