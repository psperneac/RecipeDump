package com.ikarsoft.rd;

import com.google.common.base.Preconditions;
import com.ikarsoft.rd.ingredients.IngredientRegistry;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nullable;

/**
 * For JEI internal use only, these are normally accessed from the API.
 */
public final class Internal {
	@Nullable
	private static StackHelper stackHelper;
	@Nullable
	private static IngredientRegistry ingredientRegistry;

	private Internal() {

	}

	public static IngredientRegistry getIngredientRegistry() {
		Preconditions.checkState(ingredientRegistry != null, "Ingredient Registry has not been created yet.");
		return ingredientRegistry;
	}

	public static void setIngredientRegistry(IngredientRegistry ingredientRegistry) {
		Internal.ingredientRegistry = ingredientRegistry;
	}
}
