package dev.goren98.brewerydelight.crop;

import dev.goren98.brewerydelight.BreweryDelight;
import dev.goren98.brewerydelight.registry.ModComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemLike;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class AromaCropBlock extends CropBlock implements EntityBlock {
    private final String cropId;
    private final String defaultAroma;
    private final boolean orchard;

    public AromaCropBlock(BlockBehaviour.Properties properties, String cropId, String defaultAroma, boolean orchard) {
        super(properties);
        this.cropId = cropId;
        this.defaultAroma = defaultAroma;
        this.orchard = orchard;
    }

    @Override
    public int getMaxAge() {
        return 3;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return orchard ? state.is(net.minecraft.tags.BlockTags.DIRT) : super.mayPlaceOn(state, level, pos);
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(BreweryDelight.MOD_ID, cropId + "_seeds"));
    }

    private Item produceItem() {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(BreweryDelight.MOD_ID, cropId));
    }

    private String aromaAt(LevelReader level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        return be instanceof AromaCropBlockEntity crop ? crop.getAroma(defaultAroma) : defaultAroma;
    }

    private ItemStack withAroma(Item item, int count, String aroma) {
        ItemStack stack = new ItemStack(item, count);
        stack.set(ModComponents.CROP_AROMA.get(), aroma);
        return stack;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (getAge(state) < getMaxAge()) return InteractionResult.PASS;
        if (!level.isClientSide) {
            String aroma = aromaAt(level, pos);
            level.setBlock(pos, getStateForAge(0), Block.UPDATE_CLIENTS);
            BlockEntity fresh = level.getBlockEntity(pos);
            if (fresh instanceof AromaCropBlockEntity crop) crop.setAroma(aroma);
            popResource(level, pos, withAroma(produceItem(), 2, aroma));
            popResource(level, pos, withAroma(getBaseSeedId().asItem(), 1, aroma));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AromaCropBlockEntity(pos, state);
    }
}
