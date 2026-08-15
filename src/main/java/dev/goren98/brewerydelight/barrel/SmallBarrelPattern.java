package dev.goren98.brewerydelight.barrel;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;

/**
 * Small Barrel: a 2x2x2 body of eight stairs.
 *
 * Target shape (matching the reference screenshot):
 * - lower layer: 4 upside-down stairs (TOP half)
 * - upper layer: 4 normal stairs (BOTTOM half)
 * - every stair faces away from the center of the 2x2 body
 *
 * Minecraft may automatically change a stair's rendered SHAPE into an inner/outer
 * corner when neighboring stairs are placed. That visual corner state is deliberately
 * ignored here; only HALF and FACING define whether the multiblock is valid.
 */
public final class SmallBarrelPattern {
    public static BlockPos findController(Level level, BlockPos signPos) {
        for (int dx = -2; dx <= 1; dx++) {
            for (int dy = -1; dy <= 0; dy++) {
                for (int dz = -2; dz <= 1; dz++) {
                    BlockPos origin = signPos.offset(dx, dy, dz);
                    if (isBody(level, origin)) return origin;
                }
            }
        }
        return null;
    }

    public static boolean isBody(Level level, BlockPos origin) {
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 2; y++) {
                for (int z = 0; z < 2; z++) {
                    BlockState state = level.getBlockState(origin.offset(x, y, z));
                    if (!(state.getBlock() instanceof StairBlock)) return false;

                    // The reference barrel bulges in the middle:
                    // bottom layer is upside-down, top layer is normal.
                    Half expectedHalf = y == 0 ? Half.TOP : Half.BOTTOM;
                    if (state.getValue(StairBlock.HALF) != expectedHalf) return false;

                    Direction facing = state.getValue(StairBlock.FACING);
                    if (!facesOutward(x, z, facing)) return false;
                }
            }
        }
        return true;
    }

    private static boolean facesOutward(int x, int z, Direction facing) {
        // Each corner has two valid outward directions. This allows the exact barrel
        // silhouette to be built along either axis while still rejecting inward-facing
        // or arbitrary 2x2 stair cubes.
        return (x == 0 && facing == Direction.WEST)
                || (x == 1 && facing == Direction.EAST)
                || (z == 0 && facing == Direction.NORTH)
                || (z == 1 && facing == Direction.SOUTH);
    }

    private SmallBarrelPattern() {}
}
