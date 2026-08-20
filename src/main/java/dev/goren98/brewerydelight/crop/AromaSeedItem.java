package dev.goren98.brewerydelight.crop;

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

public class AromaSeedItem extends BlockItem {
    private final String defaultAroma;

    public AromaSeedItem(Block block, Properties properties, String defaultAroma) {
        super(block, properties);
        this.defaultAroma = defaultAroma;
    }

    @Override
    public Component getName(ItemStack stack) {
        if (getBlock() instanceof AromaCropBlock crop && crop.growthStyle() == AromaCropBlock.GrowthStyle.TREE) {
            return Component.literal(pretty(defaultAroma) + " Sapling");
        }
        return super.getName(stack);
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        ItemStack source = context.getItemInHand();
        String aroma = source.getOrDefault(ModComponents.CROP_AROMA.get(), defaultAroma);
        InteractionResult result = super.place(context);
        if (result.consumesAction() && !context.getLevel().isClientSide) {
            BlockPos pos = context.getClickedPos();
            BlockEntity be = context.getLevel().getBlockEntity(pos);
            if (!(be instanceof AromaCropBlockEntity)) {
                pos = pos.relative(context.getClickedFace());
                be = context.getLevel().getBlockEntity(pos);
            }
            if (be instanceof AromaCropBlockEntity crop) crop.setAroma(aroma);
        }
        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        String aroma = stack.getOrDefault(ModComponents.CROP_AROMA.get(), defaultAroma);
        tooltip.add(Component.literal("Aroma: " + pretty(aroma)));
    }

    private static String pretty(String value) {
        String[] parts = value.split("_");
        StringBuilder out = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) continue;
            if (!out.isEmpty()) out.append(' ');
            out.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return out.toString();
    }
}
