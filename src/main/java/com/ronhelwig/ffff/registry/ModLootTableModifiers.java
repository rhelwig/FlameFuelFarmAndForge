package com.ronhelwig.ffff.registry;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

public final class ModLootTableModifiers {

	private static final ResourceKey<LootTable> COW_LOOT = lootTable(EntityType.COW);

	private static final ResourceKey<LootTable> PIG_LOOT = lootTable(EntityType.PIG);

	private static final ResourceKey<LootTable> SALMON_LOOT = lootTable(EntityType.SALMON);

	private static final ResourceKey<LootTable> SHEEP_LOOT = lootTable(EntityType.SHEEP);

	private ModLootTableModifiers() {}

	public static void init() {

		LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {

			if (!source.isBuiltin()) {

				return;

			}

			if (key.equals(COW_LOOT)) {

				tableBuilder.withPool(createAnimalFatPool(UniformGenerator.between(1.0F, 4.0F)));

			} else if (key.equals(PIG_LOOT)) {

				tableBuilder.withPool(createAnimalFatPool(UniformGenerator.between(2.0F, 5.0F)));

			} else if (key.equals(SALMON_LOOT)) {

				tableBuilder.withPool(createAnimalFatPool(UniformGenerator.between(0.0F, 2.0F)));

			} else if (key.equals(SHEEP_LOOT)) {

				tableBuilder.withPool(createAnimalFatPool(UniformGenerator.between(0.0F, 3.0F)));

			}

		});

	}

	private static LootPool.Builder createAnimalFatPool(UniformGenerator countRange) {

		return LootPool.lootPool()

			.setRolls(ConstantValue.exactly(1.0F))

			.add(LootItem.lootTableItem(ModItems.ANIMAL_FAT)

				.apply(SetItemCountFunction.setCount(countRange)));

	}

	private static ResourceKey<LootTable> lootTable(EntityType<?> entityType) {
		return entityType.getDefaultLootTable().orElseThrow();
	}

}
