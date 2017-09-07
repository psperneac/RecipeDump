package com.ikarsoft.rd;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nullable;
import java.io.*;
import java.util.*;

public class RecipeDumpStarter {

    protected boolean started = false;

    protected ObjectMapper mapper = new ObjectMapper();

    protected StackHelper stackHelper = StackHelper.instance();

    public void start() {

        long startedTime = System.currentTimeMillis();

        File ff = new File("test.recipedump.json");
        Log.get().info("Writing recipe dump to "+ff.getAbsolutePath());
        try(PrintWriter out = new PrintWriter(new FileWriter(ff))) {

            loadIngredients();

            List<IRecipe> craftingManagerRecipes = new ArrayList<>();
            Iterator<IRecipe> recipeIterator = CraftingManager.REGISTRY.iterator();
            while(recipeIterator.hasNext()) {
                IRecipe recipe = recipeIterator.next();
                craftingManagerRecipes.add(recipe);
                //mapper.writeValue(out, recipe);
                Log.get().info("Recipe: "+recipe.toString());
            }

            for(IRecipe recipe: craftingManagerRecipes) {
                if(recipe instanceof ShapelessRecipes) {
                    ShapelessRecipes sRecipe = (ShapelessRecipes) recipe;
                    String group = sRecipe.getGroup();
                    ItemStack output = sRecipe.getRecipeOutput();
                    List<Ingredient> ingredients = sRecipe.getIngredients();
                    ResourceLocation registryName = sRecipe.getRegistryName();

                    Log.get().info("Registry name: "+registryName.toString()+"Group: "+group+" Output: "+output+" ingredients: "+ingredients);
                }
            }

//            List<IRecipe> forgeRegistryRecipes = new ArrayList<>();
//            List<IRecipe> recipes = ForgeRegistries.RECIPES.getValues();
//            for(IRecipe recipe: recipes) {
//                forgeRegistryRecipes.add(recipe);
//                Log.get().info("Recipe: "+recipe.toString());
//            }

            IForgeRegistry<Block> blockRegistry = GameRegistry.findRegistry(Block.class);
            Set<ResourceLocation> blockKeys = blockRegistry.getKeys();
            Log.get().info("BLOCKS! "+blockKeys.size());
            for(ResourceLocation rl: blockKeys) {
                Log.get().info(blockRegistry.getValue(rl));
                //mapper.writeValue(out, blockRegistry.getValue(rl));
            }

            out.println("THIS IS A TEST");
        } catch (IOException e) {
            e.printStackTrace();
        }

        long endedTime = System.currentTimeMillis();
        Log.get().info("RecipeDump started in "+(endedTime-startedTime)+" ms.");
        started = true;
    }

    private void loadIngredients() {
        final List<ItemStack> itemList = new ArrayList<>();
        final Set<String> itemNameSet = new HashSet<>();

        for (CreativeTabs creativeTab : CreativeTabs.CREATIVE_TAB_ARRAY) {
            if (creativeTab == CreativeTabs.HOTBAR) {
                continue;
            }
            NonNullList<ItemStack> creativeTabItemStacks = NonNullList.create();
            try {
                creativeTab.displayAllRelevantItems(creativeTabItemStacks);
            } catch (RuntimeException | LinkageError e) {
                Log.get().error("Creative tab crashed while getting items. Some items from this tab will be missing from the item list. {}", creativeTab, e);
            }
            for (ItemStack itemStack : creativeTabItemStacks) {
                subtypeCount.add(itemStack.getItem());
            }
            for (ItemStack itemStack : creativeTabItemStacks) {
                if (itemStack.isEmpty()) {
                    Log.get().error("Found an empty itemStack from creative tab: {}", creativeTab);
                } else {
                    addItemStack(stackHelper, itemStack, itemList, itemNameSet);
                }
            }
        }

        for (Block block : ForgeRegistries.BLOCKS) {
            addBlockAndSubBlocks(stackHelper, block, itemList, itemNameSet);
        }

        for (Item item : ForgeRegistries.ITEMS) {
            addItemAndSubItems(stackHelper, item, itemList, itemNameSet);
        }

        return itemList;

    }

    private void addItemAndSubItems(StackHelper stackHelper, @Nullable Item item, List<ItemStack> itemList, Set<String> itemNameSet) {
        if (item == null || item == Items.AIR) {
            return;
        }

        NonNullList<ItemStack> items = stackHelper.getSubtypes(item, 1);
        subtypeCount.setCount(item, items.size());
        for (ItemStack stack : items) {
            addItemStack(stackHelper, stack, itemList, itemNameSet);
        }
    }

    private void addBlockAndSubBlocks(StackHelper stackHelper, @Nullable Block block, List<ItemStack> itemList, Set<String> itemNameSet) {
        if (block == null) {
            return;
        }

        Item item = Item.getItemFromBlock(block);
        if (item == Items.AIR) {
            return;
        }

        for (CreativeTabs itemTab : item.getCreativeTabs()) {
            NonNullList<ItemStack> subBlocks = NonNullList.create();
            try {
                block.getSubBlocks(itemTab, subBlocks);
            } catch (RuntimeException | LinkageError e) {
                String itemStackInfo = ErrorHelper.getItemStackInfo(new ItemStack(item));
                Log.get().error("Failed to getSubBlocks {}", itemStackInfo, e);
            }

            for (ItemStack subBlock : subBlocks) {
                if (subBlock == null) {
                    Log.get().error("Found null subBlock of {}", block);
                } else if (subBlock.isEmpty()) {
                    Log.get().error("Found empty subBlock of {}", block);
                } else {
                    addItemStack(stackHelper, subBlock, itemList, itemNameSet);
                }
            }
        }
    }

    private void addItemStack(StackHelper stackHelper, ItemStack stack, List<ItemStack> itemList, Set<String> itemNameSet) {
        Item item = stack.getItem();
        if (subtypeCount.count(item) >= Config.getMaxSubtypes()) {
            return;
        }

        String itemKey = null;

        try {
            addFallbackSubtypeInterpreter(stack);
            itemKey = stackHelper.getUniqueIdentifierForStack(stack, StackHelper.UidMode.FULL);
        } catch (RuntimeException | LinkageError e) {
            String stackInfo = ErrorHelper.getItemStackInfo(stack);
            Log.get().error("Couldn't get unique name for itemStack {}", stackInfo, e);
        }

        if (itemKey != null) {
            if (itemNameSet.contains(itemKey)) {
                return;
            }
            itemNameSet.add(itemKey);
            itemList.add(stack);
        }
    }


    public boolean hasStarted() {
        return started;
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

}
