package dev.goren98.brewerydelight.crop;

import net.minecraft.core.BlockPos;
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
import org.jetbrains.annotations.Nullable;

/** Fruits Delight PassableLeavesBlock lifecycle: leaves -> flowers -> fruits -> flowers after harvest. */
public class AromaFruitLeavesBlock extends LeavesBlock implements BonemealableBlock, EntityBlock {
    public enum FruitState implements StringRepresentable {
        LEAVES("leaves"), FLOWERS("flowers"), FRUITS("fruits");
        private final String name; FruitState(String name) { this.name = name; }
        @Override public String getSerializedName() { return name; }
    }
    public static final EnumProperty<FruitState> TYPE = EnumProperty.create("type", FruitState.class);
    private final String cropId;

    public AromaFruitLeavesBlock(BlockBehaviour.Properties properties, String cropId) {
        super(properties);
        this.cropId = cropId;
        registerDefaultState(defaultBlockState().setValue(TYPE, FruitState.LEAVES));
    }
    public String cropId() { return cropId; }

    @Override public boolean isRandomlyTicking(BlockState state) { return super.isRandomlyTicking(state) || state.getValue(TYPE) != FruitState.FRUITS; }
    @Override protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(state, level, pos, random);
        if (!level.getBlockState(pos).is(this)) return;
        FruitState type = state.getValue(TYPE);
        if (type == FruitState.LEAVES && random.nextInt(12) == 0) level.setBlock(pos, state.setValue(TYPE, FruitState.FLOWERS), UPDATE_CLIENTS);
        else if (type == FruitState.FLOWERS && random.nextInt(8) == 0) level.setBlock(pos, state.setValue(TYPE, FruitState.FRUITS), UPDATE_CLIENTS);
    }

    @Override public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) { return state.getValue(TYPE) != FruitState.FRUITS; }
    @Override public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) { return true; }
    @Override public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        FruitState next = state.getValue(TYPE) == FruitState.LEAVES ? FruitState.FLOWERS : FruitState.FRUITS;
        level.setBlock(pos, state.setValue(TYPE, next), UPDATE_CLIENTS);
    }

    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (state.getValue(TYPE) != FruitState.FRUITS) return InteractionResult.PASS;
        if (!level.isClientSide) {
            String aroma = AromaPlantUtil.aromaAt(level, pos, cropId);
            popResource(level, pos, AromaPlantUtil.produce(cropId, aroma));
            level.setBlock(pos, state.setValue(TYPE, FruitState.FLOWERS), UPDATE_CLIENTS);
            AromaPlantUtil.setAroma(level, pos, aroma);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) { super.createBlockStateDefinition(builder); builder.add(TYPE); }
    @Nullable @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new AromaCropBlockEntity(pos, state); }
}
