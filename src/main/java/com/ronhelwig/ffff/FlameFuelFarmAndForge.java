package com.ronhelwig.ffff;

import com.ronhelwig.ffff.combat.TorchCombatHandler;
import com.ronhelwig.ffff.registry.ModBlocks;
import com.ronhelwig.ffff.registry.ModBlockEntities;
import com.ronhelwig.ffff.registry.ModItems;
import com.ronhelwig.ffff.registry.ModLootTableModifiers;
import com.ronhelwig.ffff.registry.ModMenuTypes;
import com.ronhelwig.ffff.registry.ModWorldGeneration;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.world.level.block.Blocks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlameFuelFarmAndForge implements ModInitializer {
	public static final String MOD_ID = "flame-fuel-farm-and-forge";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModMenuTypes.init();
		ModBlockEntities.init();
		ModItems.init();
		ModLootTableModifiers.init();
		ModItems.registerItemGroupEntries();
		ModBlocks.registerItemGroupEntries();
		ModWorldGeneration.init();
		TorchCombatHandler.init();
		registerFlammableBlocks();

		LOGGER.info("Timed torch logic initialized.");
	}

	private void registerFlammableBlocks() {
		FlammableBlockRegistry registry = FlammableBlockRegistry.getDefaultInstance();
		registry.add(Blocks.COAL_ORE, 15, 30);
		registry.add(Blocks.DEEPSLATE_COAL_ORE, 15, 30);
	}

}
