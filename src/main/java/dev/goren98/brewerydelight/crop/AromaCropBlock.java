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
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class AromaCropBlock extends CropBlock implements EntityBlock {
    public enum GrowthStyle { FIELD, BUSH, TALL, TREE }

    private final String cropId;
    private final String defaultAroma;
    private final GrowthStyle growthStyle;

    public AromaCropBlock(BlockBehaviour.Properties properties, String cropId, String defaultAroma, GrowthStyle growthStyle) {
        super(properties);
        this.cropId = cropId;
        this.defaultAroma = defaultAroma;
        this.growthStyle = growthStyle;
    }

    public GrowthStyle growthStyle() {
        return growthStyle;
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
            case BUSH -> state.is(net.minecraft.tags.BlockTags.DIRT) || super.mayPlaceOn(state, level, pos);
            case TALL -> state.is(net.minecraft.tags.BlockTags.DIRT) || state.is(this) || super.mayPlaceOn(state, level, pos);
            case TREE -> state.is(net.minecraft.tags.BlockTags.DIRT) || state.is(net.minecraft.tags.BlockTags.LOGS);
        };
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

    private ItemStack withAroma(Item item, int count, String aroma) {
        ItemStack stack = new ItemStack(item, count);
        stack.set(ModComponents.CROP_AROMA.get(), aroma);
        return stack;
    }

    private boolean isTallUpper(LevelReader level, BlockPos pos) {
        return growthStyle == GrowthStyle.TALL && level.getBlockState(pos.below()).is(this);
    }

    private boolean isTreeCrown(LevelReader level, BlockPos pos) {
        return growthStyle == GrowthStyle.TREE && level.getBlockState(pos.below()).is(net.minecraft.tags.BlockTags.LOGS);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (isTallUpper(level, pos)) return;
        super.randomTick(state, level, pos, random);
        BlockState after = level.getBlockState(pos);
        if (after.is(this)) afterGrowth(level, pos, after);
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        if (isTallUpper(level, pos)) return;
        super.performBonemeal(level, random, pos, state);
        BlockState after = level.getBlockState(pos);
        if (after.is(this)) afterGrowth(level, pos, after);
    }

    private void afterGrowth(Level level, BlockPos pos, BlockState state) {
        if (growthStyle == GrowthStyle.TREE && !isTreeCrown(level, pos) && getAge(state) >= 1) {
            tryGrowTree(level, pos);
            return;
        }
        if (growthStyle == GrowthStyle.TALL) syncTallTop(level, pos, state);
    }

    private void syncTallTop(Level level, BlockPos pos, BlockState state) {
        if (isTallUpper(level, pos)) return;
        int age = getAge(state);
        BlockPos top = pos.above();
        if (age >= 4) {
            BlockState topState = level.getBlockState(top);
            String aroma = aromaAt(level, pos);
            if (topState.isAir() || topState.is(this)) {
                level.setBlock(top, getStateForAge(age), Block.UPDATE_CLIENTS);
                setAroma(level, top, aroma);
            }
        } else if (level.getBlockState(top).is(this)) {
            level.removeBlock(top, false);
        }
    }

    private void tryGrowTree(Level level, BlockPos pos) {
        if (!level.getBlockState(pos.below()).is(net.minecraft.tags.BlockTags.DIRT)) return;
        for (int y = 1; y <= 4; y++) {
            BlockState check = level.getBlockState(pos.above(y));
            if (!check.isAir() && !check.canBeReplaced()) return;
        }

        String aroma = aromaAt(level, pos);
        BlockState log = Blocks.OAK_LOG.defaultBlockState();
        BlockState leaves = Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
        for (int y = 0; y <= 2; y++) level.setBlock(pos.above(y), log, Block.UPDATE_CLIENTS);

        BlockPos crown = pos.above(3);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue;
                BlockPos leafPos = crown.offset(dx, 0, dz);
                if (level.getBlockState(leafPos).isAir() || level.getBlockState(leafPos).canBeReplaced()) {
                    level.setBlock(leafPos, leaves, Block.UPDATE_CLIENTS);
                }
            }
        }
        for (BlockPos leafPos : new BlockPos[]{crown.above(), crown.above().north(), crown.above().south(), crown.above().east(), crown.above().west()}) {
            if (level.getBlockState(leafPos).isAir() || level.getBlockState(leafPos).canBeReplaced()) {
                level.setBlock(leafPos, leaves, Block.UPDATE_CLIENTS);
            }
        }
        level.setBlock(crown, getStateForAge(2), Block.UPDATE_CLIENTS);
        setAroma(level, crown, aroma);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        BlockPos harvestPos = pos;
        BlockState harvestState = state;
        if (growthStyle == GrowthStyle.TALL && isTallUpper(level, pos)) {
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
                case TREE -> 2;
            };
            level.setBlock(harvestPos, getStateForAge(resetAge), Block.UPDATE_CLIENTS);
            setAroma(level, harvestPos, aroma);

            if (growthStyle == GrowthStyle.TALL) {
                BlockPos top = harvestPos.above();
                level.setBlock(top, getStateForAge(resetAge), Block.UPDATE_CLIENTS);
                setAroma(level, top, aroma);
            }

            popResource(level, harvestPos, withAroma(produceItem(), 2, aroma));
            popResource(level, harvestPos, withAroma(getBaseSeedId().asItem(), 1, aroma));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AromaCropBlockEntity(pos, state);
    }
}
