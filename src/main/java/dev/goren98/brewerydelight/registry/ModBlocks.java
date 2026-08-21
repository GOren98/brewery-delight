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
    /** Stable planting block for each produce id. */
    public static final Map<String, Supplier<? extends Block>> PLANTS = new LinkedHashMap<>();
    /** Every block that stores Aroma in an AromaCropBlockEntity. */
    public static final List<Supplier<? extends Block>> AROMA_BLOCKS = new ArrayList<>();
    public static final Set<String> TREE_IDS = Set.of("apple","pear","peach","orange","mango","kiwi","cherry","plum","apricot","lime","banana","almond","walnut","cinnamon","nutmeg");

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
    private static Supplier<AromaBerryBlock> berry() {
        Supplier<AromaBerryBlock> b = track(BLOCKS.register("berry_crop", () -> new AromaBerryBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SWEET_BERRY_BUSH).noCollission().randomTicks())));
        return plant("berry", b);
    }
    private static Supplier<AromaGrapeSaplingBlock> grape(String id) {
        Supplier<AromaGrapeSaplingBlock> sapling = track(BLOCKS.register(id + "_crop", () -> new AromaGrapeSaplingBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SAPLING).noCollission().randomTicks(), id)));
        track(BLOCKS.register(id + "_fruiting_bottom", () -> new AromaGrapePartBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SAPLING).noCollission().randomTicks(), id, false, true)));
        track(BLOCKS.register(id + "_fruiting_top", () -> new AromaGrapePartBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SAPLING).noCollission().randomTicks(), id, true, true)));
        track(BLOCKS.register(id + "_empty_bottom", () -> new AromaGrapePartBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SAPLING).noCollission().randomTicks(), id, false, false)));
        track(BLOCKS.register(id + "_empty_top", () -> new AromaGrapePartBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SAPLING).noCollission().randomTicks(), id, true, false)));
        return plant(id, sapling);
    }
    private static Supplier<AromaFruitSaplingBlock> tree(String id) {
        Supplier<AromaFruitLeavesBlock> leaves = track(BLOCKS.register(id + "_leaves", () -> new AromaFruitLeavesBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES).randomTicks(), id)));
        ResourceKey<ConfiguredFeature<?, ?>> key = ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceLocation.fromNamespaceAndPath(BreweryDelight.MOD_ID, "tree/" + id + "_tree"));
        TreeGrower grower = new TreeGrower(BreweryDelight.MOD_ID + ":" + id, Optional.empty(), Optional.of(key), Optional.empty());
        Supplier<AromaFruitSaplingBlock> sapling = track(BLOCKS.register(id + "_crop", () -> new AromaFruitSaplingBlock(grower, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SAPLING).noCollission().randomTicks(), id)));
        return plant(id, sapling);
    }

    public static final Supplier<AromaGrapeSaplingBlock> RED_GRAPE = grape("red_grape");
    public static final Supplier<AromaGrapeSaplingBlock> WHITE_GRAPE = grape("white_grape");
    public static final Supplier<AromaBerryBlock> BERRY = berry();

    public static final Supplier<AromaFruitSaplingBlock> APPLE = tree("apple");
    public static final Supplier<AromaFruitSaplingBlock> PEAR = tree("pear");
    public static final Supplier<AromaFruitSaplingBlock> PEACH = tree("peach");
    public static final Supplier<AromaFruitSaplingBlock> ORANGE = tree("orange");
    public static final Supplier<AromaFruitSaplingBlock> MANGO = tree("mango");
    public static final Supplier<AromaFruitSaplingBlock> KIWI = tree("kiwi");
    public static final Supplier<AromaFruitSaplingBlock> CHERRY = tree("cherry");
    public static final Supplier<AromaFruitSaplingBlock> PLUM = tree("plum");
    public static final Supplier<AromaFruitSaplingBlock> APRICOT = tree("apricot");
    public static final Supplier<AromaFruitSaplingBlock> LIME = tree("lime");
    public static final Supplier<AromaFruitSaplingBlock> BANANA = tree("banana");
    public static final Supplier<AromaFruitSaplingBlock> ALMOND = tree("almond");
    public static final Supplier<AromaFruitSaplingBlock> WALNUT = tree("walnut");
    public static final Supplier<AromaFruitSaplingBlock> CINNAMON = tree("cinnamon");
    public static final Supplier<AromaFruitSaplingBlock> NUTMEG = tree("nutmeg");

    public static final Supplier<AromaBushCropBlock> LEMON = bush("lemon");
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
