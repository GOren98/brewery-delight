package dev.goren98.brewerydelight.crop;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
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
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.CommonHooks;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/** Fruits Delight PassableLeavesBlock lifecycle, with Aroma carried across state changes. */
public class AromaFruitLeavesBlock extends LeavesBlock implements BonemealableBlock, EntityBlock {
    public enum FruitState implements StringRepresentable {
        LEAVES("leaves"), FLOWERS("flowers"), FRUITS("fruits");
        private final String name;
        FruitState(String name) { this.name = name; }
        @Override public String getSerializedName() { return name; }
    }

    public static final EnumProperty<FruitState> TYPE = EnumProperty.create("type", FruitState.class);
    private static final double GROW_CHANCE = 0.10D;
    private static final double DROP_CHANCE = 0.10D;
    private static final double FLOWER_SPREAD_CHANCE = 0.10D;

    private final String cropId;

    public AromaFruitLeavesBlock(BlockBehaviour.Properties properties, String cropId) {
        super(properties);
        this.cropId = cropId;
        registerDefaultState(defaultBlockState().setValue(TYPE, FruitState.LEAVES));
    }

    public String cropId() { return cropId; }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        if (state.getValue(PERSISTENT)) return false;
        return state.getValue(TYPE) != FruitState.LEAVES || super.isRandomlyTicking(state);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(state, level, pos, random);
        BlockState current = level.getBlockState(pos);
        if (!current.is(this) || current.getValue(PERSISTENT)) return;

        FruitState type = current.getValue(TYPE);
        if (type == FruitState.FLOWERS) {
            boolean grow = random.nextDouble() < GROW_CHANCE;
            if (CommonHooks.canCropGrow(level, pos, current, grow)) {
                String aroma = AromaPlantUtil.aromaAt(level, pos, cropId);
                level.setBlock(pos, current.setValue(TYPE, FruitState.FRUITS), UPDATE_CLIENTS);
                AromaPlantUtil.setAroma(level, pos, aroma);
                spreadFlower(level, pos, random);
                CommonHooks.fireCropGrowPost(level, pos, current);
            }
        } else if (type == FruitState.FRUITS && random.nextDouble() < DROP_CHANCE) {
            String aroma = AromaPlantUtil.aromaAt(level, pos, cropId);
            popResource(level, pos, AromaPlantUtil.produce(cropId, aroma));
            level.setBlock(pos, current.setValue(TYPE, FruitState.LEAVES), UPDATE_CLIENTS);
            AromaPlantUtil.setAroma(level, pos, aroma);
        }
    }

    private void spreadFlower(ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextDouble() >= FLOWER_SPREAD_CHANCE) return;

        List<BlockPos> candidates = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            BlockPos target = pos.relative(direction);
            BlockState targetState = level.getBlockState(target);
            if (!targetState.is(this) || targetState.getValue(PERSISTENT)
                    || targetState.getValue(TYPE) != FruitState.LEAVES) continue;
            int weight = targetState.getValue(DISTANCE) + 2;
            for (int i = 0; i < weight; i++) candidates.add(target);
        }

        if (candidates.isEmpty()) return;
        BlockPos target = candidates.get(random.nextInt(candidates.size()));
        BlockState targetState = level.getBlockState(target);
        String aroma = AromaPlantUtil.aromaAt(level, target, cropId);
        level.setBlock(target, targetState.setValue(TYPE, FruitState.FLOWERS), UPDATE_CLIENTS);
        AromaPlantUtil.setAroma(level, target, aroma);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return state.getValue(PERSISTENT) || state.getValue(TYPE) == FruitState.FLOWERS;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        String aroma = AromaPlantUtil.aromaAt(level, pos, cropId);
        level.setBlock(pos, state.cycle(TYPE), UPDATE_CLIENTS);
        AromaPlantUtil.setAroma(level, pos, aroma);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (state.getValue(TYPE) != FruitState.FRUITS || state.getValue(PERSISTENT)) return InteractionResult.PASS;
        if (!level.isClientSide) {
            String aroma = AromaPlantUtil.aromaAt(level, pos, cropId);
            popResource(level, pos, AromaPlantUtil.produce(cropId, aroma));
            level.setBlock(pos, state.setValue(TYPE, FruitState.LEAVES), UPDATE_CLIENTS);
            AromaPlantUtil.setAroma(level, pos, aroma);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(TYPE);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AromaCropBlockEntity(pos, state);
    }
}
