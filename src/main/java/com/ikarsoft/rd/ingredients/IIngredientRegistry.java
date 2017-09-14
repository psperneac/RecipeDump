package com.ikarsoft.rd.ingredients;

import net.minecraft.item.ItemStack;

import java.util.Collection;
import java.util.List;

/**
 * The IIngredientRegistry is provided by JEI and has some useful functions related to recipe ingredients.
 * Get the instance from {@link IModRegistry#getIngredientRegistry()}.
 *
 * @since JEI 3.11.0
 */
public interface IIngredientRegistry {
	<V> List<V> getIngredients(Class<V> ingredientClass);

	/**
	 * Returns an unmodifiable collection of all the ingredients known to JEI, of the specified class.
	 * @since JEI 4.7.3
	 */
	<V> Collection<V> getAllIngredients(Class<V> ingredientClass);

	/**
	 * Returns the appropriate ingredient helper for this ingredient.
	 */
	<V> IIngredientHelper<V> getIngredientHelper(V ingredient);

	/**
	 * Returns the appropriate ingredient helper for this ingredient class
	 */
	<V> IIngredientHelper<V> getIngredientHelper(Class<? extends V> ingredientClass);

	/**
	 * Returns an unmodifiable collection of all registered ingredient classes.
	 * Without addons, there is ItemStack.class and FluidStack.class.
	 */
	Collection<Class> getRegisteredIngredientClasses();

	/**
	 * Returns an unmodifiable list of all the ItemStacks that can be used as fuel in a vanilla furnace.
	 */
	List<ItemStack> getFuels();

	/**
	 * Returns an unmodifiable list of all the ItemStacks that return true to isPotionIngredient.
	 */
	List<ItemStack> getPotionIngredients();
}
