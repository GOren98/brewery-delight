package dev.goren98.brewerydelight.crop;

import dev.goren98.brewerydelight.registry.ModBlocks;
import dev.goren98.brewerydelight.registry.ModComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;

/** Planting item wrapper only; cultivation behavior remains in the source-style block classes. */
public class AromaSeedItem extends BlockItem {
    private final String defaultAroma;
    public AromaSeedItem(Block block, Properties properties, String defaultAroma) { super(block, properties); this.defaultAroma = defaultAroma; }

    @Override public Component getName(ItemStack stack) {
        if (ModBlocks.TREE_IDS.contains(defaultAroma)) return Component.literal(pretty(defaultAroma) + " Sapling");
        return super.getName(stack);
    }
    @Override public InteractionResult place(BlockPlaceContext context) {
        String aroma = context.getItemInHand().getOrDefault(ModComponents.CROP_AROMA.get(), defaultAroma);
        InteractionResult result = super.place(context);
        if (result.consumesAction() && !context.getLevel().isClientSide) {
            BlockPos[] candidates = {context.getClickedPos(), context.getClickedPos().relative(context.getClickedFace())};
            for (BlockPos pos : candidates) {
                BlockEntity be = context.getLevel().getBlockEntity(pos);
                if (be instanceof AromaCropBlockEntity crop) { crop.setAroma(aroma); break; }
            }
        }
        return result;
    }
    @Override public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Aroma: " + pretty(stack.getOrDefault(ModComponents.CROP_AROMA.get(), defaultAroma))));
    }
    private static String pretty(String value) {
        StringBuilder out = new StringBuilder(); for (String p : value.split("_")) { if (p.isEmpty()) continue; if (!out.isEmpty()) out.append(' '); out.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1)); } return out.toString();
    }
}
