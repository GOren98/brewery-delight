package dev.goren98.brewerydelight.aroma;

import dev.goren98.brewerydelight.crop.AromaProduceItem;
import dev.goren98.brewerydelight.registry.ModComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Optional;

/** Single policy entry point for Aroma Table and later Brewing Station recipes. */
public final class AromaItems {
    public static ItemAromaClass classify(ItemStack stack) {
        if (stack.isEmpty()) return ItemAromaClass.NORMAL_ITEM;
        if (stack.getItem() instanceof AromaProduceItem) return ItemAromaClass.BREEDABLE_BASE;

        // Minimal fixed-base compatibility set for 6-2 foundation tests. These items may donate
        // their inherent aroma to production but must never accept a transferred crop aroma.
        if (stack.is(Items.APPLE) || stack.is(Items.SUGAR_CANE) || stack.is(Items.EGG)) {
            return ItemAromaClass.FIXED_BASE;
        }

        // A non-crop stack carrying CROP_AROMA is an aroma donor, not a breedable recipient.
        if (stack.has(ModComponents.CROP_AROMA.get())) return ItemAromaClass.AROMA_ONLY;
        return ItemAromaClass.NORMAL_ITEM;
    }

    public static boolean hasAroma(ItemStack stack) { return classify(stack).hasAroma(); }
    public static boolean canReceiveAroma(ItemStack stack) { return classify(stack).canReceiveAroma(); }
    public static boolean canMakeBase(ItemStack stack) { return classify(stack).canMakeBase(); }

    public static Optional<String> currentAromaId(ItemStack stack) {
        if (!hasAroma(stack)) return Optional.empty();
        String component = stack.get(ModComponents.CROP_AROMA.get());
        if (component != null && !component.isBlank()) return Optional.of(component);
        if (stack.getItem() instanceof AromaProduceItem produce) return Optional.of(produce.defaultAroma());
        if (stack.is(Items.APPLE)) return Optional.of("apple");
        if (stack.is(Items.SUGAR_CANE)) return Optional.of("sugar_cane");
        if (stack.is(Items.EGG)) return Optional.of("egg");
        return Optional.empty();
    }

    public static Optional<AromaDefinition> definition(ItemStack stack) {
        return currentAromaId(stack).flatMap(AromaDefinitions::find);
    }

    private AromaItems() {}
}
