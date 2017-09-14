package com.ikarsoft.rd.ingredients;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.ikarsoft.rd.ErrorHelper;
import com.ikarsoft.rd.Log;
import com.ikarsoft.rd.support.IngredientSet;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionHelper;
import net.minecraft.tileentity.TileEntityFurnace;

import java.util.*;

public class IngredientRegistry implements IIngredientRegistry {
	private final Map<Class, IngredientSet> ingredientsMap;
	private final ImmutableMap<Class, IIngredientHelper> ingredientHelperMap;
	private final List<ItemStack> fuels = new ArrayList<>();
	private final List<ItemStack> potionIngredients = new ArrayList<>();

	public IngredientRegistry(Map<Class, IngredientSet> ingredientsMap, ImmutableMap<Class, IIngredientHelper> ingredientHelperMap) {
		this.ingredientsMap = ingredientsMap;
		this.ingredientHelperMap = ingredientHelperMap;

		for (ItemStack itemStack : getAllIngredients(ItemStack.class)) {
			getStackProperties(itemStack);
		}
	}

	private void getStackProperties(ItemStack itemStack) {
		try {
			if (TileEntityFurnace.isItemFuel(itemStack)) {
				fuels.add(itemStack);
			}
		} catch (RuntimeException | LinkageError e) {
			String itemStackInfo = ErrorHelper.getItemStackInfo(itemStack);
			Log.get().error("Failed to check if item is fuel {}.", itemStackInfo, e);
		}

		try {
			if (PotionHelper.isReagent(itemStack)) {
				potionIngredients.add(itemStack);
			}
		} catch (RuntimeException | LinkageError e) {
			String itemStackInfo = ErrorHelper.getItemStackInfo(itemStack);
			Log.get().error("Failed to check if item is a potion ingredient {}.", itemStackInfo, e);
		}
	}

	@Override
	public <V> List<V> getIngredients(Class<V> ingredientClass) {
		ErrorHelper.checkNotNull(ingredientClass, "ingredientClass");

		//noinspection unchecked
		IngredientSet<V> ingredients = ingredientsMap.get(ingredientClass);
		if (ingredients == null) {
			return ImmutableList.of();
		} else {
			return ImmutableList.copyOf(ingredients);
		}
	}

	@Override
	public <V> Collection<V> getAllIngredients(Class<V> ingredientClass) {
		//noinspection unchecked
		IngredientSet<V> ingredients = ingredientsMap.get(ingredientClass);
		if (ingredients == null) {
			return Collections.emptySet();
		} else {
			return Collections.unmodifiableCollection(ingredients);
		}
	}

	public <V> boolean isValidIngredient(V ingredient) {
		//noinspection unchecked
		IIngredientHelper<V> ingredientHelper = ingredientHelperMap.get(ingredient.getClass());
		return ingredientHelper != null && ingredientHelper.isValidIngredient(ingredient);
	}

	@Override
	public <V> IIngredientHelper<V> getIngredientHelper(V ingredient) {
		ErrorHelper.checkNotNull(ingredient, "ingredient");

		//noinspection unchecked
		return (IIngredientHelper<V>) getIngredientHelper(ingredient.getClass());
	}

	@Override
	public <V> IIngredientHelper<V> getIngredientHelper(Class<? extends V> ingredientClass) {
		ErrorHelper.checkNotNull(ingredientClass, "ingredientClass");

		//noinspection unchecked
		IIngredientHelper<V> ingredientHelper = ingredientHelperMap.get(ingredientClass);
		if (ingredientHelper == null) {
			throw new IllegalArgumentException("Unknown ingredient type: " + ingredientClass);
		}
		return ingredientHelper;
	}

	@Override
	public Collection<Class> getRegisteredIngredientClasses() {
		return Collections.unmodifiableCollection(ingredientsMap.keySet());
	}

	@Override
	public List<ItemStack> getFuels() {
		return Collections.unmodifiableList(fuels);
	}

	@Override
	public List<ItemStack> getPotionIngredients() {
		return Collections.unmodifiableList(potionIngredients);
	}
}
