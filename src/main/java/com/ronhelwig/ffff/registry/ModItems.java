package com.ronhelwig.ffff.registry;

import static com.ronhelwig.ffff.FlameFuelFarmAndForge.MOD_ID;

import com.ronhelwig.ffff.item.ModArmorMaterials;
import com.ronhelwig.ffff.item.ModToolMaterials;
import java.util.function.Function;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.registry.FuelRegistryEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.equipment.ArmorType;

public final class ModItems {

	public static Item KINDLING;

	public static Item ANIMAL_FAT;
	public static Item RAW_TIN;
	public static Item TIN_INGOT;
	public static Item TIN_NUGGET;
	public static Item RAW_ZINC;
	public static Item ZINC_INGOT;
	public static Item ZINC_NUGGET;
	public static Item BRONZE_INGOT;
	public static Item BRONZE_NUGGET;
	public static Item BRASS_INGOT;
	public static Item BRASS_NUGGET;
	public static Item OIL_BOTTLE;
	public static Item OIL_BUCKET;
	public static Item BRONZE_PICKAXE;
	public static Item BRONZE_AXE;
	public static Item BRONZE_HOE;
	public static Item BRONZE_SHOVEL;
	public static Item BRONZE_SWORD;
	public static Item BRONZE_HELMET;
	public static Item BRONZE_CHESTPLATE;
	public static Item BRONZE_LEGGINGS;
	public static Item BRONZE_BOOTS;

	private ModItems() {}

	private static Item registerSimpleItem(String name) {
		return registerItem(name, Item::new);
	}

	private static Item registerItem(String name, Function<Item.Properties, Item> factory) {
		ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
		Item.Properties properties = new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id));
		return Registry.register(BuiltInRegistries.ITEM, id, factory.apply(properties));
	}

	public static void init() {

		KINDLING = registerSimpleItem("kindling");

		ANIMAL_FAT = registerSimpleItem("animal_fat");
		RAW_TIN = registerSimpleItem("raw_tin");
		TIN_INGOT = registerSimpleItem("tin_ingot");
		TIN_NUGGET = registerSimpleItem("tin_nugget");
		RAW_ZINC = registerSimpleItem("raw_zinc");
		ZINC_INGOT = registerSimpleItem("zinc_ingot");
		ZINC_NUGGET = registerSimpleItem("zinc_nugget");
		BRONZE_INGOT = registerSimpleItem("bronze_ingot");
		BRONZE_NUGGET = registerSimpleItem("bronze_nugget");
		BRASS_INGOT = registerSimpleItem("brass_ingot");
		BRASS_NUGGET = registerSimpleItem("brass_nugget");
		OIL_BOTTLE = registerItem("oil_bottle", properties ->
			new Item(properties.craftRemainder(Items.GLASS_BOTTLE).stacksTo(16))
		);
		OIL_BUCKET = registerItem("oil_bucket", properties ->
			new Item(properties.craftRemainder(Items.BUCKET).stacksTo(1))
		);
		BRONZE_PICKAXE = registerItem("bronze_pickaxe", properties -> {
			Item.Properties toolProperties = ModToolMaterials.BRONZE.applyToolProperties(
				properties,
				BlockTags.MINEABLE_WITH_PICKAXE,
				1.0f,
				-2.8f,
				0.0f
			);
			return new Item(toolProperties);
		});
		BRONZE_AXE = registerItem("bronze_axe", properties ->
			new AxeItem(ModToolMaterials.BRONZE, 6, -3.0f, properties)
		);
		BRONZE_HOE = registerItem("bronze_hoe", properties ->
			new HoeItem(ModToolMaterials.BRONZE, 0, -1.0f, properties)
		);
		BRONZE_SHOVEL = registerItem("bronze_shovel", properties ->
			new ShovelItem(ModToolMaterials.BRONZE, 1, -3.0f, properties)
		);
		BRONZE_SWORD = registerItem("bronze_sword", properties -> {
			Item.Properties swordProperties = properties.sword(ModToolMaterials.BRONZE, 3.0f, -2.4f);
			return new Item(swordProperties);
		});
		BRONZE_HELMET = registerItem("bronze_helmet", properties ->
			new Item(properties.humanoidArmor(ModArmorMaterials.BRONZE, ArmorType.HELMET))
		);
		BRONZE_CHESTPLATE = registerItem("bronze_chestplate", properties ->
			new Item(properties.humanoidArmor(ModArmorMaterials.BRONZE, ArmorType.CHESTPLATE))
		);
		BRONZE_LEGGINGS = registerItem("bronze_leggings", properties ->
			new Item(properties.humanoidArmor(ModArmorMaterials.BRONZE, ArmorType.LEGGINGS))
		);
		BRONZE_BOOTS = registerItem("bronze_boots", properties ->
			new Item(properties.humanoidArmor(ModArmorMaterials.BRONZE, ArmorType.BOOTS))
		);

		registerFuelEntries();

	}

	public static void registerItemGroupEntries() {

		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(entries -> {

			entries.addAfter(Items.STICK, KINDLING);

			entries.addAfter(KINDLING, ANIMAL_FAT);
			entries.addAfter(ANIMAL_FAT, OIL_BOTTLE);
			entries.addAfter(OIL_BOTTLE, OIL_BUCKET);

		});

		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS).register(entries -> {

			entries.addAfter(Items.RAW_COPPER, RAW_TIN);
			entries.addAfter(RAW_TIN, TIN_INGOT);
			entries.addAfter(TIN_INGOT, TIN_NUGGET);
			entries.addAfter(TIN_NUGGET, RAW_ZINC);
			entries.addAfter(RAW_ZINC, ZINC_INGOT);
			entries.addAfter(ZINC_INGOT, ZINC_NUGGET);
			entries.addAfter(ZINC_NUGGET, BRONZE_INGOT);
			entries.addAfter(BRONZE_INGOT, BRONZE_NUGGET);
			entries.addAfter(BRONZE_NUGGET, BRASS_INGOT);
			entries.addAfter(BRASS_INGOT, BRASS_NUGGET);

		});

		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries -> {
			entries.addAfter(Items.IRON_PICKAXE, BRONZE_PICKAXE);
			entries.addAfter(Items.IRON_AXE, BRONZE_AXE);
			entries.addAfter(Items.IRON_HOE, BRONZE_HOE);
			entries.addAfter(Items.IRON_SHOVEL, BRONZE_SHOVEL);
		});

		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT).register(entries -> {
			entries.addAfter(Items.IRON_SWORD, BRONZE_SWORD);
			entries.addAfter(Items.IRON_HELMET, BRONZE_HELMET);
			entries.addAfter(Items.IRON_CHESTPLATE, BRONZE_CHESTPLATE);
			entries.addAfter(Items.IRON_LEGGINGS, BRONZE_LEGGINGS);
			entries.addAfter(Items.IRON_BOOTS, BRONZE_BOOTS);
		});

	}

	private static void registerFuelEntries() {
		FuelRegistryEvents.BUILD.register((builder, context) -> {
			builder.add(KINDLING, 150);
			builder.add(ANIMAL_FAT, 1500);
			builder.add(OIL_BOTTLE, 2400);
			builder.add(OIL_BUCKET, 2400 * 3); // bucket holds 3 bottles worth
		});
	}

}
