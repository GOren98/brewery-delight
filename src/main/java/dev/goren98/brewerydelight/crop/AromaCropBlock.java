package dev.goren98.brewerydelight.crop;

import dev.goren98.brewerydelight.BreweryDelight;
import dev.goren98.brewerydelight.registry.ModComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class AromaCropBlock extends CropBlock implements EntityBlock {
    public enum GrowthStyle { FIELD, BUSH, TALL, TREE }

    public static final BooleanProperty UPPER = BooleanProperty.create("upper");
    public static final BooleanProperty CROWN = BooleanProperty.create("crown");

    private final String cropId;
    private final String defaultAroma;
    private final GrowthStyle growthStyle;

    public AromaCropBlock(BlockBehaviour.Properties properties, String cropId, String defaultAroma, GrowthStyle growthStyle) {
        super(properties);
        this.cropId = cropId;
        this.defaultAroma = defaultAroma;
        this.growthStyle = growthStyle;
        this.registerDefaultState(this.defaultBlockState().setValue(UPPER, false).setValue(CROWN, false));
    }

    public GrowthStyle growthStyle() { return growthStyle; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(UPPER, CROWN);
    }

    @Override
    public int getMaxAge() {
        return switch (growthStyle) {
            case FIELD, TALL -> 7;
            case BUSH, TREE -> 3;
        };
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return switch (growthStyle) {
            case FIELD -> super.mayPlaceOn(state, level, pos);
            case BUSH, TALL -> state.is(net.minecraft.tags.BlockTags.DIRT) || super.mayPlaceOn(state, level, pos) || state.is(this);
            case TREE -> state.is(net.minecraft.tags.BlockTags.DIRT) || state.is(net.minecraft.tags.BlockTags.LOGS) || state.is(net.minecraft.tags.BlockTags.LEAVES);
        };
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (growthStyle == GrowthStyle.TREE && state.getValue(CROWN)) return true;
        if (growthStyle == GrowthStyle.TALL && state.getValue(UPPER)) return level.getBlockState(pos.below()).is(this);
        return super.canSurvive(state, level, pos);
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(BreweryDelight.MOD_ID, cropId + "_seeds"));
    }

    private Item produceItem() {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(BreweryDelight.MOD_ID, cropId));
    }

    private String aromaAt(LevelReader level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        return be instanceof AromaCropBlockEntity crop ? crop.getAroma(defaultAroma) : defaultAroma;
    }

    private void setAroma(Level level, BlockPos pos, String aroma) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof AromaCropBlockEntity crop) crop.setAroma(aroma);
    }

    private ItemStack produceStack(int count, String aroma) {
        ItemStack stack = new ItemStack(produceItem(), count);
        if (!defaultAroma.equals(aroma)) stack.set(ModComponents.CROP_AROMA.get(), aroma);
        return stack;
    }

    private boolean isTallUpper(BlockState state) {
        return growthStyle == GrowthStyle.TALL && state.getValue(UPPER);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (isTallUpper(state)) return;
        super.randomTick(state, level, pos, random);
        BlockState after = level.getBlockState(pos);
        if (after.is(this)) afterGrowth(level, pos, after);
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        if (isTallUpper(state)) return;
        super.performBonemeal(level, random, pos, state);
        BlockState after = level.getBlockState(pos);
        if (after.is(this)) afterGrowth(level, pos, after);
    }

    private void afterGrowth(Level level, BlockPos pos, BlockState state) {
        if (growthStyle == GrowthStyle.TREE && !state.getValue(CROWN) && getAge(state) >= 1) {
            tryGrowTree(level, pos);
            return;
        }
        if (growthStyle == GrowthStyle.TALL) syncTallTop(level, pos, state);
    }

    private BlockState stateFor(int age, boolean upper, boolean crown) {
        return getStateForAge(age).setValue(UPPER, upper).setValue(CROWN, crown);
    }

    private void syncTallTop(Level level, BlockPos pos, BlockState state) {
        if (isTallUpper(state)) return;
        int age = getAge(state);
        BlockPos top = pos.above();
        if (age >= 4) {
            String aroma = aromaAt(level, pos);
            BlockState topState = level.getBlockState(top);
            if (topState.isAir() || topState.is(this) || topState.canBeReplaced()) {
                level.setBlock(top, stateFor(age, true, false), Block.UPDATE_CLIENTS);
                setAroma(level, top, aroma);
                level.setBlock(pos, stateFor(age, false, false), Block.UPDATE_CLIENTS);
                setAroma(level, pos, aroma);
            }
        } else if (level.getBlockState(top).is(this) && level.getBlockState(top).getValue(UPPER)) {
            level.removeBlock(top, false);
        }
    }

    private void tryGrowTree(Level level, BlockPos pos) {
        if (!level.getBlockState(pos.below()).is(net.minecraft.tags.BlockTags.DIRT)) return;
        final int trunkHeight = 5;
        for (int y = 1; y <= trunkHeight + 3; y++) {
            BlockState check = level.getBlockState(pos.above(y));
            if (!check.isAir() && !check.canBeReplaced()) return;
        }
        String aroma = aromaAt(level, pos);
        BlockState log = Blocks.OAK_LOG.defaultBlockState();
        BlockState leaves = Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);

        for (int y = 0; y < trunkHeight; y++) level.setBlock(pos.above(y), log, Block.UPDATE_CLIENTS);

        BlockPos center = pos.above(trunkHeight);
        int fruitIndex = 0;
        for (int dy = -1; dy <= 1; dy++) {
            int radius = dy == 1 ? 1 : 2;
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.abs(dx) == radius && Math.abs(dz) == radius && radius == 2) continue;
                    BlockPos leafPos = center.offset(dx, dy, dz);
                    BlockState old = level.getBlockState(leafPos);
                    if (!old.isAir() && !old.canBeReplaced()) continue;

                    boolean fruitLeaf = dy <= 0 && (Math.abs(dx) + Math.abs(dz) >= 2) && ((fruitIndex++ & 1) == 0);
                    if (fruitLeaf) {
                        level.setBlock(leafPos, stateFor(2, false, true), Block.UPDATE_CLIENTS);
                        setAroma(level, leafPos, aroma);
                    } else {
                        level.setBlock(leafPos, leaves, Block.UPDATE_CLIENTS);
                    }
                }
            }
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        BlockPos harvestPos = pos;
        BlockState harvestState = state;
        if (growthStyle == GrowthStyle.TALL && isTallUpper(state)) {
            harvestPos = pos.below();
            harvestState = level.getBlockState(harvestPos);
        }
        if (!harvestState.is(this) || getAge(harvestState) < getMaxAge()) return InteractionResult.PASS;

        if (!level.isClientSide) {
            String aroma = aromaAt(level, harvestPos);
            int resetAge = switch (growthStyle) {
                case FIELD -> 0;
                case BUSH -> 1;
                case TALL -> 4;
                case TREE -> 1;
            };
            boolean crown = growthStyle == GrowthStyle.TREE && harvestState.getValue(CROWN);
            level.setBlock(harvestPos, stateFor(resetAge, false, crown), Block.UPDATE_CLIENTS);
            setAroma(level, harvestPos, aroma);

            if (growthStyle == GrowthStyle.TALL) {
                BlockPos top = harvestPos.above();
                level.setBlock(top, stateFor(resetAge, true, false), Block.UPDATE_CLIENTS);
                setAroma(level, top, aroma);
            }

            popResource(level, harvestPos, produceStack(1, aroma));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AromaCropBlockEntity(pos, state);
    }
}
