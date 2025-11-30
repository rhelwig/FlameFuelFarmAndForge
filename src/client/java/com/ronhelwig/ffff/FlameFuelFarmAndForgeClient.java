package com.ronhelwig.ffff;

import com.ronhelwig.ffff.client.screen.BronzeHopperScreen;
import com.ronhelwig.ffff.client.screen.LanternScreen;
import com.ronhelwig.ffff.registry.ModItems;
import com.ronhelwig.ffff.registry.ModMenuTypes;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.player.PlayerPickItemEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public final class FlameFuelFarmAndForgeClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		MenuScreens.register(ModMenuTypes.BRONZE_HOPPER, BronzeHopperScreen::new);
		MenuScreens.register(ModMenuTypes.LANTERN, LanternScreen::new);
		registerItemAssetDebugLogging();

		PlayerPickItemEvents.BLOCK.register((player, pos, state, includeData) -> {
			if (state == null) {
				return null;
			}

			if (!state.is(Blocks.TORCH) && !state.is(Blocks.WALL_TORCH)) {
				return null;
			}

			if (player.getAbilities().instabuild) {
				return new ItemStack(Items.TORCH);
			}

			return new ItemStack(ModItems.KINDLING);
		});
	}

	private static void registerItemAssetDebugLogging() {
		ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			private final ResourceLocation id = ResourceLocation.fromNamespaceAndPath(FlameFuelFarmAndForge.MOD_ID, "item_asset_debug");

			@Override
			public ResourceLocation getFabricId() {
				return id;
			}

			@Override
			public void onResourceManagerReload(ResourceManager resourceManager) {
				logItemAssets(resourceManager, "oil_bottle");
				logItemAssets(resourceManager, "copper_kettle");
			}
		});
	}

	private static void logItemAssets(ResourceManager resourceManager, String itemId) {
		logAsset(resourceManager, itemId, "models/item/" + itemId + ".json", "item model");
		logAsset(resourceManager, itemId, "items/" + itemId + ".json", "item definition");
		logAsset(resourceManager, itemId, "textures/item/" + itemId + ".png", "item texture");
	}

	private static void logAsset(ResourceManager resourceManager, String itemId, String path, String label) {
		ResourceLocation location = ResourceLocation.fromNamespaceAndPath(FlameFuelFarmAndForge.MOD_ID, path);
		boolean found = resourceManager.getResource(location).isPresent();
		FlameFuelFarmAndForge.LOGGER.info("Asset check for {} ({}) at {}: {}", itemId, label, location, found ? "FOUND" : "MISSING");
	}
}
