package dev.goren98.brewerydelight.crop;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.neoforged.neoforge.common.CommonHooks;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/** Fruits Delight PeachLeavesBlock lifecycle with Brewery Delight Aroma preservation. */
public final class AromaPeachLeavesBlock extends AromaFruitLeavesBlock {
    public static final BooleanProperty FERTILE = BooleanProperty.create("fertile");

    private static final double PEACH_GROW_CHANCE = 0.10D;
    private static final double PEACH_FRUIT_CHANCE = 0.30D;
    private static final double FLOWER_DECAY_CHANCE = 0.10D;
    private static final double FRUIT_DROP_CHANCE = 0.10D;

    public AromaPeachLeavesBlock(BlockBehaviour.Properties properties) {
        super(properties, "peach");
        registerDefaultState(defaultBlockState().setValue(FERTILE, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FERTILE);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        if (state.getValue(PERSISTENT)) return false;
        if (state.getValue(TYPE) == FruitState.FRUITS) return true;
        if (state.getValue(FERTILE) && state.getValue(DISTANCE) == 1) return true;
        return state.getValue(DISTANCE) == 7;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(PERSISTENT) || state.getValue(DISTANCE) == 7) {
            super.randomTick(state, level, pos, random);
            return;
        }

        FruitState type = state.getValue(TYPE);
        if (type == FruitState.LEAVES && state.getValue(FERTILE) && state.getValue(DISTANCE) == 1) {
            boolean grow = random.nextDouble() < PEACH_GROW_CHANCE;
            if (!CommonHooks.canCropGrow(level, pos, state, grow)) return;

            for (BlockPos leafPos : scanLeaves(level, pos)) {
                BlockState leaf = level.getBlockState(leafPos);
                if (leaf.is(this)) {
                    setStateKeepingAroma(level, leafPos, leaf.setValue(TYPE, FruitState.FLOWERS));
                }
            }
            CommonHooks.fireCropGrowPost(level, pos, state);
            return;
        }

        if (type == FruitState.FLOWERS) {
            boolean grow = random.nextDouble() < PEACH_GROW_CHANCE;
            if (!CommonHooks.canCropGrow(level, pos, state, grow)) return;

            for (BlockPos leafPos : scanLeaves(level, pos)) {
                BlockState leaf = level.getBlockState(leafPos);
                if (!leaf.is(this)) continue;

                if (leaf.getValue(TYPE) == FruitState.FRUITS) {
                    dropFruit(level, leafPos);
                }

                BlockState next = leaf.setValue(TYPE,
                        random.nextDouble() < PEACH_FRUIT_CHANCE ? FruitState.FRUITS : FruitState.LEAVES);

                if (next.getValue(DISTANCE) > 1
                        || (leafPos.equals(pos) && random.nextDouble() < FLOWER_DECAY_CHANCE)) {
                    next = next.setValue(FERTILE, false);
                }
                setStateKeepingAroma(level, leafPos, next);
            }
            CommonHooks.fireCropGrowPost(level, pos, state);
            return;
        }

        if (type == FruitState.FRUITS && random.nextDouble() < FRUIT_DROP_CHANCE) {
            dropFruit(level, pos);
            BlockState current = level.getBlockState(pos);
            if (current.is(this)) {
                setStateKeepingAroma(level, pos, current.setValue(TYPE, FruitState.LEAVES));
            }
        }
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return state.getValue(PERSISTENT);
    }

    private void dropFruit(ServerLevel level, BlockPos pos) {
        String aroma = AromaPlantUtil.aromaAt(level, pos, "peach");
        popResource(level, pos, AromaPlantUtil.produce("peach", aroma));
    }

    private void setStateKeepingAroma(Level level, BlockPos pos, BlockState next) {
        String aroma = AromaPlantUtil.aromaAt(level, pos, "peach");
        level.setBlock(pos, next, UPDATE_CLIENTS);
        AromaPlantUtil.setAroma(level, pos, aroma);
    }

    private List<BlockPos> scanLeaves(Level level, BlockPos start) {
        List<BlockPos> result = new ArrayList<>();
        BlockState startState = level.getBlockState(start);
        if (!startState.is(this) || startState.getValue(DISTANCE) > 1) return result;

        BlockPos logPos = start.below();
        if (!level.getBlockState(logPos).is(BlockTags.LOGS)) return result;

        record Node(int distance, BlockPos pos) {}
        Set<BlockPos> visited = new HashSet<>();
        Queue<Node> queue = new ArrayDeque<>();
        queue.add(new Node(-1, logPos));

        while (!queue.isEmpty()) {
            Node node = queue.poll();
            for (Direction direction : Direction.values()) {
                BlockPos nextPos = node.pos().relative(direction);
                if (!visited.add(nextPos)) continue;

                BlockState next = level.getBlockState(nextPos);
                if (!next.is(this) || next.getValue(PERSISTENT)) continue;

                int distance = next.getValue(DISTANCE);
                if (distance < node.distance()) continue;

                queue.add(new Node(distance, nextPos));
                result.add(nextPos);
            }
        }
        return result;
    }
}
