package dev.goren98.brewerydelight.crop;

import com.mojang.serialization.MapCodec;
import dev.goren98.brewerydelight.BreweryDelight;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/** Berries & Cherries blueberry two-block bush: empty <-> fruiting, harvest returns to empty. */
public final class AromaBerryPartBlock extends BushBlock implements BonemealableBlock, EntityBlock {
    private final boolean top;
    private final boolean fruiting;

    public AromaBerryPartBlock(BlockBehaviour.Properties properties, boolean top, boolean fruiting) {
        super(properties);
        this.top = top;
        this.fruiting = fruiting;
    }

    @Override protected MapCodec<? extends BushBlock> codec() { return null; }
    private Block block(String suffix) {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(BreweryDelight.MOD_ID, "berry" + suffix));
    }
    private BlockPos base(BlockPos pos) { return top ? pos.below() : pos; }
    private Block expectedBottom() { return block(fruiting ? "_fruiting_bottom" : "_empty_bottom"); }

    @Override public boolean isRandomlyTicking(BlockState state) { return !fruiting; }
    @Override protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!fruiting && random.nextInt(5) == 0) setPair(level, base(pos), true);
    }
    @Override public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) { return !fruiting; }
    @Override public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) { return !fruiting; }
    @Override public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        if (!fruiting) setPair(level, base(pos), true);
    }

    private void setPair(Level level, BlockPos base, boolean withFruit) {
        String aroma = AromaPlantUtil.aromaAt(level, base, "berry");
        if ("berry".equals(aroma)) aroma = AromaPlantUtil.aromaAt(level, base.above(), "berry");
        level.setBlock(base, block(withFruit ? "_fruiting_bottom" : "_empty_bottom").defaultBlockState(), UPDATE_CLIENTS);
        level.setBlock(base.above(), block(withFruit ? "_fruiting_top" : "_empty_top").defaultBlockState(), UPDATE_CLIENTS);
        AromaPlantUtil.setAroma(level, base, aroma);
        AromaPlantUtil.setAroma(level, base.above(), aroma);
    }

    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!fruiting) return InteractionResult.PASS;
        if (!level.isClientSide) {
            BlockPos base = base(pos);
            String aroma = AromaPlantUtil.aromaAt(level, base, "berry");
            popResource(level, base, AromaPlantUtil.produce("berry", aroma));
            setPair(level, base, false);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (top) return level.getBlockState(pos.below()).is(expectedBottom());
        return super.canSurvive(state, level, pos);
    }

    @Nullable @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new AromaCropBlockEntity(pos, state); }
}
