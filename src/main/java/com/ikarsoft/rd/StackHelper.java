package com.ikarsoft.rd;

import com.ikarsoft.rd.ingredients.SubtypeRegistry;
import com.ikarsoft.rd.support.UniqueItemStackListBuilder;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.oredict.OreDictionary;

import com.ikarsoft.rd.ingredients.ItemStackListFactory.FluidSubtypeInterpreter;

import javax.annotation.Nullable;
import java.util.*;

public class StackHelper {

    /**
     * Uids are cached during loading to improve startup performance.
     */
    private final Map<UidMode, Map<ItemStack, String>> uidCache = new EnumMap<>(UidMode.class);
    private boolean uidCacheEnabled = true;

    private static StackHelper __instance;

    public enum UidMode {
        NORMAL, WILDCARD, FULL
    }

    private StackHelper() {
        for (UidMode mode : UidMode.values()) {
            uidCache.put(mode, new IdentityHashMap<>());
        }
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
            String subtypeInfo = SubtypeRegistry.instance().getSubtypeInfo(stack);
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

    public void addFallbackSubtypeInterpreter(ItemStack itemStack) {
        if (!SubtypeRegistry.instance().hasSubtypeInterpreter(itemStack)) {
            if (itemStack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
                SubtypeRegistry.instance().registerSubtypeInterpreter(itemStack.getItem(), FluidSubtypeInterpreter.INSTANCE);
            }
        }
    }

    public List<ItemStack> getSubtypes(@Nullable ItemStack itemStack) {
        if (itemStack == null) {
            return Collections.emptyList();
        }

        if (itemStack.isEmpty()) {
            return Collections.emptyList();
        }

        if (itemStack.getItemDamage() != OreDictionary.WILDCARD_VALUE) {
            List<ItemStack> subtypes = new ArrayList<>();
            subtypes.add(itemStack);
            return subtypes;
        }

        return getSubtypes(itemStack.getItem(), itemStack.getCount());
    }

    public NonNullList<ItemStack> getSubtypes(final Item item, final int stackSize) {
        NonNullList<ItemStack> itemStacks = NonNullList.create();

        for (CreativeTabs itemTab : item.getCreativeTabs()) {
            if (itemTab == null) {
                itemStacks.add(new ItemStack(item, stackSize));
            } else {
                NonNullList<ItemStack> subItems = NonNullList.create();
                try {
                    item.getSubItems(itemTab, subItems);
                } catch (RuntimeException | LinkageError e) {
                    Log.get().warn("Caught a crash while getting sub-items of {}", item, e);
                }

                for (ItemStack subItem : subItems) {
                    if (subItem.isEmpty()) {
                        Log.get().warn("Found an empty subItem of {}", item);
                    } else {
                        if (subItem.getCount() != stackSize) {
                            ItemStack subItemCopy = subItem.copy();
                            subItemCopy.setCount(stackSize);
                            itemStacks.add(subItemCopy);
                        } else {
                            itemStacks.add(subItem);
                        }
                    }
                }
            }
        }

        return itemStacks;
    }

    public List<List<ItemStack>> expandRecipeItemStackInputs(@Nullable List inputs) {
        if (inputs == null) {
            return Collections.emptyList();
        }

        return expandRecipeItemStackInputs(inputs, true);
    }

    public List<List<ItemStack>> expandRecipeItemStackInputs(List inputs, boolean expandSubtypes) {
        List<List<ItemStack>> expandedInputs = new ArrayList<>();
        for (Object input : inputs) {
            List<ItemStack> expandedInput = toItemStackList(input, expandSubtypes);
            expandedInputs.add(expandedInput);
        }
        return expandedInputs;
    }

    public NonNullList<ItemStack> toItemStackList(@Nullable Object stacks) {
        if (stacks == null) {
            return NonNullList.create();
        }

        return toItemStackList(stacks, true);
    }

    public NonNullList<ItemStack> toItemStackList(Object stacks, boolean expandSubtypes) {
        UniqueItemStackListBuilder itemStackListBuilder = new UniqueItemStackListBuilder();
        toItemStackList(itemStackListBuilder, stacks, expandSubtypes);
        return itemStackListBuilder.build();
    }

    private void toItemStackList(UniqueItemStackListBuilder itemStackListBuilder, @Nullable Object input, boolean expandSubtypes) {
        if (input instanceof ItemStack) {
            ItemStack stack = (ItemStack) input;
            if (expandSubtypes && stack.getMetadata() == OreDictionary.WILDCARD_VALUE) {
                List<ItemStack> subtypes = getSubtypes(stack);
                for (ItemStack subtype : subtypes) {
                    itemStackListBuilder.add(subtype);
                }
            } else {
                itemStackListBuilder.add(stack);
            }
        } else if (input instanceof String) {
            List<ItemStack> stacks = OreDictionary.getOres((String) input);
            toItemStackList(itemStackListBuilder, stacks, expandSubtypes);
        } else if (input instanceof Ingredient) {
            List<ItemStack> stacks = Arrays.asList(((Ingredient) input).getMatchingStacks());
            toItemStackList(itemStackListBuilder, stacks, expandSubtypes);
        } else if (input instanceof Iterable) {
            for (Object obj : (Iterable) input) {
                toItemStackList(itemStackListBuilder, obj, expandSubtypes);
            }
        } else if (input != null) {
            Log.get().error("Unknown object found: {}", input);
        }
    }
}
