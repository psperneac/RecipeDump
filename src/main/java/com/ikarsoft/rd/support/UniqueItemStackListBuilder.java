package com.ikarsoft.rd.support;

import com.ikarsoft.rd.Log;
import com.ikarsoft.rd.StackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import java.util.HashSet;
import java.util.Set;

public class UniqueItemStackListBuilder {
    private final NonNullList<ItemStack> ingredients = NonNullList.create();
    private final Set<String> ingredientUids = new HashSet<>();

    public void add(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return;
        }
        try {
            String uid = StackHelper.instance().getUniqueIdentifierForStack(itemStack);
            if (!ingredientUids.contains(uid)) {
                ingredientUids.add(uid);
                ingredients.add(itemStack);
            }
        } catch (RuntimeException | LinkageError e) {
            Log.get().error("Failed to get unique identifier for stack.", e);
        }
    }

    public NonNullList<ItemStack> build() {
        return ingredients;
    }
}