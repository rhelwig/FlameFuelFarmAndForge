package com.ronhelwig.ffff.registry;

import static com.ronhelwig.ffff.FlameFuelFarmAndForge.MOD_ID;

import com.ronhelwig.ffff.block.BronzeHopperBlock;
import com.ronhelwig.ffff.block.BurntTorchBlock;
import com.ronhelwig.ffff.block.BurntWallTorchBlock;
import com.ronhelwig.ffff.mixin.BlockEntityTypeAccessor;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class ModBlocks {
	private static final ResourceLocation BURNT_TORCH_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "burnt_torch");
	private static final ResourceLocation BURNT_WALL_TORCH_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "burnt_wall_torch");
	private static final ResourceLocation TIN_ORE_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "tin_ore");
	private static final ResourceLocation ZINC_ORE_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "zinc_ore");
	private static final ResourceLocation TIN_BLOCK_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "tin_block");
	private static final ResourceLocation ZINC_BLOCK_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "zinc_block");
	private static final ResourceLocation BRONZE_BLOCK_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "bronze_block");
	private static final ResourceLocation BRASS_BLOCK_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "brass_block");
	private static final ResourceLocation BRONZE_HOPPER_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "bronze_hopper");
	private static final ResourceLocation BRONZE_CHAIN_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "bronze_chain");
	private static final ResourceLocation BRONZE_LANTERN_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "bronze_lantern");
	private static final ResourceLocation COPPER_KETTLE_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "copper_kettle");

	public static final Block BURNT_TORCH = registerBlock(
		BURNT_TORCH_ID,
		() -> new BurntTorchBlock(baseTorchSettings(BURNT_TORCH_ID, Blocks.TORCH).lightLevel(state -> 0), ParticleTypes.SMOKE)
	);

	public static final Block BURNT_WALL_TORCH = registerBlock(
		BURNT_WALL_TORCH_ID,
		() -> new BurntWallTorchBlock(baseTorchSettings(BURNT_WALL_TORCH_ID, Blocks.WALL_TORCH).lightLevel(state -> 0), ParticleTypes.SMOKE)
	);

	public static final Block TIN_ORE = registerBlockWithItem(
		TIN_ORE_ID,
		() -> new DropExperienceBlock(UniformInt.of(0, 2), baseBlockSettings(TIN_ORE_ID, Blocks.COPPER_ORE))
	);

	public static final Block ZINC_ORE = registerBlockWithItem(
		ZINC_ORE_ID,
		() -> new DropExperienceBlock(UniformInt.of(0, 2), baseBlockSettings(ZINC_ORE_ID, Blocks.COPPER_ORE))
	);

	public static final Block TIN_BLOCK = registerBlockWithItem(
		TIN_BLOCK_ID,
		() -> new Block(baseBlockSettings(TIN_BLOCK_ID, Blocks.COPPER_BLOCK))
	);

	public static final Block ZINC_BLOCK = registerBlockWithItem(
		ZINC_BLOCK_ID,
		() -> new Block(baseBlockSettings(ZINC_BLOCK_ID, Blocks.COPPER_BLOCK))
	);

	public static final Block BRONZE_BLOCK = registerBlockWithItem(
		BRONZE_BLOCK_ID,
		() -> new Block(baseBlockSettings(BRONZE_BLOCK_ID, Blocks.COPPER_BLOCK))
	);
	public static final Block BRASS_BLOCK = registerBlockWithItem(
		BRASS_BLOCK_ID,
		() -> new Block(baseBlockSettings(BRASS_BLOCK_ID, Blocks.COPPER_BLOCK))
	);
	public static final Block BRONZE_HOPPER = registerBlockWithItem(
		BRONZE_HOPPER_ID,
		() -> new BronzeHopperBlock(baseBlockSettings(BRONZE_HOPPER_ID, Blocks.HOPPER))
	);
	public static final Block BRONZE_CHAIN = registerBlockWithItem(
		BRONZE_CHAIN_ID,
		() -> new ChainBlock(baseBlockSettings(BRONZE_CHAIN_ID, Blocks.IRON_CHAIN))
	);
	public static final Block BRONZE_LANTERN = registerBlockWithItem(
		BRONZE_LANTERN_ID,
		() -> new LanternBlock(baseBlockSettings(BRONZE_LANTERN_ID, Blocks.LANTERN))
	);
	public static final Block COPPER_KETTLE = registerBlockWithItem(
		COPPER_KETTLE_ID,
		() -> new com.ronhelwig.ffff.block.CopperKettleBlock(baseBlockSettings(COPPER_KETTLE_ID, Blocks.CAULDRON))
	);

	static {
		Registry.register(BuiltInRegistries.ITEM, BURNT_TORCH_ID, createTorchItem());
		((BlockEntityTypeAccessor) BlockEntityType.HOPPER).ffff$getValidBlocks().add(BRONZE_HOPPER);
	}

	private ModBlocks() {}

	private static Block registerBlock(ResourceLocation id, Supplier<Block> blockFactory) {
		return Registry.register(BuiltInRegistries.BLOCK, id, blockFactory.get());
	}

	private static Block registerBlockWithItem(ResourceLocation id, Supplier<Block> blockFactory) {
		Block block = registerBlock(id, blockFactory);
		ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id);
		Item.Properties itemProperties = new Item.Properties().setId(itemKey);
		Registry.register(BuiltInRegistries.ITEM, id, new BlockItem(block, itemProperties));
		return block;
	}

	private static Item createTorchItem() {
		ResourceKey<Item> torchItemKey = ResourceKey.create(Registries.ITEM, BURNT_TORCH_ID);
		Item.Properties itemProperties = new Item.Properties().setId(torchItemKey);
		return new StandingAndWallBlockItem(BURNT_TORCH, BURNT_WALL_TORCH, Direction.UP, itemProperties);
	}

	private static BlockBehaviour.Properties baseTorchSettings(ResourceLocation id, Block copiedBlock) {
		return baseBlockSettings(id, copiedBlock);
	}

	private static BlockBehaviour.Properties baseBlockSettings(ResourceLocation id, Block copiedBlock) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, id);
		return BlockBehaviour.Properties.ofFullCopy(copiedBlock).setId(key);
	}

	public static void registerItemGroupEntries() {
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(entries -> {
			entries.addAfter(Items.TORCH, BURNT_TORCH.asItem());
			entries.addAfter(Items.HOPPER, BRONZE_HOPPER.asItem());
			entries.addAfter(Items.LANTERN, BRONZE_LANTERN.asItem());
			entries.addAfter(Items.CAULDRON, COPPER_KETTLE.asItem());
		});

		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.NATURAL_BLOCKS).register(entries -> {
			entries.addAfter(Items.COPPER_ORE, TIN_ORE.asItem());
			entries.addAfter(TIN_ORE.asItem(), ZINC_ORE.asItem());
		});

		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.BUILDING_BLOCKS).register(entries -> {
			entries.addAfter(Items.COPPER_BLOCK, TIN_BLOCK.asItem());
			entries.addAfter(TIN_BLOCK.asItem(), ZINC_BLOCK.asItem());
			entries.addAfter(ZINC_BLOCK.asItem(), BRONZE_BLOCK.asItem());
			entries.addAfter(BRONZE_BLOCK.asItem(), BRASS_BLOCK.asItem());
			entries.addAfter(BRASS_BLOCK.asItem(), BRONZE_CHAIN.asItem());
		});

		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.REDSTONE_BLOCKS).register(entries ->
			entries.addAfter(Items.HOPPER, BRONZE_HOPPER.asItem())
		);
	}
}
