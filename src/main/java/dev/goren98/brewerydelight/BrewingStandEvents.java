package dev.goren98.brewerydelight;

import dev.goren98.brewerydelight.item.AromaUtil;
import dev.goren98.brewerydelight.registry.ModComponents;
import dev.goren98.brewerydelight.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.BrewingStandMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BrewingStandBlock;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@EventBusSubscriber(modid = BreweryDelight.MOD_ID)
public final class BrewingStandEvents {
    private static final int PROCESS_TICKS = 400;
    private static final Set<StandKey> TRACKED = new HashSet<>();
    private static final Map<StandKey, Integer> PROGRESS = new HashMap<>();

    private enum Mode { NONE, DISTILL, RECYCLE, BLEND }

    @SubscribeEvent
    public static void registerBrewingContainers(RegisterBrewingRecipesEvent event) {
        // Bottom-slot registrations. Barrier keeps vanilla brewing from triggering these MVP recipes.
        event.getBuilder().addRecipe(
                Ingredient.of(ModItems.TEST_BREW.get()),
                Ingredient.of(Items.BARRIER),
                new ItemStack(ModItems.TEST_SPIRIT.get()));
        event.getBuilder().addRecipe(
                Ingredient.of(ModItems.NEUTRAL_BASE.get()),
                Ingredient.of(Items.BARRIER),
                new ItemStack(ModItems.NEUTRAL_SPIRIT.get()));
        event.getBuilder().addRecipe(
                Ingredient.of(ModItems.TEST_SPIRIT.get()),
                Ingredient.of(Items.BARRIER),
                new ItemStack(ModItems.TEST_SPIRIT.get()));
        event.getBuilder().addRecipe(
                Ingredient.of(ModItems.TEST_LIQUEUR.get()),
                Ingredient.of(Items.BARRIER),
                new ItemStack(ModItems.TEST_LIQUEUR.get()));

        // Top-slot registrations for blending ingredients. The impossible Barrier input means
        // these recipes exist only to make vanilla's ingredient slot accept our alcohol items.
        event.getBuilder().addRecipe(
                Ingredient.of(Items.BARRIER),
                Ingredient.of(ModItems.TEST_BREW.get()),
                new ItemStack(Items.BARRIER));
        event.getBuilder().addRecipe(
                Ingredient.of(Items.BARRIER),
                Ingredient.of(ModItems.TEST_SPIRIT.get()),
                new ItemStack(Items.BARRIER));
        event.getBuilder().addRecipe(
                Ingredient.of(Items.BARRIER),
                Ingredient.of(ModItems.TEST_LIQUEUR.get()),
                new ItemStack(Items.BARRIER));
    }

    @SubscribeEvent
    public static void trackStand(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getLevel() instanceof ServerLevel level)
                || !(event.getEntity() instanceof ServerPlayer player)
                || !(level.getBlockState(event.getPos()).getBlock() instanceof BrewingStandBlock)
                || !(level.getBlockEntity(event.getPos()) instanceof BrewingStandBlockEntity stand)) {
            return;
        }

        StandKey key = new StandKey(level.dimension(), event.getPos().immutable());
        TRACKED.add(key);

        ContainerData displayData = new ContainerData() {
            @Override
            public int get(int index) {
                if (index == 0) {
                    int progress = PROGRESS.getOrDefault(key, 0);
                    return progress > 0 ? Math.max(1, PROCESS_TICKS - progress) : 0;
                }
                if (index == 1) {
                    return stand.getItem(4).is(Items.BLAZE_POWDER) ? 20 : 0;
                }
                return 0;
            }

            @Override public void set(int index, int value) {}
            @Override public int getCount() { return 2; }
        };

        player.openMenu(new SimpleMenuProvider(
                (id, playerInventory, p) -> new BrewingStandMenu(id, playerInventory, stand, displayData),
                Component.translatable("container.brewing")));
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void serverTick(ServerTickEvent.Post event) {
        Iterator<StandKey> iterator = TRACKED.iterator();
        while (iterator.hasNext()) {
            StandKey key = iterator.next();
            Level level = event.getServer().getLevel(key.dimension());
            if (level == null || !level.isLoaded(key.pos())) continue;
            if (!(level.getBlockEntity(key.pos()) instanceof BrewingStandBlockEntity stand)) {
                iterator.remove();
                PROGRESS.remove(key);
                continue;
            }

            Mode mode = determineMode(stand);
            ItemStack fuel = stand.getItem(4);
            if (mode == Mode.NONE || !fuel.is(Items.BLAZE_POWDER)) {
                PROGRESS.remove(key);
                continue;
            }

            int next = PROGRESS.getOrDefault(key, 0) + 1;
            if (next < PROCESS_TICKS) {
                PROGRESS.put(key, next);
                continue;
            }

            fuel.shrink(1);
            switch (mode) {
                case DISTILL -> finishDistillation(stand);
                case RECYCLE -> finishRecycling(stand);
                case BLEND -> finishBlending(stand);
                default -> { }
            }
            stand.setChanged();
            PROGRESS.remove(key);
        }
    }

    private static Mode determineMode(BrewingStandBlockEntity stand) {
        ItemStack ingredient = stand.getItem(3);

        if (ingredient.is(Items.SUGAR)) {
            return allBottomMatch(stand, BrewingStandEvents::isRecyclableFailedBase) ? Mode.RECYCLE : Mode.NONE;
        }

        if (!ingredient.isEmpty()) {
            return isBlendSource(ingredient) && validBlendTargets(stand, ingredient) ? Mode.BLEND : Mode.NONE;
        }

        return allBottomMatch(stand, BrewingStandEvents::isDistillableBase) ? Mode.DISTILL : Mode.NONE;
    }

    private static boolean allBottomMatch(BrewingStandBlockEntity stand, java.util.function.Predicate<ItemStack> predicate) {
        boolean found = false;
        for (int slot = 0; slot < 3; slot++) {
            ItemStack stack = stand.getItem(slot);
            if (stack.isEmpty()) continue;
            found = true;
            if (!predicate.test(stack)) return false;
        }
        return found;
    }

    private static boolean isDistillableBase(ItemStack stack) {
        if (stack.is(ModItems.NEUTRAL_BASE.get())) return true;
        return stack.is(ModItems.TEST_BREW.get())
                && stack.getOrDefault(ModComponents.STAGE.get(), 0) == 0;
    }

    private static boolean isRecyclableFailedBase(ItemStack stack) {
        if (stack.is(ModItems.NEUTRAL_BASE.get())) return false;
        if (stack.getOrDefault(ModComponents.STAGE.get(), -1) != 0) return false;
        if (stack.getOrDefault(ModComponents.PRIMARY_LEVEL.get(), -1) != 0) return false;
        String aroma = stack.getOrDefault(ModComponents.PRIMARY_AROMA.get(), "");
        return !aroma.isEmpty();
    }

    private static boolean isBlendSource(ItemStack stack) {
        int stage = stack.getOrDefault(ModComponents.STAGE.get(), -1);
        if (stage < 1 || stage > 3) return false;
        if (stack.getOrDefault(ModComponents.AGE.get(), 0) != 0) return false;
        if (stack.getOrDefault(ModComponents.BARREL_LEVEL.get(), 0) != 0) return false;
        if (!stack.getOrDefault(ModComponents.BLEND_AROMAS.get(), Map.of()).isEmpty()) return false;

        String primary = stack.getOrDefault(ModComponents.PRIMARY_AROMA.get(), "");
        int level = stack.getOrDefault(ModComponents.PRIMARY_LEVEL.get(), 0);
        return !primary.isEmpty() && level > 0 && AromaUtil.hasExactlyOneAroma(stack);
    }

    private static boolean validBlendTargets(BrewingStandBlockEntity stand, ItemStack source) {
        int sourceStage = source.getOrDefault(ModComponents.STAGE.get(), -1);
        String sourceAroma = source.getOrDefault(ModComponents.PRIMARY_AROMA.get(), "");
        int sourceLevel = stackPrimaryLevel(source);

        String product = "";
        boolean found = false;
        for (int slot = 0; slot < 3; slot++) {
            ItemStack target = stand.getItem(slot);
            if (target.isEmpty()) continue;
            found = true;

            int stage = target.getOrDefault(ModComponents.STAGE.get(), -1);
            if (stage != sourceStage || stage < 1 || stage > 3) return false; // Brew↔Brew, Spirit↔Spirit, Liqueur↔Liqueur only.

            String targetProduct = target.getOrDefault(ModComponents.PRODUCT_ID.get(), "");
            if (targetProduct.isEmpty()) return false;
            if (product.isEmpty()) product = targetProduct;
            else if (!product.equals(targetProduct)) return false;

            int total = AromaUtil.total(target);
            if (total < 10 || total >= AromaUtil.MAX_TOTAL_AROMA) return false;
            if (AromaUtil.blendGain(target, sourceAroma, sourceLevel) <= 0) return false;
        }
        return found;
    }

    private static int stackPrimaryLevel(ItemStack stack) {
        return stack.getOrDefault(ModComponents.PRIMARY_LEVEL.get(), 0);
    }

    private static void finishDistillation(BrewingStandBlockEntity stand) {
        for (int slot = 0; slot < 3; slot++) {
            ItemStack base = stand.getItem(slot);
            if (isDistillableBase(base)) stand.setItem(slot, makeSpirit(base));
        }
    }

    private static void finishRecycling(BrewingStandBlockEntity stand) {
        for (int slot = 0; slot < 3; slot++) {
            ItemStack base = stand.getItem(slot);
            if (isRecyclableFailedBase(base)) stand.setItem(slot, makeNeutralSpirit(base.getCount()));
        }
        stand.getItem(3).shrink(1); // one Sugar recycles one batch, matching vanilla-style ingredient use
    }

    private static void finishBlending(BrewingStandBlockEntity stand) {
        ItemStack source = stand.getItem(3);
        if (!isBlendSource(source)) return;

        String aroma = source.getOrDefault(ModComponents.PRIMARY_AROMA.get(), "");
        int level = source.getOrDefault(ModComponents.PRIMARY_LEVEL.get(), 0);
        for (int slot = 0; slot < 3; slot++) {
            ItemStack target = stand.getItem(slot);
            if (!target.isEmpty()) AromaUtil.applyBlend(target, aroma, level);
        }
        source.shrink(1);
    }

    private static ItemStack makeSpirit(ItemStack base) {
        if (base.is(ModItems.NEUTRAL_BASE.get())) return makeNeutralSpirit(base.getCount());

        ItemStack spirit = new ItemStack(ModItems.TEST_SPIRIT.get(), base.getCount());
        String aroma = base.getOrDefault(ModComponents.PRIMARY_AROMA.get(), "test");
        int level = base.getOrDefault(ModComponents.PRIMARY_LEVEL.get(), 0);
        spirit.set(ModComponents.PRODUCT_ID.get(), "test_spirit");
        spirit.set(ModComponents.STAGE.get(), 2);
        spirit.set(ModComponents.AGE.get(), 0);
        spirit.set(ModComponents.PRIMARY_AROMA.get(), aroma);
        spirit.set(ModComponents.PRIMARY_LEVEL.get(), level <= 0 ? 0 : Math.min(5, level + 1));
        spirit.set(ModComponents.BARREL_LEVEL.get(), 0);
        spirit.set(ModComponents.BLEND_AROMAS.get(), Map.of());
        spirit.set(ModComponents.SEASONING_COUNTED.get(), false);
        return spirit;
    }

    private static ItemStack makeNeutralSpirit(int count) {
        ItemStack spirit = new ItemStack(ModItems.NEUTRAL_SPIRIT.get(), count);
        spirit.set(ModComponents.PRODUCT_ID.get(), "neutral_spirit");
        spirit.set(ModComponents.STAGE.get(), 2);
        spirit.set(ModComponents.AGE.get(), 0);
        spirit.set(ModComponents.PRIMARY_AROMA.get(), "");
        spirit.set(ModComponents.PRIMARY_LEVEL.get(), 0);
        spirit.remove(ModComponents.BARREL_AROMA.get());
        spirit.set(ModComponents.BARREL_LEVEL.get(), 0);
        spirit.set(ModComponents.BLEND_AROMAS.get(), Map.of());
        spirit.set(ModComponents.SEASONING_COUNTED.get(), false);
        return spirit;
    }

    private record StandKey(ResourceKey<Level> dimension, BlockPos pos) {}
    private BrewingStandEvents() {}
}
