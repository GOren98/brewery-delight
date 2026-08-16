package dev.goren98.brewerydelight;

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
    private static final int DISTILL_TICKS = 400;
    private static final Set<StandKey> TRACKED = new HashSet<>();
    private static final Map<StandKey, Integer> PROGRESS = new HashMap<>();

    @SubscribeEvent
    public static void registerBrewingContainers(RegisterBrewingRecipesEvent event) {
        event.getBuilder().addRecipe(
                Ingredient.of(ModItems.TEST_BREW.get()),
                Ingredient.of(Items.BARRIER),
                new ItemStack(ModItems.TEST_SPIRIT.get()));
        event.getBuilder().addRecipe(
                Ingredient.of(ModItems.NEUTRAL_BASE.get()),
                Ingredient.of(Items.BARRIER),
                new ItemStack(ModItems.NEUTRAL_SPIRIT.get()));
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
                    return progress > 0 ? Math.max(1, DISTILL_TICKS - progress) : 0;
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
        if (stack.is(ModItems.NEUTRAL_BASE.get())) return true;
        return stack.is(ModItems.TEST_BREW.get())
                && stack.getOrDefault(ModComponents.STAGE.get(), 0) == 0;
    }

    private static ItemStack makeSpirit(ItemStack base) {
        if (base.is(ModItems.NEUTRAL_BASE.get())) {
            ItemStack spirit = new ItemStack(ModItems.NEUTRAL_SPIRIT.get(), base.getCount());
            spirit.set(ModComponents.PRODUCT_ID.get(), "neutral_spirit");
            spirit.set(ModComponents.STAGE.get(), 2);
            spirit.set(ModComponents.AGE.get(), 0);
            spirit.set(ModComponents.PRIMARY_AROMA.get(), "");
            spirit.set(ModComponents.PRIMARY_LEVEL.get(), 0);
            spirit.set(ModComponents.BARREL_LEVEL.get(), 0);
            spirit.set(ModComponents.SEASONING_COUNTED.get(), false);
            return spirit;
        }

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
