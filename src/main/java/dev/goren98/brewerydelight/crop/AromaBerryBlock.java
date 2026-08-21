package dev.goren98.brewerydelight.crop;

import com.mojang.serialization.MapCodec;
import dev.goren98.brewerydelight.BreweryDelight;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Berries & Cherries blueberry sapling lifecycle: sapling -> empty two-block bush. */
public final class AromaBerryBlock extends BushBlock implements BonemealableBlock, EntityBlock {
    public AromaBerryBlock(BlockBehaviour.Properties properties) { super(properties); }
    @Override protected MapCodec<? extends BushBlock> codec() { return null; }

    private Block block(String suffix) {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(BreweryDelight.MOD_ID, "berry" + suffix));
    }

    @Override public boolean isRandomlyTicking(BlockState state) { return true; }
    @Override protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(5) == 0) grow(level, pos);
    }
    @Override public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) { return level.getBlockState(pos.above()).canBeReplaced(); }
    @Override public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) { return true; }
    @Override public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) { grow(level, pos); }

    private void grow(Level level, BlockPos pos) {
        if (!level.getBlockState(pos.above()).canBeReplaced()) return;
        String aroma = AromaPlantUtil.aromaAt(level, pos, "berry");
        level.setBlock(pos, block("_empty_bottom").defaultBlockState(), UPDATE_CLIENTS);
        level.setBlock(pos.above(), block("_empty_top").defaultBlockState(), UPDATE_CLIENTS);
        AromaPlantUtil.setAroma(level, pos, aroma);
        AromaPlantUtil.setAroma(level, pos.above(), aroma);
    }

    @Nullable @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new AromaCropBlockEntity(pos, state); }
}
