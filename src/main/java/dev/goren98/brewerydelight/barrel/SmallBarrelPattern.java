package dev.goren98.brewerydelight.barrel;

import net.minecraft.core.BlockPos;
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
 *
 * Horizontal stair facing and automatic inner/outer stair shape are deliberately ignored
 * for the MVP. This guarantees that the visually correct barrel shape is accepted while
 * still rejecting ordinary 2x2x2 stair cubes whose upper/lower halves are wrong.
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

                    Half expectedHalf = y == 0 ? Half.TOP : Half.BOTTOM;
                    if (state.getValue(StairBlock.HALF) != expectedHalf) return false;
                }
            }
        }
        return true;
    }

    private SmallBarrelPattern() {}
}
