package dev.goren98.brewerydelight.registry;

import dev.goren98.brewerydelight.BreweryDelight;
import dev.goren98.brewerydelight.cooking.CookingPotBlock;
import dev.goren98.brewerydelight.crop.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.*;
import java.util.function.Supplier;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, BreweryDelight.MOD_ID);
    public static final Map<String, Supplier<? extends Block>> PLANTS = new LinkedHashMap<>();
    public static final List<Supplier<? extends Block>> AROMA_BLOCKS = new ArrayList<>();
    public static final List<Supplier<? extends Block>> CROPTOPIA_LEAF_BLOCKS = new ArrayList<>();

    public static final Set<String> FD_TREE_IDS = Set.of("apple", "pear", "peach", "orange", "mango", "kiwi");
    public static final Set<String> CROPTOPIA_TREE_IDS = Set.of("cherry", "plum", "apricot", "lime", "banana", "almond", "walnut", "cinnamon", "nutmeg");
    public static final Set<String> TREE_IDS;
    static {
        LinkedHashSet<String> trees = new LinkedHashSet<>();
        trees.addAll(FD_TREE_IDS);
        trees.addAll(CROPTOPIA_TREE_IDS);
        TREE_IDS = Collections.unmodifiableSet(trees);
    }

    private static <T extends Block> Supplier<T> track(Supplier<T> block) { AROMA_BLOCKS.add(block); return block; }
    private static <T extends Block> Supplier<T> plant(String id, Supplier<T> block) { PLANTS.put(id, block); return block; }

    private static Supplier<AromaFieldCropBlock> field(String id) {
        Supplier<AromaFieldCropBlock> b = track(BLOCKS.register(id + "_crop", () -> new AromaFieldCropBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT).noCollission().randomTicks(), id)));
        return plant(id, b);
    }

    private static Supplier<AromaBushCropBlock> bush(String id) {
        Supplier<AromaBushCropBlock> b = track(BLOCKS.register(id + "_crop", () -> new AromaBushCropBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SWEET_BERRY_BUSH).noCollission().randomTicks(), id)));
        return plant(id, b);
    }

    private static Supplier<AromaPineappleBlock> pineapple() {
        Supplier<AromaPineappleBlock> b = track(BLOCKS.register("pineapple_crop", () -> new AromaPineappleBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SWEET_BERRY_BUSH).noCollission().randomTicks())));
        return plant("pineapple", b);
    }

    private static Supplier<AromaLemonBlock> lemon() {
        Supplier<AromaLemonBlock> b = track(BLOCKS.register("lemon_crop", () -> new AromaLemonBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SWEET_BERRY_BUSH).noCollission().randomTicks())));
        return plant("lemon", b);
    }

    private static Supplier<AromaBerryBlock> berry() {
        Supplier<AromaBerryBlock> sapling = track(BLOCKS.register("berry_crop", () -> new AromaBerryBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SAPLING).noCollission().randomTicks())));
        track(BLOCKS.register("berry_fruiting_bottom", () -> new AromaBerryPartBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SWEET_BERRY_BUSH).noCollission().randomTicks(), false, true)));
        track(BLOCKS.register("berry_fruiting_top", () -> new AromaBerryPartBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SWEET_BERRY_BUSH).noCollission().randomTicks(), true, true)));
        track(BLOCKS.register("berry_empty_bottom", () -> new AromaBerryPartBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SWEET_BERRY_BUSH).noCollission().randomTicks(), false, false)));
        track(BLOCKS.register("berry_empty_top", () -> new AromaBerryPartBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SWEET_BERRY_BUSH).noCollission().randomTicks(), true, false)));
        return plant("berry", sapling);
    }

    private static Supplier<AromaGrapeSaplingBlock> grape(String id) {
        Supplier<AromaGrapeSaplingBlock> sapling = track(BLOCKS.register(id + "_crop", () -> new AromaGrapeSaplingBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SAPLING).noCollission().randomTicks(), id)));
        track(BLOCKS.register(id + "_fruiting_bottom", () -> new AromaGrapePartBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SAPLING).noCollission().randomTicks(), id, false, true)));
        track(BLOCKS.register(id + "_fruiting_top", () -> new AromaGrapePartBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SAPLING).noCollission().randomTicks(), id, true, true)));
        track(BLOCKS.register(id + "_empty_bottom", () -> new AromaGrapePartBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SAPLING).noCollission().randomTicks(), id, false, false)));
        track(BLOCKS.register(id + "_empty_top", () -> new AromaGrapePartBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SAPLING).noCollission().randomTicks(), id, true, false)));
        return plant(id, sapling);
    }

    private static TreeGrower grower(String id) {
        ResourceKey<ConfiguredFeature<?, ?>> key = ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceLocation.fromNamespaceAndPath(BreweryDelight.MOD_ID, "tree/" + id + "_tree"));
        return new TreeGrower(BreweryDelight.MOD_ID + ":" + id, Optional.empty(), Optional.of(key), Optional.empty());
    }

    private static Supplier<AromaFruitSaplingBlock> fdTree(String id) {
        track(BLOCKS.register(id + "_leaves", () -> new AromaFruitLeavesBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES).randomTicks(), id)));
        Supplier<AromaFruitSaplingBlock> sapling = track(BLOCKS.register(id + "_crop", () -> new AromaFruitSaplingBlock(grower(id), BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SAPLING).noCollission().randomTicks(), id)));
        return plant(id, sapling);
    }

    private static Supplier<AromaFruitSaplingBlock> croptopiaTree(String id) {
        Supplier<AromaTreeCropBlock> leafCrop = track(BLOCKS.register(id + "_tree_crop", () -> new AromaTreeCropBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES).randomTicks(), id)));
        CROPTOPIA_LEAF_BLOCKS.add(leafCrop);
        Supplier<AromaFruitSaplingBlock> sapling = track(BLOCKS.register(id + "_crop", () -> new AromaFruitSaplingBlock(grower(id), BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SAPLING).noCollission().randomTicks(), id)));
        return plant(id, sapling);
    }

    public static final Supplier<AromaGrapeSaplingBlock> RED_GRAPE = grape("red_grape");
    public static final Supplier<AromaGrapeSaplingBlock> WHITE_GRAPE = grape("white_grape");
    public static final Supplier<AromaBerryBlock> BERRY = berry();

    public static final Supplier<AromaFruitSaplingBlock> APPLE = fdTree("apple");
    public static final Supplier<AromaFruitSaplingBlock> PEAR = fdTree("pear");
    public static final Supplier<AromaFruitSaplingBlock> PEACH = fdTree("peach");
    public static final Supplier<AromaFruitSaplingBlock> ORANGE = fdTree("orange");
    public static final Supplier<AromaFruitSaplingBlock> MANGO = fdTree("mango");
    public static final Supplier<AromaFruitSaplingBlock> KIWI = fdTree("kiwi");

    public static final Supplier<AromaFruitSaplingBlock> CHERRY = croptopiaTree("cherry");
    public static final Supplier<AromaFruitSaplingBlock> PLUM = croptopiaTree("plum");
    public static final Supplier<AromaFruitSaplingBlock> APRICOT = croptopiaTree("apricot");
    public static final Supplier<AromaFruitSaplingBlock> LIME = croptopiaTree("lime");
    public static final Supplier<AromaFruitSaplingBlock> BANANA = croptopiaTree("banana");
    public static final Supplier<AromaFruitSaplingBlock> ALMOND = croptopiaTree("almond");
    public static final Supplier<AromaFruitSaplingBlock> WALNUT = croptopiaTree("walnut");
    public static final Supplier<AromaFruitSaplingBlock> CINNAMON = croptopiaTree("cinnamon");
    public static final Supplier<AromaFruitSaplingBlock> NUTMEG = croptopiaTree("nutmeg");

    public static final Supplier<AromaLemonBlock> LEMON = lemon();
    public static final Supplier<AromaPineappleBlock> PINEAPPLE = pineapple();
    public static final Supplier<AromaBushCropBlock> AGAVE = bush("agave");
    public static final Supplier<AromaBushCropBlock> JUNIPER = bush("juniper");

    public static final Supplier<AromaFieldCropBlock> RICE = field("rice");
    public static final Supplier<AromaFieldCropBlock> BARLEY = field("barley");
    public static final Supplier<AromaFieldCropBlock> CORN = field("corn");
    public static final Supplier<AromaFieldCropBlock> SWEET_POTATO = field("sweet_potato");
    public static final Supplier<AromaFieldCropBlock> HOPS = field("hops");
    public static final Supplier<AromaFieldCropBlock> TEA = field("tea");
    public static final Supplier<AromaFieldCropBlock> RYE = field("rye");
    public static final Supplier<AromaFieldCropBlock> MILLET = field("millet");
    public static final Supplier<AromaFieldCropBlock> ANISE = field("anise");
    public static final Supplier<AromaFieldCropBlock> COFFEE = field("coffee");
    public static final Supplier<AromaFieldCropBlock> VANILLA = field("vanilla");
    public static final Supplier<AromaFieldCropBlock> GINGER = field("ginger");

    public static final Supplier<Block> COOKING_POT = BLOCKS.register("cooking_pot", () -> new CookingPotBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).strength(2.0F)));

    private ModBlocks() {}
}
