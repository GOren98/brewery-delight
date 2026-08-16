package dev.goren98.brewerydelight;

import dev.goren98.brewerydelight.item.AromaUtil;
import dev.goren98.brewerydelight.registry.ModComponents;
import dev.goren98.brewerydelight.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.BrewingStandMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
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
import java.util.List;
import java.util.Map;
import java.util.Set;

@EventBusSubscriber(modid = BreweryDelight.MOD_ID)
public final class BrewingStandEvents {
    private static final int PROCESS_TICKS = 400;
    private static final int NEUTRAL_COLOR = 0xE7EFF0;
    private static final Set<StandKey> TRACKED = new HashSet<>();
    private static final Map<StandKey, Integer> PROGRESS = new HashMap<>();

    private static final List<LiqueurRecipe> LIQUEUR_RECIPES = List.of(
            tagLiqueur("c:crops/orange", "orange_liqueur", "Orange Liqueur", "orange", 15170338),
            tagLiqueur("c:crops/lemon", "lemon_liqueur", "Lemon Liqueur", "lemon", 14273849),
            tagLiqueur("c:crops/lime", "lime_liqueur", "Lime Liqueur", "lime", 8366661),
            tagLiqueur("c:crops/banana", "banana_liqueur", "Banana Liqueur", "banana", 14468685),
            tagLiqueur("c:crops/mango", "mango_liqueur", "Mango Liqueur", "mango", 14519082),
            tagLiqueur("c:crops/lychee", "lychee_liqueur", "Lychee Liqueur", "lychee", 14264745),
            tagLiqueur("c:crops/passionfruit", "passion_fruit_liqueur", "Passion Fruit Liqueur", "passion_fruit", 13668652),
            itemLiqueur(Items.MELON_SLICE, "watermelon_liqueur", "Watermelon Liqueur", "watermelon", 14243176),

            tagLiqueur("c:crops/almond", "almond_liqueur", "Almond Liqueur", "almond", 12886650),
            tagLiqueur("c:crops/chestnut", "chestnut_liqueur", "Chestnut Liqueur", "chestnut", 9330751),
            tagLiqueur("c:crops/hazelnut", "hazelnut_liqueur", "Hazelnut Liqueur", "hazelnut", 10118975),
            tagLiqueur("c:crops/pecan", "pecan_liqueur", "Pecan Liqueur", "pecan", 8737843),
            tagLiqueur("c:crops/pistachio", "pistachio_liqueur", "Pistachio Liqueur", "pistachio", 10201693),
            tagLiqueur("c:crops/walnut", "walnut_liqueur", "Walnut Liqueur", "walnut", 7358767),

            tagLiqueur("c:crops/coffeebean", "coffee_liqueur", "Coffee Liqueur", "coffee", 6635566),
            tagLiqueur("c:crops/vanillabean", "vanilla_liqueur", "Vanilla Liqueur", "vanilla", 14139018),
            tagLiqueur("c:crops/cinnamon", "cinnamon_liqueur", "Cinnamon Liqueur", "cinnamon", 10769970),
            tagLiqueur("c:crops/ginger", "ginger_liqueur", "Ginger Liqueur", "ginger", 13014338),
            tagLiqueur("c:crops/nutmeg", "nutmeg_liqueur", "Nutmeg Liqueur", "nutmeg", 8870978),

            tagLiqueur("c:crops/juniperberry", "gin", "Gin", "juniper", 12374461),
            itemLiqueur(Items.EGG, "advocaat", "Advocaat", "egg", 14531402),
            tagLiqueur("c:crops/tealeaf", "absinthe", "Absinthe", "herbal", 7906896),
            tagLiqueur("c:crops/spiceleaf", "aquavit", "Aquavit", "spiced", 13746040)
    );

    private enum Mode { NONE, DISTILL, RECYCLE, BLEND, LIQUEUR }

    @SubscribeEvent
    public static void registerBrewingContainers(RegisterBrewingRecipesEvent event) {
        // Legacy MVP registrations retained for regression compatibility.
        registerBottom(event, ModItems.TEST_BREW.get(), ModItems.TEST_SPIRIT.get());
        registerBottom(event, ModItems.NEUTRAL_BASE.get(), ModItems.NEUTRAL_SPIRIT.get());
        registerBottom(event, ModItems.TEST_SPIRIT.get(), ModItems.TEST_SPIRIT.get());
        registerBottom(event, ModItems.TEST_LIQUEUR.get(), ModItems.TEST_LIQUEUR.get());
        registerTop(event, ModItems.TEST_BREW.get());
        registerTop(event, ModItems.TEST_SPIRIT.get());
        registerTop(event, ModItems.TEST_LIQUEUR.get());

        // Generic category bottles. Product-specific behavior comes from stack components.
        registerBottom(event, ModItems.BREW_BOTTLE.get(), ModItems.SPIRIT_BOTTLE.get());
        registerBottom(event, ModItems.SPIRIT_BOTTLE.get(), ModItems.SPIRIT_BOTTLE.get());
        registerBottom(event, ModItems.LIQUEUR_BOTTLE.get(), ModItems.LIQUEUR_BOTTLE.get());
        registerBottom(event, ModItems.NEUTRAL_SPIRIT.get(), ModItems.LIQUEUR_BOTTLE.get());
        registerTop(event, ModItems.BREW_BOTTLE.get());
        registerTop(event, ModItems.SPIRIT_BOTTLE.get());
        registerTop(event, ModItems.LIQUEUR_BOTTLE.get());

        // Allow all configured liqueur ingredients in the brewing stand ingredient slot.
        for (LiqueurRecipe recipe : LIQUEUR_RECIPES) registerTop(event, recipe.ingredient());
    }

    private static void registerBottom(RegisterBrewingRecipesEvent event, Item input, Item output) {
        event.getBuilder().addRecipe(Ingredient.of(input), Ingredient.of(Items.BARRIER), new ItemStack(output));
    }

    private static void registerTop(RegisterBrewingRecipesEvent event, Item ingredient) {
        registerTop(event, Ingredient.of(ingredient));
    }

    private static void registerTop(RegisterBrewingRecipesEvent event, Ingredient ingredient) {
        event.getBuilder().addRecipe(Ingredient.of(Items.BARRIER), ingredient, new ItemStack(Items.BARRIER));
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
                case LIQUEUR -> finishLiqueur(stand, level);
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
            LiqueurRecipe liqueur = findLiqueurRecipe(ingredient);
            if (liqueur != null) {
                return allBottomMatch(stand, BrewingStandEvents::isNeutralSpirit) ? Mode.LIQUEUR : Mode.NONE;
            }
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

    private static boolean isNeutralSpirit(ItemStack stack) {
        return stack.is(ModItems.NEUTRAL_SPIRIT.get());
    }

    private static boolean isDistillableBase(ItemStack stack) {
        if (stack.is(ModItems.NEUTRAL_BASE.get())) return true;
        if (stack.is(ModItems.TEST_BREW.get())) {
            return stack.getOrDefault(ModComponents.STAGE.get(), 0) == 0;
        }
        return stack.is(ModItems.BREW_BOTTLE.get())
                && stack.getOrDefault(ModComponents.STAGE.get(), 0) == 0
                && !stack.getOrDefault(ModComponents.DISTILL_PRODUCT_ID.get(), "").isEmpty();
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
        int sourceLevel = source.getOrDefault(ModComponents.PRIMARY_LEVEL.get(), 0);

        String product = "";
        boolean found = false;
        for (int slot = 0; slot < 3; slot++) {
            ItemStack target = stand.getItem(slot);
            if (target.isEmpty()) continue;
            found = true;

            int stage = target.getOrDefault(ModComponents.STAGE.get(), -1);
            if (stage != sourceStage || stage < 1 || stage > 3) return false;

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
        stand.getItem(3).shrink(1);
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

    private static void finishLiqueur(BrewingStandBlockEntity stand, Level level) {
        ItemStack ingredient = stand.getItem(3);
        LiqueurRecipe recipe = findLiqueurRecipe(ingredient);
        if (recipe == null) return;

        for (int slot = 0; slot < 3; slot++) {
            ItemStack neutral = stand.getItem(slot);
            if (!isNeutralSpirit(neutral)) continue;

            ItemStack result = new ItemStack(ModItems.LIQUEUR_BOTTLE.get(), neutral.getCount());
            result.set(ModComponents.PRODUCT_ID.get(), recipe.productId());
            result.set(ModComponents.DISPLAY_NAME.get(), recipe.displayName());
            result.set(ModComponents.STAGE.get(), 3);
            result.set(ModComponents.AGE.get(), 0);
            result.set(ModComponents.PRIMARY_AROMA.get(), recipe.aroma());
            result.set(ModComponents.PRIMARY_LEVEL.get(), 1 + level.random.nextInt(5));
            result.set(ModComponents.COLOR.get(), recipe.color());
            result.set(ModComponents.BARREL_LEVEL.get(), 0);
            result.set(ModComponents.AGING_AROMAS.get(), Map.of());
            result.set(ModComponents.BLEND_AROMAS.get(), Map.of());
            result.set(ModComponents.SEASONING_COUNTED.get(), false);
            stand.setItem(slot, result);
        }
        ingredient.shrink(1);
    }

    private static ItemStack makeSpirit(ItemStack base) {
        if (base.is(ModItems.NEUTRAL_BASE.get())) return makeNeutralSpirit(base.getCount());

        // Legacy Test Brew path remains equivalent in behavior.
        if (base.is(ModItems.TEST_BREW.get())) {
            ItemStack spirit = new ItemStack(ModItems.TEST_SPIRIT.get(), base.getCount());
            copySpiritData(base, spirit, "test_spirit", "Test Spirit");
            return spirit;
        }

        ItemStack spirit = new ItemStack(ModItems.SPIRIT_BOTTLE.get(), base.getCount());
        String resultId = base.getOrDefault(ModComponents.DISTILL_PRODUCT_ID.get(), "");
        String resultName = base.getOrDefault(ModComponents.DISTILL_DISPLAY_NAME.get(), prettyId(resultId));
        copySpiritData(base, spirit, resultId, resultName);
        return spirit;
    }

    private static void copySpiritData(ItemStack base, ItemStack spirit, String resultId, String resultName) {
        String aroma = base.getOrDefault(ModComponents.PRIMARY_AROMA.get(), "");
        int level = base.getOrDefault(ModComponents.PRIMARY_LEVEL.get(), 0);
        int color = base.getOrDefault(ModComponents.DISTILL_COLOR.get(),
                base.getOrDefault(ModComponents.COLOR.get(), 0xFFFFFF));

        spirit.set(ModComponents.PRODUCT_ID.get(), resultId);
        spirit.set(ModComponents.DISPLAY_NAME.get(), resultName);
        spirit.set(ModComponents.STAGE.get(), 2);
        spirit.set(ModComponents.AGE.get(), 0);
        spirit.set(ModComponents.PRIMARY_AROMA.get(), aroma);
        spirit.set(ModComponents.PRIMARY_LEVEL.get(), level <= 0 ? 0 : Math.min(5, level + 1));
        spirit.set(ModComponents.COLOR.get(), color);
        spirit.set(ModComponents.BARREL_LEVEL.get(), 0);
        spirit.set(ModComponents.AGING_AROMAS.get(), Map.of());
        spirit.set(ModComponents.BLEND_AROMAS.get(), Map.of());
        spirit.set(ModComponents.SEASONING_COUNTED.get(), false);
    }

    private static ItemStack makeNeutralSpirit(int count) {
        ItemStack spirit = new ItemStack(ModItems.NEUTRAL_SPIRIT.get(), count);
        spirit.set(ModComponents.PRODUCT_ID.get(), "neutral_spirit");
        spirit.set(ModComponents.DISPLAY_NAME.get(), "Neutral Spirit");
        spirit.set(ModComponents.STAGE.get(), 2);
        spirit.set(ModComponents.AGE.get(), 0);
        spirit.set(ModComponents.PRIMARY_AROMA.get(), "");
        spirit.set(ModComponents.PRIMARY_LEVEL.get(), 0);
        spirit.set(ModComponents.COLOR.get(), NEUTRAL_COLOR);
        spirit.remove(ModComponents.BARREL_AROMA.get());
        spirit.set(ModComponents.BARREL_LEVEL.get(), 0);
        spirit.set(ModComponents.AGING_AROMAS.get(), Map.of());
        spirit.set(ModComponents.BLEND_AROMAS.get(), Map.of());
        spirit.set(ModComponents.SEASONING_COUNTED.get(), false);
        return spirit;
    }

    private static LiqueurRecipe findLiqueurRecipe(ItemStack ingredient) {
        for (LiqueurRecipe recipe : LIQUEUR_RECIPES) {
            if (recipe.ingredient().test(ingredient)) return recipe;
        }
        return null;
    }

    private static LiqueurRecipe tagLiqueur(String tagId, String productId, String displayName, String aroma, int color) {
        TagKey<Item> tag = TagKey.create(Registries.ITEM, ResourceLocation.parse(tagId));
        return new LiqueurRecipe(Ingredient.of(tag), productId, displayName, aroma, color);
    }

    private static LiqueurRecipe itemLiqueur(Item item, String productId, String displayName, String aroma, int color) {
        return new LiqueurRecipe(Ingredient.of(item), productId, displayName, aroma, color);
    }

    private static String prettyId(String value) {
        if (value == null || value.isEmpty()) return "Spirit";
        String path = value.contains(":") ? value.substring(value.indexOf(':') + 1) : value;
        String[] parts = path.split("_");
        StringBuilder out = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) continue;
            if (!out.isEmpty()) out.append(' ');
            out.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return out.toString();
    }

    private record LiqueurRecipe(Ingredient ingredient, String productId, String displayName, String aroma, int color) {}
    private record StandKey(ResourceKey<Level> dimension, BlockPos pos) {}
    private BrewingStandEvents() {}
}
