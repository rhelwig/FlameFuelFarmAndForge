package com.ronhelwig.ffff.registry;

import static com.ronhelwig.ffff.FlameFuelFarmAndForge.MOD_ID;

import com.ronhelwig.ffff.menu.BronzeHopperMenu;
import com.ronhelwig.ffff.menu.LanternMenu;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public final class ModMenuTypes {

	public static final MenuType<BronzeHopperMenu> BRONZE_HOPPER = Registry.register(
		BuiltInRegistries.MENU,
		ResourceLocation.fromNamespaceAndPath(MOD_ID, "bronze_hopper"),
		new MenuType<>(BronzeHopperMenu::new, FeatureFlags.VANILLA_SET)
	);

	public static final MenuType<LanternMenu> LANTERN = Registry.register(
		BuiltInRegistries.MENU,
		ResourceLocation.fromNamespaceAndPath(MOD_ID, "lantern"),
		new MenuType<>(LanternMenu::new, FeatureFlags.VANILLA_SET)
	);

	private ModMenuTypes() {}

	public static void init() {
		// Trigger class loading.
	}
}
