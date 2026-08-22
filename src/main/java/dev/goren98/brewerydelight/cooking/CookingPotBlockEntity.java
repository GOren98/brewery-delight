package dev.goren98.brewerydelight.cooking;

import dev.goren98.brewerydelight.cooking.recipe.BaseCookingInput;
import dev.goren98.brewerydelight.cooking.recipe.BaseCookingRecipe;
import dev.goren98.brewerydelight.registry.ModBlockEntities;
import dev.goren98.brewerydelight.registry.ModItems;
import dev.goren98.brewerydelight.registry.ModRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CookingPotBlockEntity extends BaseContainerBlockEntity {
    // Farmer's Delight Cooking Pot inventory contract: 0-5 ingredients, 6 meal/base,
    // 7 serving container, 8 served output.
    public static final int SLOT_INGREDIENT_START = 0;
    public static final int SLOT_INGREDIENT_END = 5;
    public static final int SLOT_MEAL = 6;
    public static final int SLOT_CONTAINER = 7;
    public static final int SLOT_OUTPUT = 8;
    public static final int SIZE = 9;
    public static final int DEFAULT_COOK_TIME = 100;

    private NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
    private int progress;
    private int cookTime = DEFAULT_COOK_TIME;
    private final ContainerData data = new SimpleContainerData(2) {
        @Override public int get(int index) { return index == 0 ? progress : cookTime; }
        @Override public void set(int index, int value) { if (index == 0) progress = value; else if (index == 1) cookTime = Math.max(1, value); }
    };

    public CookingPotBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COOKING_POT.get(), pos, state);
    }

    @Override
    protected Component getDefaultName() {
        return Component.literal("Brewing Pot");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new CookingPotMenu(containerId, inventory, this, data);
    }

    @Override protected NonNullList<ItemStack> getItems() { return items; }
    @Override protected void setItems(NonNullList<ItemStack> items) { this.items = items; }
    @Override public int getContainerSize() { return SIZE; }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Progress", progress);
        tag.putInt("CookTime", cookTime);
        ContainerHelper.saveAllItems(tag, items, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        progress = tag.getInt("Progress");
        cookTime = tag.contains("CookTime") ? Math.max(1, tag.getInt("CookTime")) : DEFAULT_COOK_TIME;
        items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CookingPotBlockEntity pot) {
        if (pot.tryServeOne()) {
            setChanged(level, pos, state);
        }

        Optional<RecipeHolder<BaseCookingRecipe>> match = pot.findBaseRecipe(level);
        if (match.isPresent() && pot.items.get(SLOT_MEAL).isEmpty()) {
            BaseCookingRecipe recipe = match.get().value();
            pot.cookTime = recipe.cookingTime();
            pot.progress++;
            if (pot.progress >= pot.cookTime) {
                pot.craftBase(recipe, level);
                pot.progress = 0;
                setChanged(level, pos, state);
            }
        } else if (pot.progress != 0) {
            pot.progress = 0;
            pot.cookTime = DEFAULT_COOK_TIME;
            setChanged(level, pos, state);
        }
    }

    private BaseCookingInput currentInput() {
        List<ItemStack> stacks = new ArrayList<>();
        for (int i = SLOT_INGREDIENT_START; i <= SLOT_INGREDIENT_END; i++) {
            ItemStack stack = items.get(i);
            if (!stack.isEmpty()) stacks.add(stack);
        }
        return new BaseCookingInput(stacks);
    }

    private Optional<RecipeHolder<BaseCookingRecipe>> findBaseRecipe(Level level) {
        BaseCookingInput input = currentInput();
        if (input.size() == 0) return Optional.empty();
        return level.getRecipeManager().getRecipeFor(ModRecipes.BASE_COOKING_TYPE.get(), input, level);
    }

    private void craftBase(BaseCookingRecipe recipe, Level level) {
        BaseCookingInput input = currentInput();
        ItemStack base = recipe.assemble(input, level.registryAccess());
        if (base.isEmpty()) return;

        for (int i = SLOT_INGREDIENT_START; i <= SLOT_INGREDIENT_END; i++) {
            ItemStack stack = items.get(i);
            if (stack.isEmpty()) continue;
            ItemStack remainder = stack.hasCraftingRemainingItem() ? stack.getCraftingRemainingItem() : ItemStack.EMPTY;
            stack.shrink(1);
            if (stack.isEmpty() && !remainder.isEmpty()) items.set(i, remainder);
        }

        items.set(SLOT_MEAL, base);
        setChanged();
    }

    private boolean tryServeOne() {
        ItemStack meal = items.get(SLOT_MEAL);
        ItemStack container = items.get(SLOT_CONTAINER);
        ItemStack output = items.get(SLOT_OUTPUT);

        if (meal.isEmpty() || !meal.is(ModItems.BASE_BOTTLE.get()) || !container.is(Items.GLASS_BOTTLE)) return false;

        ItemStack serving = meal.copy();
        serving.setCount(1);
        if (!output.isEmpty() && (!ItemStack.isSameItemSameComponents(output, serving) || output.getCount() >= output.getMaxStackSize())) {
            return false;
        }

        container.shrink(1);
        meal.shrink(1);

        if (output.isEmpty()) items.set(SLOT_OUTPUT, serving);
        else output.grow(1);
        setChanged();
        return true;
    }
}
