package com.ikarsoft.rd;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;

import java.util.EnumMap;
import java.util.Map;

public class StackHelper {

    /**
     * Uids are cached during loading to improve startup performance.
     */
    private final Map<UidMode, Map<ItemStack, String>> uidCache = new EnumMap<>(UidMode.class);
    private boolean uidCacheEnabled = true;
    private ISubtypeRegistry subtypeRegistry;

    private static StackHelper __instance;

    public enum UidMode {
        NORMAL, WILDCARD, FULL
    }

    private StackHelper() {

    }

    public static StackHelper instance() {
        if(__instance == null) {
            __instance = new StackHelper();
        }

        return __instance;
    }

    public String getUniqueIdentifierForStack(ItemStack stack) {
        return getUniqueIdentifierForStack(stack, UidMode.NORMAL);
    }

    public String getUniqueIdentifierForStack(ItemStack stack, UidMode mode) {
        ErrorHelper.checkNotEmpty(stack, "stack");
        if (uidCacheEnabled) {
            String result = uidCache.get(mode).get(stack);
            if (result != null) {
                return result;
            }
        }

        Item item = stack.getItem();
        ResourceLocation itemName = item.getRegistryName();
        if (itemName == null) {
            String stackInfo = ErrorHelper.getItemStackInfo(stack);
            throw new IllegalStateException("Item has no registry name: " + stackInfo);
        }

        StringBuilder itemKey = new StringBuilder(itemName.toString());

        int metadata = stack.getMetadata();
        if (mode != UidMode.WILDCARD && metadata != OreDictionary.WILDCARD_VALUE) {
            String subtypeInfo = subtypeRegistry.getSubtypeInfo(stack);
            if (subtypeInfo != null) {
                itemKey.append(':').append(subtypeInfo);
            } else {
                if (mode == UidMode.FULL) {
                    itemKey.append(':').append(metadata);

                    NBTTagCompound serializedNbt = stack.serializeNBT();
                    NBTTagCompound nbtTagCompound = serializedNbt.getCompoundTag("tag").copy();
                    if (serializedNbt.hasKey("ForgeCaps")) {
                        NBTTagCompound forgeCaps = serializedNbt.getCompoundTag("ForgeCaps");
                        if (!forgeCaps.hasNoTags()) { // ForgeCaps should never be empty
                            nbtTagCompound.setTag("ForgeCaps", forgeCaps);
                        }
                    }
                    if (!nbtTagCompound.hasNoTags()) {
                        itemKey.append(':').append(nbtTagCompound);
                    }
                } else if (stack.getHasSubtypes()) {
                    itemKey.append(':').append(metadata);
                }
            }
        }

        String result = itemKey.toString();
        if (uidCacheEnabled) {
            uidCache.get(mode).put(stack, result);
        }
        return result;
    }

}
