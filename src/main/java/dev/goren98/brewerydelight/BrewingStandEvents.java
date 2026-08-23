package dev.goren98.brewerydelight;

import dev.goren98.brewerydelight.alcohol.CoreAlcoholInput;
import dev.goren98.brewerydelight.alcohol.CoreAlcoholRecipe;
import dev.goren98.brewerydelight.alcohol.AlcoholTransformationResolver;
import dev.goren98.brewerydelight.item.AromaUtil;
import dev.goren98.brewerydelight.registry.ModComponents;
import dev.goren98.brewerydelight.registry.ModItems;
import dev.goren98.brewerydelight.registry.ModRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.BrewingStandMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
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
import java.util.Optional;
import java.util.Set;

@EventBusSubscriber(modid = BreweryDelight.MOD_ID)
public final class BrewingStandEvents {
    private static final int PROCESS_TICKS = 400;
    private static final Set<StandKey> TRACKED = new HashSet<>();
    private static final Map<StandKey, Integer> PROGRESS = new HashMap<>();

    private enum Mode { NONE, DISTILL, BLEND }

    @SubscribeEvent
    public static void registerBrewingContainers(RegisterBrewingRecipesEvent event) {
        registerBottom(event, ModItems.BASE_BOTTLE.get(), ModItems.SPIRIT_BOTTLE.get());
        registerBottom(event, ModItems.BREW_BOTTLE.get(), ModItems.BREW_BOTTLE.get());
        registerBottom(event, ModItems.SPIRIT_BOTTLE.get(), ModItems.SPIRIT_BOTTLE.get());
        registerBottom(event, ModItems.LIQUEUR_BOTTLE.get(), ModItems.LIQUEUR_BOTTLE.get());
        registerTop(event, ModItems.BREW_BOTTLE.get());
        registerTop(event, ModItems.SPIRIT_BOTTLE.get());
        registerTop(event, ModItems.LIQUEUR_BOTTLE.get());
    }

    private static void registerBottom(RegisterBrewingRecipesEvent event, Item input, Item output) {
        event.getBuilder().addRecipe(Ingredient.of(input), Ingredient.of(Items.BARRIER), new ItemStack(output));
    }
    private static void registerTop(RegisterBrewingRecipesEvent event, Item ingredient) { registerTop(event, Ingredient.of(ingredient)); }
    private static void registerTop(RegisterBrewingRecipesEvent event, Ingredient ingredient) {
        event.getBuilder().addRecipe(Ingredient.of(Items.BARRIER), ingredient, new ItemStack(Items.BARRIER));
    }

    @SubscribeEvent
    public static void trackStand(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getLevel() instanceof ServerLevel level)
                || !(event.getEntity() instanceof ServerPlayer player)
                || !(level.getBlockState(event.getPos()).getBlock() instanceof BrewingStandBlock)
                || !(level.getBlockEntity(event.getPos()) instanceof BrewingStandBlockEntity stand)) return;

        StandKey key = new StandKey(level.dimension(), event.getPos().immutable());
        TRACKED.add(key);
        ContainerData displayData = new ContainerData() {
            @Override public int get(int index) {
                if (index == 0) {
                    int progress = PROGRESS.getOrDefault(key, 0);
                    return progress > 0 ? Math.max(1, PROCESS_TICKS - progress) : 0;
                }
                if (index == 1) return stand.getItem(4).is(Items.BLAZE_POWDER) ? 20 : 0;
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
            if (level == null || !level.isLoaded(key.pos())) {
                iterator.remove(); PROGRESS.remove(key); continue;
            }
            if (!(level.getBlockEntity(key.pos()) instanceof BrewingStandBlockEntity stand)) {
                iterator.remove(); PROGRESS.remove(key); continue;
            }

            Mode mode = determineMode(stand, level);
            ItemStack fuel = stand.getItem(4);
            if (mode == Mode.NONE || !fuel.is(Items.BLAZE_POWDER)) { PROGRESS.remove(key); continue; }
            int next = PROGRESS.getOrDefault(key, 0) + 1;
            if (next < PROCESS_TICKS) { PROGRESS.put(key, next); continue; }

            fuel.shrink(1);
            switch (mode) {
                case DISTILL -> finishDistillation(stand, level);
                case BLEND -> finishBlending(stand, level);
                default -> { }
            }
            stand.setChanged(); PROGRESS.remove(key);
        }
    }

    private static Mode determineMode(BrewingStandBlockEntity stand, Level level) {
        ItemStack ingredient = stand.getItem(3);
        if (!ingredient.isEmpty()) {
            return isBlendSource(ingredient) && validBlendTargets(stand, ingredient) ? Mode.BLEND : Mode.NONE;
        }
        return allBottomMatch(stand, stack -> findCoreRecipe(stack, "spirit", level).isPresent()) ? Mode.DISTILL : Mode.NONE;
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

    private static Optional<RecipeHolder<CoreAlcoholRecipe>> findCoreRecipe(ItemStack stack, String process, Level level) {
        if (!stack.is(ModItems.BASE_BOTTLE.get())) return Optional.empty();
        return level.getRecipeManager().getRecipeFor(
                ModRecipes.CORE_ALCOHOL_TYPE.get(), new CoreAlcoholInput(stack, process), level);
    }

    private static boolean isBlendSource(ItemStack stack) {
        int stage = stack.getOrDefault(ModComponents.STAGE.get(), -1);
        if (stage < 1 || stage > 3) return false;
        if (stack.getOrDefault(ModComponents.AGE.get(), 0) != 0) return false;
        if (stack.getOrDefault(ModComponents.BARREL_LEVEL.get(), 0) != 0) return false;
        if (stack.getOrDefault(ModComponents.CORE_ALCOHOL_ID.get(), "").isEmpty()) return false;
        String primary = stack.getOrDefault(ModComponents.PRIMARY_AROMA.get(), "");
        int level = stack.getOrDefault(ModComponents.PRIMARY_LEVEL.get(), 0);
        return !primary.isEmpty() && level > 0;
    }

    private static boolean validBlendTargets(BrewingStandBlockEntity stand, ItemStack source) {
        String sourceCore = source.getOrDefault(ModComponents.CORE_ALCOHOL_ID.get(), "");
        String sourceAroma = source.getOrDefault(ModComponents.PRIMARY_AROMA.get(), "");
        int sourceLevel = source.getOrDefault(ModComponents.PRIMARY_LEVEL.get(), 0);
        boolean found = false;
        for (int slot = 0; slot < 3; slot++) {
            ItemStack target = stand.getItem(slot);
            if (target.isEmpty()) continue;
            found = true;
            int stage = target.getOrDefault(ModComponents.STAGE.get(), -1);
            if (stage < 1 || stage > 3) return false;
            if (target.getOrDefault(ModComponents.AGE.get(), 0) != 5) return false;
            if (!sourceCore.equals(target.getOrDefault(ModComponents.CORE_ALCOHOL_ID.get(), ""))) return false;
            if (!AromaUtil.canApplyBlendFully(target, sourceAroma, sourceLevel)) return false;
        }
        return found;
    }

    private static void finishDistillation(BrewingStandBlockEntity stand, Level level) {
        for (int slot = 0; slot < 3; slot++) {
            ItemStack base = stand.getItem(slot);
            Optional<RecipeHolder<CoreAlcoholRecipe>> recipe = findCoreRecipe(base, "spirit", level);
            if (recipe.isPresent()) {
                ItemStack result = recipe.get().value().assemble(new CoreAlcoholInput(base, "spirit"), level.registryAccess());
                AlcoholTransformationResolver.resolve(level, result);
                stand.setItem(slot, result);
            }
        }
    }

    private static void finishBlending(BrewingStandBlockEntity stand, Level level) {
        ItemStack source = stand.getItem(3);
        if (!isBlendSource(source)) return;
        String aroma = source.getOrDefault(ModComponents.PRIMARY_AROMA.get(), "");
        int sourceLevel = source.getOrDefault(ModComponents.PRIMARY_LEVEL.get(), 0);
        for (int slot = 0; slot < 3; slot++) {
            ItemStack target = stand.getItem(slot);
            if (!target.isEmpty() && AromaUtil.applyBlend(target, aroma, sourceLevel)) {
                AlcoholTransformationResolver.resolve(level, target);
            }
        }
        source.shrink(1);
    }

    private record StandKey(ResourceKey<Level> dimension, BlockPos pos) {}
    private BrewingStandEvents() {}
}
