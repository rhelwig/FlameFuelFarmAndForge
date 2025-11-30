package com.ronhelwig.ffff.registry;

import static com.ronhelwig.ffff.FlameFuelFarmAndForge.MOD_ID;

import com.ronhelwig.ffff.blockentity.LanternBlockEntity;
import com.ronhelwig.ffff.blockentity.TimedTorchBlockEntity;
import com.ronhelwig.ffff.util.VanillaReferences;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ModBlockEntities {
	public static final BlockEntityType<TimedTorchBlockEntity> TORCH_TIMER = Registry.register(
		BuiltInRegistries.BLOCK_ENTITY_TYPE,
		ResourceLocation.fromNamespaceAndPath(MOD_ID, "torch_timer"),
		FabricBlockEntityTypeBuilder.create(
			TimedTorchBlockEntity::new,
			Blocks.TORCH,
			Blocks.WALL_TORCH,
			VanillaReferences.COPPER_TORCH_BLOCK,
			VanillaReferences.COPPER_WALL_TORCH_BLOCK
		).build()
	);

	public static final BlockEntityType<LanternBlockEntity> LANTERN = Registry.register(
		BuiltInRegistries.BLOCK_ENTITY_TYPE,
		ResourceLocation.fromNamespaceAndPath(MOD_ID, "lantern"),
		FabricBlockEntityTypeBuilder.create(
			LanternBlockEntity::new,
			resolveLanternBlocks()
		).build()
	);

	private ModBlockEntities() {}

	public static void init() {
		// Trigger class loading.
	}

	private static Block[] resolveLanternBlocks() {
		// Ensure mod blocks are registered before scanning.
		com.ronhelwig.ffff.registry.ModBlocks.BRONZE_LANTERN.toString();
		return BuiltInRegistries.BLOCK
			.stream()
			.filter(block -> block instanceof LanternBlock)
			.toArray(Block[]::new);
	}
}
