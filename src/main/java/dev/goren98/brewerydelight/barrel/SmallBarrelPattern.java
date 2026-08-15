package dev.goren98.brewerydelight.barrel;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.StairBlock;

/**
 * MVP pattern: the sign/controller is attached beside a compact 2x2x2 body made from exactly eight wooden stairs.
 * We accept either X- or Z-oriented bodies. Stair facing is intentionally not restricted in the first test build;
 * this makes it easy to validate the inventory/timer mechanics before tightening the visual pattern.
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
            if (!(level.getBlockState(origin.offset(x, y, z)).getBlock() instanceof StairBlock)) return false;
        }
        return true;
    }
    private SmallBarrelPattern() {}
}
