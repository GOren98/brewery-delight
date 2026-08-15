package dev.goren98.brewerydelight.barrel;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;

public final class SmallBarrelPattern {
    public record Match(BlockPos controller, ResourceLocation woodId, String displayName) {}

    public static Match find(Level level, BlockPos signPos) {
        Block signBlock = level.getBlockState(signPos).getBlock();
        ResourceLocation signId = BuiltInRegistries.BLOCK.getKey(signBlock);

        for (int dx = -2; dx <= 1; dx++) {
            for (int dy = -1; dy <= 0; dy++) {
                for (int dz = -2; dz <= 1; dz++) {
                    BlockPos origin = signPos.offset(dx, dy, dz);
                    Match match = matchBody(level, origin, signId);
                    if (match != null) return match;
                }
            }
        }
        return null;
    }

    private static Match matchBody(Level level, BlockPos origin, ResourceLocation signId) {
        Block stairBlock = null;
        ResourceLocation stairId = null;

        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 2; y++) {
                for (int z = 0; z < 2; z++) {
                    BlockState state = level.getBlockState(origin.offset(x, y, z));
                    if (!(state.getBlock() instanceof StairBlock)) return null;
                    if (!state.is(BlockTags.WOODEN_STAIRS)) return null;

                    Half expectedHalf = y == 0 ? Half.TOP : Half.BOTTOM;
                    if (state.getValue(StairBlock.HALF) != expectedHalf) return null;

                    if (stairBlock == null) {
                        stairBlock = state.getBlock();
                        stairId = BuiltInRegistries.BLOCK.getKey(stairBlock);
                    } else if (state.getBlock() != stairBlock) {
                        return null;
                    }
                }
            }
        }

        if (stairId == null || !stairId.getPath().endsWith("_stairs")) return null;
        String woodPath = stairId.getPath().substring(0, stairId.getPath().length() - "_stairs".length());
        if (!signMatchesWood(signId, stairId.getNamespace(), woodPath)) return null;

        ResourceLocation woodId = ResourceLocation.fromNamespaceAndPath(stairId.getNamespace(), woodPath);
        return new Match(origin, woodId, prettyWoodName(woodPath) + " Barrel");
    }

    private static boolean signMatchesWood(ResourceLocation signId, String namespace, String woodPath) {
        if (!signId.getNamespace().equals(namespace)) return false;
        String path = signId.getPath();
        return path.equals(woodPath + "_sign") || path.equals(woodPath + "_wall_sign");
    }

    private static String prettyWoodName(String path) {
        String[] parts = path.split("_");
        StringBuilder out = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) continue;
            if (!out.isEmpty()) out.append(' ');
            out.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return out.toString();
    }

    private SmallBarrelPattern() {}
}
