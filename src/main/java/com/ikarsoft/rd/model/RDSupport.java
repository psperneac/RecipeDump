package com.ikarsoft.rd.model;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.List;

public class RDSupport {
    public static RDRecipe create(IRecipe recipe) {
        RDRecipe ret = new RDRecipe();
        ret.setRecipeOutput(create(recipe.getRecipeOutput()));
        ret.setGroup(recipe.getGroup());
        ret.setHidden(recipe.isHidden());
        if(recipe.getIngredients()!=null) {
            List<RDIngredient> ingredients = new ArrayList<>();
            for (Ingredient ingredient : recipe.getIngredients()) {
                ingredients.add(create(ingredient));
            }
            ret.setIngredients(ingredients);
        }

        return ret;
    }

    private static RDIngredient create(Ingredient ingredient) {
        RDIngredient ret = new RDIngredient();

        if(ingredient.getMatchingStacks()!=null) {
            List<RDItemStack> matchingStacks = new ArrayList<>();
            for(ItemStack itemStack: ingredient.getMatchingStacks()) {
                matchingStacks.add(create(itemStack));
            }
            ret.setMatchingStacks(matchingStacks);
        }

        return ret;
    }

    public static RDItemStack create(ItemStack itemStack) {
        RDItemStack ret = new RDItemStack();

        ret.setItem(create(itemStack.getItem()));

        return ret;
    }

    private static RDItem create(Item item) {
        RDItem ret = new RDItem();

        ret.setId(Item.getIdFromItem(item));
        ret.setUnlocalizedName(item.getUnlocalizedName());

        return ret;
    }
}
