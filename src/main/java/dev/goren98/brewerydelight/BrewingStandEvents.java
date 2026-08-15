package dev.goren98.brewerydelight;

import dev.goren98.brewerydelight.registry.ModComponents;
import dev.goren98.brewerydelight.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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

/** MVP distillation path using the vanilla Brewing Stand as the vessel. */
@EventBusSubscriber(modid = BreweryDelight.MOD_ID)
public final class BrewingStandEvents {
    private static final int DISTILL_TICKS = 400; // vanilla brewing duration: 20 seconds
    private static final Set<StandKey> TRACKED = new HashSet<>();
    private static final Map<StandKey, Integer> PROGRESS = new HashMap<>();

    @SubscribeEvent
    public static void registerBrewingContainers(RegisterBrewingRecipesEvent event) {
        // Validates Test Brew as a bottle-slot container. Distillation itself is custom
        // because the design intentionally uses no top-slot reagent.
        event.getBuilder().addContainer(ModItems.TEST_BREW.get());
    }

    @SubscribeEvent
    public static void trackStand(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getLevel().getBlockState(event.getPos()).getBlock() instanceof BrewingStandBlock)) return;
        TRACKED.add(new StandKey(event.getLevel().dimension(), event.getPos().immutable()));
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

            boolean hasBase = false;
            for (int slot = 0; slot < 3; slot++) {
                if (isDistillableBase(stand.getItem(slot))) {
                    hasBase = true;
                    break;
                }
            }

            ItemStack fuel = stand.getItem(4);
            if (!hasBase || !fuel.is(Items.BLAZE_POWDER)) {
                PROGRESS.remove(key);
                continue;
            }

            int next = PROGRESS.getOrDefault(key, 0) + 1;
            if (next < DISTILL_TICKS) {
                PROGRESS.put(key, next);
                continue;
            }

            // One blaze powder powers one MVP distillation batch (up to three bottles).
            fuel.shrink(1);
            for (int slot = 0; slot < 3; slot++) {
                ItemStack base = stand.getItem(slot);
                if (!isDistillableBase(base)) continue;
                stand.setItem(slot, makeSpirit(base));
            }
            stand.setChanged();
            PROGRESS.remove(key);
        }
    }

    private static boolean isDistillableBase(ItemStack stack) {
        return stack.is(ModItems.TEST_BREW.get())
                && stack.getOrDefault(ModComponents.STAGE.get(), 0) == 0;
    }

    private static ItemStack makeSpirit(ItemStack base) {
        ItemStack spirit = new ItemStack(ModItems.TEST_SPIRIT.get(), base.getCount());
        String aroma = base.getOrDefault(ModComponents.PRIMARY_AROMA.get(), "test");
        int level = base.getOrDefault(ModComponents.PRIMARY_LEVEL.get(), 0);

        spirit.set(ModComponents.PRODUCT_ID.get(), "test_spirit");
        spirit.set(ModComponents.STAGE.get(), 2);
        spirit.set(ModComponents.AGE.get(), 0);
        spirit.set(ModComponents.PRIMARY_AROMA.get(), aroma);
        spirit.set(ModComponents.PRIMARY_LEVEL.get(), level <= 0 ? 0 : Math.min(5, level + 1));
        spirit.set(ModComponents.BARREL_LEVEL.get(), 0);
        spirit.set(ModComponents.SEASONING_COUNTED.get(), false);
        return spirit;
    }

    private record StandKey(ResourceKey<Level> dimension, BlockPos pos) {}

    private BrewingStandEvents() {}
}
