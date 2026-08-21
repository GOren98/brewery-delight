package dev.goren98.brewerydelight.crop;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Fruits Delight/Croptopia SaplingBlock pattern. TreeGrower uses data-driven configured tree features. */
public final class AromaFruitSaplingBlock extends SaplingBlock implements EntityBlock {
    private final String cropId;

    public AromaFruitSaplingBlock(TreeGrower treeGrower, BlockBehaviour.Properties properties, String cropId) {
        super(treeGrower, properties);
        this.cropId = cropId;
    }

    @Override public void advanceTree(ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
        String aroma = AromaPlantUtil.aromaAt(level, pos, cropId);
        super.advanceTree(level, pos, state, random);
        if (!level.getBlockState(pos).is(this)) {
            BlockPos.betweenClosedStream(pos.offset(-5, 0, -5), pos.offset(5, 10, 5)).forEach(p -> {
                if (level.getBlockState(p).getBlock() instanceof AromaFruitLeavesBlock leaves && leaves.cropId().equals(cropId)) {
                    AromaPlantUtil.setAroma(level, p, aroma);
                }
            });
        }
    }

    @Nullable @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new AromaCropBlockEntity(pos, state); }
}
