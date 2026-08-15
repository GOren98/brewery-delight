package dev.goren98.brewerydelight.barrel;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;

/**
 * Small Barrel: a 2x2x2 body of eight stairs. The stairs must form the barrel/cross silhouette:
 * bottom layer stairs are BOTTOM-half and face outward; top layer stairs are TOP-half and face outward.
 * The pattern may be rotated freely in the world.
 */
public final class SmallBarrelPattern {
    public static BlockPos findController(Level level, BlockPos signPos) {
        for (int dx = -2; dx <= 1; dx++) for (int dy = -1; dy <= 0; dy++) for (int dz = -2; dz <= 1; dz++) {
            BlockPos origin = signPos.offset(dx, dy, dz);
            if (isBody(level, origin)) return origin;
        }
        return null;
    }

    public static boolean isBody(Level level, BlockPos origin) {
        for (int x = 0; x < 2; x++) for (int y = 0; y < 2; y++) for (int z = 0; z < 2; z++) {
            BlockState state = level.getBlockState(origin.offset(x, y, z));
            if (!(state.getBlock() instanceof StairBlock)) return false;
            if (state.getValue(StairBlock.SHAPE) != StairsShape.STRAIGHT) return false;
            Half expectedHalf = y == 0 ? Half.BOTTOM : Half.TOP;
            if (state.getValue(StairBlock.HALF) != expectedHalf) return false;

            Direction facing = state.getValue(StairBlock.FACING);
            boolean outwardX = (x == 0 && facing == Direction.WEST) || (x == 1 && facing == Direction.EAST);
            boolean outwardZ = (z == 0 && facing == Direction.NORTH) || (z == 1 && facing == Direction.SOUTH);
            if (!outwardX && !outwardZ) return false;
        }
        return true;
    }

    private SmallBarrelPattern() {}
}
