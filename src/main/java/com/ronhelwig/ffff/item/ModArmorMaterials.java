package com.ronhelwig.ffff.item;

import static com.ronhelwig.ffff.FlameFuelFarmAndForge.MOD_ID;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

public final class ModArmorMaterials {

	public static final ResourceKey<EquipmentAsset> BRONZE_EQUIPMENT_ASSET = ResourceKey.create(
		EquipmentAssets.ROOT_ID,
		ResourceLocation.fromNamespaceAndPath(MOD_ID, "bronze")
	);

	private static Map<ArmorType, Integer> defenseValues(int boots, int leggings, int chestplate, int helmet, int body) {
		EnumMap<ArmorType, Integer> values = new EnumMap<>(ArmorType.class);
		values.put(ArmorType.BOOTS, boots);
		values.put(ArmorType.LEGGINGS, leggings);
		values.put(ArmorType.CHESTPLATE, chestplate);
		values.put(ArmorType.HELMET, helmet);
		values.put(ArmorType.BODY, body);
		return Map.copyOf(values);
	}

	public static final ArmorMaterial BRONZE = new ArmorMaterial(
		10,
		defenseValues(2, 3, 4, 2, 4),
		12,
		SoundEvents.ARMOR_EQUIP_IRON,
		0.0f,
		0.0f,
		ModToolMaterials.BRONZE_TOOL_MATERIALS,
		BRONZE_EQUIPMENT_ASSET
	);

	private ModArmorMaterials() {}
}
