package com.ronhelwig.ffff.registry;

import static com.ronhelwig.ffff.FlameFuelFarmAndForge.MOD_ID;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public final class ModWorldGeneration {
	private ModWorldGeneration() {}

	public static void init() {
		ResourceKey<PlacedFeature> tinSmall = ResourceKey.create(
			Registries.PLACED_FEATURE,
			ResourceLocation.fromNamespaceAndPath(MOD_ID, "ore_tin_small")
		);

		ResourceKey<PlacedFeature> tinLarge = ResourceKey.create(
			Registries.PLACED_FEATURE,
			ResourceLocation.fromNamespaceAndPath(MOD_ID, "ore_tin_large")
		);

		ResourceKey<PlacedFeature> zincSmall = ResourceKey.create(
			Registries.PLACED_FEATURE,
			ResourceLocation.fromNamespaceAndPath(MOD_ID, "ore_zinc_small")
		);

		ResourceKey<PlacedFeature> zincLarge = ResourceKey.create(
			Registries.PLACED_FEATURE,
			ResourceLocation.fromNamespaceAndPath(MOD_ID, "ore_zinc_large")
		);

		BiomeModifications.addFeature(
			BiomeSelectors.foundInOverworld(),
			GenerationStep.Decoration.UNDERGROUND_ORES,
			tinSmall
		);

		BiomeModifications.addFeature(
			BiomeSelectors.foundInOverworld(),
			GenerationStep.Decoration.UNDERGROUND_ORES,
			tinLarge
		);

		BiomeModifications.addFeature(
			BiomeSelectors.foundInOverworld(),
			GenerationStep.Decoration.UNDERGROUND_ORES,
			zincSmall
		);

		BiomeModifications.addFeature(
			BiomeSelectors.foundInOverworld(),
			GenerationStep.Decoration.UNDERGROUND_ORES,
			zincLarge
		);
	}
}
