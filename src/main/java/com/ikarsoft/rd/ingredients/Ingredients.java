package com.ikarsoft.rd.ingredients;

import com.ikarsoft.rd.Internal;

import java.util.*;

public class Ingredients implements IIngredients {
	private final Map<Class, List<List>> inputs = new IdentityHashMap<>();
	private final Map<Class, List<List>> outputs = new IdentityHashMap<>();

	@Override
	public <T> void setInput(Class<? extends T> ingredientClass, T input) {
		setInputs(ingredientClass, Collections.singletonList(input));
	}

	@Override
	public <T> void setInputLists(Class<? extends T> ingredientClass, List<List<T>> inputs) {
		IIngredientRegistry ingredientRegistry = Internal.getIngredientRegistry();
		IIngredientHelper<T> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredientClass);
		List<List> expandedInputs = new ArrayList<>();
		for (List<T> input : inputs) {
			List<T> itemStacks = ingredientHelper.expandSubtypes(input);
			expandedInputs.add(itemStacks);
		}

		this.inputs.put(ingredientClass, expandedInputs);
	}

	@Override
	public <T> void setInputs(Class<? extends T> ingredientClass, List<T> input) {
		IIngredientRegistry ingredientRegistry = Internal.getIngredientRegistry();
		IIngredientHelper<T> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredientClass);
		List<List> expandedInputs = new ArrayList<>();
		for (T input1 : input) {
			List<T> itemStacks = ingredientHelper.expandSubtypes(Collections.singletonList(input1));
			expandedInputs.add(itemStacks);
		}

		this.inputs.put(ingredientClass, expandedInputs);
	}

	@Override
	public <T> void setOutput(Class<? extends T> ingredientClass, T output) {
		setOutputs(ingredientClass, Collections.singletonList(output));
	}

	@Override
	public <T> void setOutputs(Class<? extends T> ingredientClass, List<T> outputs) {
		IIngredientRegistry ingredientRegistry = Internal.getIngredientRegistry();
		IIngredientHelper<T> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredientClass);
		List<List> expandedOutputs = new ArrayList<>();
		for (T output : outputs) {
			List<T> expandedOutput = ingredientHelper.expandSubtypes(Collections.singletonList(output));
			expandedOutputs.add(expandedOutput);
		}

		this.outputs.put(ingredientClass, expandedOutputs);
	}

	@Override
	public <T> void setOutputLists(Class<? extends T> ingredientClass, List<List<T>> outputs) {
		IIngredientRegistry ingredientRegistry = Internal.getIngredientRegistry();
		IIngredientHelper<T> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredientClass);
		List<List> expandedOutputs = new ArrayList<>();
		for (List<T> output : outputs) {
			List<T> itemStacks = ingredientHelper.expandSubtypes(output);
			expandedOutputs.add(itemStacks);
		}

		this.outputs.put(ingredientClass, expandedOutputs);
	}

	@Override
	public <T> List<List<T>> getInputs(Class<? extends T> ingredientClass) {
		//noinspection unchecked
		List<List<T>> inputs = (List<List<T>>) (Object) this.inputs.get(ingredientClass);
		if (inputs == null) {
			return Collections.emptyList();
		}
		return inputs;
	}

	@Override
	public <T> List<List<T>> getOutputs(Class<? extends T> ingredientClass) {
		//noinspection unchecked
		List<List<T>> outputs = (List<List<T>>) (Object) this.outputs.get(ingredientClass);
		if (outputs == null) {
			return Collections.emptyList();
		}
		return outputs;
	}

	public Map<Class, List> getInputIngredients() {
		Map<Class, List> inputIngredients = new IdentityHashMap<>();
		for (Map.Entry<Class, List<List>> entry : inputs.entrySet()) {
			List<Object> flatIngredients = new ArrayList<>();
			for (List ingredients : entry.getValue()) {
				flatIngredients.addAll(ingredients);
			}
			inputIngredients.put(entry.getKey(), flatIngredients);
		}
		return inputIngredients;
	}

	public Map<Class, List> getOutputIngredients() {
		Map<Class, List> outputIngredients = new IdentityHashMap<>();
		for (Map.Entry<Class, List<List>> entry : outputs.entrySet()) {
			List<Object> flatIngredients = new ArrayList<>();
			for (List ingredients : entry.getValue()) {
				flatIngredients.addAll(ingredients);
			}
			outputIngredients.put(entry.getKey(), flatIngredients);
		}
		return outputIngredients;
	}
}
