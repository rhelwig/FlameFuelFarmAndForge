package com.ronhelwig.ffff.item;

import static com.ronhelwig.ffff.FlameFuelFarmAndForge.MOD_ID;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;

public final class ModToolMaterials {
	public static final TagKey<Item> BRONZE_TOOL_MATERIALS = TagKey.create(
		Registries.ITEM,
		ResourceLocation.fromNamespaceAndPath(MOD_ID, "bronze_tool_materials")
	);

	public static final ToolMaterial BRONZE = new ToolMaterial(
		BlockTags.NEEDS_IRON_TOOL,
		250,
		6.0F,
		2.0F,
		14,
		BRONZE_TOOL_MATERIALS
	);

	private ModToolMaterials() {}
}
