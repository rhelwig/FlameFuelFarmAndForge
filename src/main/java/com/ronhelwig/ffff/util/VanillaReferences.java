package com.ronhelwig.ffff.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public final class VanillaReferences {
	public static final Block COPPER_TORCH_BLOCK = lookupBlock("copper_torch");
	public static final Block COPPER_WALL_TORCH_BLOCK = lookupBlock("copper_wall_torch");
	public static final Item COPPER_TORCH_ITEM = lookupItem("copper_torch");

	private VanillaReferences() {}

	private static Block lookupBlock(String id) {
		return BuiltInRegistries.BLOCK.getOptional(ResourceLocation.fromNamespaceAndPath("minecraft", id)).orElse(null);
	}

	private static Item lookupItem(String id) {
		return BuiltInRegistries.ITEM.getOptional(ResourceLocation.fromNamespaceAndPath("minecraft", id)).orElse(null);
	}
}
