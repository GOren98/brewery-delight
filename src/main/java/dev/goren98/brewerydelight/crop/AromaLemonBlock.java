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
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.CommonHooks;
import org.jetbrains.annotations.Nullable;

/** Fruits Delight DoubleFruitBushBlock semantics for lemon, with Aroma carried by both halves. */
public final class AromaLemonBlock extends DoublePlantBlock implements BonemealableBlock, EntityBlock {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_4;
    private static final int DOUBLE_BLOCK_START = 2;

    public AromaLemonBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(AGE, 0).setValue(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    public MapCodec<? extends DoublePlantBlock> codec() {
        return null;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(AGE);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER && state.getValue(AGE) < 4;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int age = state.getValue(AGE);
        if (age < 4 && level.getRawBrightness(pos.above(), 0) >= 9
                && CommonHooks.canCropGrow(level, pos, state, random.nextInt(5) == 0)) {
            setGrowth(level, pos, age + 1);
            CommonHooks.fireCropGrowPost(level, pos, state);
        }
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return state.getValue(AGE) < 4;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            pos = pos.below();
            state = level.getBlockState(pos);
            if (!state.is(this) || state.getValue(HALF) != DoubleBlockHalf.LOWER) return;
        }
        setGrowth(level, pos, Math.min(4, state.getValue(AGE) + 1));
    }

    private void setGrowth(Level level, BlockPos lowerPos, int requestedAge) {
        BlockState lowerState = level.getBlockState(lowerPos);
        String aroma = AromaPlantUtil.aromaAt(level, lowerPos, "lemon");
        if ("lemon".equals(aroma) && lowerState.is(this)) {
            aroma = AromaPlantUtil.aromaAt(level, lowerPos.above(), "lemon");
        }

        int age = requestedAge;
        if (age >= DOUBLE_BLOCK_START) {
            BlockState above = level.getBlockState(lowerPos.above());
            if (!above.is(this) && !above.canBeReplaced()) age = DOUBLE_BLOCK_START - 1;
        }

        BlockState nextLower = defaultBlockState().setValue(AGE, age).setValue(HALF, DoubleBlockHalf.LOWER);
        level.setBlock(lowerPos, nextLower, UPDATE_CLIENTS);
        AromaPlantUtil.setAroma(level, lowerPos, aroma);

        if (age >= DOUBLE_BLOCK_START) {
            BlockState nextUpper = defaultBlockState().setValue(AGE, age).setValue(HALF, DoubleBlockHalf.UPPER);
            level.setBlock(lowerPos.above(), nextUpper, UPDATE_CLIENTS);
            AromaPlantUtil.setAroma(level, lowerPos.above(), aroma);
        } else {
            BlockState above = level.getBlockState(lowerPos.above());
            if (above.is(this) && above.getValue(HALF) == DoubleBlockHalf.UPPER) {
                level.removeBlock(lowerPos.above(), false);
            }
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            pos = pos.below();
            state = level.getBlockState(pos);
        }
        if (!state.is(this) || state.getValue(HALF) != DoubleBlockHalf.LOWER || state.getValue(AGE) != 4) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            String aroma = AromaPlantUtil.aromaAt(level, pos, "lemon");
            popResource(level, pos, AromaPlantUtil.produce("lemon", aroma));
            setGrowth(level, pos, 2);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AromaCropBlockEntity(pos, state);
    }
}
