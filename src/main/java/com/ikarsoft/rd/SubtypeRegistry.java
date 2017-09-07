package com.ikarsoft.rd;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;
import java.util.IdentityHashMap;
import java.util.Map;

public class SubtypeRegistry implements ISubtypeRegistry {
    private final Map<Item, ISubtypeInterpreter> interpreters = new IdentityHashMap<>();

    @Override
    public void useNbtForSubtypes(Item... items) {
        for (Item item : items) {
            registerSubtypeInterpreter(item, AllNbt.INSTANCE);
        }
    }

    @Override
    public void registerNbtInterpreter(Item item, ISubtypeInterpreter interpreter) {
        registerSubtypeInterpreter(item, interpreter);
    }

    @Override
    public void registerSubtypeInterpreter(Item item, ISubtypeInterpreter interpreter) {
        ErrorHelper.checkNotNull(item, "item ");
        ErrorHelper.checkNotNull(interpreter, "interpreter");

        if (interpreters.containsKey(item)) {
            Log.get().error("An interpreter is already registered for this item: {}", item, new IllegalArgumentException());
            return;
        }

        interpreters.put(item, interpreter);
    }

    @Nullable
    @Override
    public String getSubtypeInfo(ItemStack itemStack) {
        ErrorHelper.checkNotEmpty(itemStack);

        Item item = itemStack.getItem();
        ISubtypeInterpreter subtypeInterpreter = interpreters.get(item);
        if (subtypeInterpreter != null) {
            return subtypeInterpreter.getSubtypeInfo(itemStack);
        }

        return null;
    }

    @Override
    public boolean hasSubtypeInterpreter(ItemStack itemStack) {
        ErrorHelper.checkNotEmpty(itemStack);

        Item item = itemStack.getItem();
        return interpreters.containsKey(item);
    }

    private static class AllNbt implements ISubtypeInterpreter {
        public static final AllNbt INSTANCE = new AllNbt();

        private AllNbt() {
        }

        @Nullable
        @Override
        public String getSubtypeInfo(ItemStack itemStack) {
            NBTTagCompound nbtTagCompound = itemStack.getTagCompound();
            if (nbtTagCompound == null || nbtTagCompound.hasNoTags()) {
                return null;
            }
            return nbtTagCompound.toString();
        }
    }
}