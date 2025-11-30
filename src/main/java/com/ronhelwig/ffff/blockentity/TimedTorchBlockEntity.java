package com.ronhelwig.ffff.blockentity;

import static com.ronhelwig.ffff.FlameFuelFarmAndForge.MOD_ID;

import com.ronhelwig.ffff.registry.ModBlockEntities;
import com.ronhelwig.ffff.registry.ModBlocks;
import com.ronhelwig.ffff.registry.ModItems;
import com.ronhelwig.ffff.blockentity.LanternBlockEntity;
import com.ronhelwig.ffff.util.VanillaReferences;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class TimedTorchBlockEntity extends BlockEntity {
	private static final long DEFAULT_LIFESPAN_TICKS = 24000L;
	private static final long COPPER_LIFESPAN_TICKS = DEFAULT_LIFESPAN_TICKS * 3;
	/* Set next line to 1.0 to make ALL regular torches into lanterns (when possible) */
	private static final float LANTERN_REPLACEMENT_CHANCE = 0.95f;
	private static final long IGNITION_CHECK_INTERVAL_TICKS = 20L;
	private static final long FAST_IGNITION_TICKS = 2400L;
	private static final long COAL_IGNITION_TICKS = 12000L;
	private static final long DEFAULT_FLAMMABLE_IGNITION_TICKS = 7200L;
	private static final Direction[] LANTERN_SEARCH_DIRECTIONS = {
		Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.DOWN, Direction.UP
	};

	private long placedGameTime = -1L;
	private long lifespanTicks = DEFAULT_LIFESPAN_TICKS;
	private boolean playerPlaced;
	private long lastIgnitionAttemptGameTime = -1L;

	public TimedTorchBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.TORCH_TIMER, pos, state);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, TimedTorchBlockEntity blockEntity) {
		if (level.isClientSide() || !(level instanceof ServerLevel serverWorld)) {
			return;
		}

		if (!blockEntity.tracksState(state)) {
			blockEntity.setRemoved();
			return;
		}

		if (blockEntity.placedGameTime < 0L && !blockEntity.playerPlaced) {
			state = blockEntity.upgradeWorldgenTorch(serverWorld, pos, state);
			if (state == null || !blockEntity.tracksState(state)) {
				blockEntity.setRemoved();
				return;
			}
		}

		long expectedLifespan = blockEntity.determineLifespanTicks(state);
		if (blockEntity.lifespanTicks != expectedLifespan) {
			blockEntity.lifespanTicks = expectedLifespan;
			blockEntity.setChanged();
		}

		if (blockEntity.placedGameTime < 0L) {
			blockEntity.placedGameTime = serverWorld.getGameTime();
			blockEntity.lifespanTicks = blockEntity.determineLifespanTicks(state);
			blockEntity.setChanged();
			return;
		}

		long elapsed = serverWorld.getGameTime() - blockEntity.placedGameTime;
		blockEntity.tryIgniteAttachedBlock(serverWorld, pos, state, elapsed);
		if (elapsed < blockEntity.lifespanTicks) {
			return;
		}

		BlockState burntState = blockEntity.buildBurntState(state);
		int flags = Block.UPDATE_ALL | Block.UPDATE_INVISIBLE;
		if (state.is(Blocks.WALL_TORCH) || state.is(VanillaReferences.COPPER_WALL_TORCH_BLOCK)) {
			flags |= Block.UPDATE_NEIGHBORS;
		}

		serverWorld.setBlock(pos, burntState, flags);
	}

	private boolean tracksState(BlockState state) {
		return state.is(Blocks.TORCH)
			|| state.is(Blocks.WALL_TORCH)
			|| state.is(VanillaReferences.COPPER_TORCH_BLOCK)
			|| state.is(VanillaReferences.COPPER_WALL_TORCH_BLOCK);
	}

	private long determineLifespanTicks(BlockState state) {
		return isCopperTorch(state) ? COPPER_LIFESPAN_TICKS : DEFAULT_LIFESPAN_TICKS;
	}

	private BlockState buildBurntState(BlockState currentState) {
		if (currentState.is(Blocks.WALL_TORCH)) {
			BlockState defaultState = ModBlocks.BURNT_WALL_TORCH.defaultBlockState();
			defaultState = defaultState.setValue(WallTorchBlock.FACING, currentState.getValue(WallTorchBlock.FACING));
			if (defaultState.hasProperty(BlockStateProperties.WATERLOGGED)) {
				defaultState = defaultState.setValue(BlockStateProperties.WATERLOGGED, currentState.getValue(BlockStateProperties.WATERLOGGED));
			}
			return defaultState;
		}

		return ModBlocks.BURNT_TORCH.defaultBlockState();
	}

	private static boolean isCopperTorch(BlockState state) {
		return state.is(VanillaReferences.COPPER_TORCH_BLOCK) || state.is(VanillaReferences.COPPER_WALL_TORCH_BLOCK);
	}

	private BlockState copyCopperTorchState(BlockState currentState) {
		if (currentState.is(Blocks.WALL_TORCH)) {
			BlockState copperWall = VanillaReferences.COPPER_WALL_TORCH_BLOCK.defaultBlockState()
				.setValue(WallTorchBlock.FACING, currentState.getValue(WallTorchBlock.FACING));
			if (copperWall.hasProperty(BlockStateProperties.WATERLOGGED)) {
				copperWall = copperWall.setValue(BlockStateProperties.WATERLOGGED, currentState.getValue(BlockStateProperties.WATERLOGGED));
			}
			return copperWall;
		}

		return VanillaReferences.COPPER_TORCH_BLOCK.defaultBlockState();
	}

	@Nullable
	private BlockState upgradeWorldgenTorch(ServerLevel level, BlockPos pos, BlockState state) {
		if (!tracksState(state) || state.is(VanillaReferences.COPPER_TORCH_BLOCK) || state.is(VanillaReferences.COPPER_WALL_TORCH_BLOCK)) {
			return state;
		}

		if (!state.is(Blocks.TORCH) && !state.is(Blocks.WALL_TORCH)) {
			return state;
		}

		if (state.is(Blocks.TORCH) && shouldPlaceLantern(level)) {
			LanternPlacement placement = tryCreateLantern(level, pos);
			if (placement != null) {
				if (!placement.pos().equals(pos)) {
					level.removeBlock(pos, false);
				}

				level.setBlock(placement.pos(), placement.state(), Block.UPDATE_ALL | Block.UPDATE_INVISIBLE);
				primeLanternFuel(level, placement.pos());
				return level.getBlockState(pos);
			}
		}

		BlockState replacement = copyCopperTorchState(state);
		if (replacement == null) {
			return null;
		}

		int flags = Block.UPDATE_ALL | Block.UPDATE_INVISIBLE;
		if (state.is(Blocks.WALL_TORCH)) {
			flags |= Block.UPDATE_NEIGHBORS;
		}

		level.setBlock(pos, replacement, flags);
		return replacement;
	}

	private boolean shouldPlaceLantern(ServerLevel level) {
		return level.getRandom().nextFloat() < LANTERN_REPLACEMENT_CHANCE;
	}

	@Nullable
	private LanternPlacement tryCreateLantern(ServerLevel level, BlockPos pos) {
		LanternBlock lanternBlock = selectLanternBlock(level);
		if (lanternBlock == null) {
			return null;
		}

		LanternPlacement direct = resolveLanternPlacement(level, pos, lanternBlock);
		if (direct != null) {
			return direct;
		}

		for (Direction direction : LANTERN_SEARCH_DIRECTIONS) {
			BlockPos candidatePos = pos.relative(direction);
			LanternPlacement placement = resolveLanternPlacement(level, candidatePos, lanternBlock);
			if (placement != null) {
				return placement;
			}
		}

		return null;
	}

	@Nullable
	private LanternPlacement resolveLanternPlacement(ServerLevel level, BlockPos pos, LanternBlock lanternBlock) {
		BlockState existingState = level.getBlockState(pos);
		boolean isTorch = existingState.is(Blocks.TORCH) || existingState.is(Blocks.WALL_TORCH);
		if (!existingState.canBeReplaced() && !isTorch) {
			return null;
		}

		BlockState baseState = lanternBlock.defaultBlockState();
		BlockState lanternState = baseState.setValue(LanternBlock.HANGING, Boolean.FALSE);
		if (lanternState.canSurvive(level, pos)) {
			return new LanternPlacement(pos, lanternState);
		}

		BlockState hangingVariant = baseState.setValue(LanternBlock.HANGING, Boolean.TRUE);
		BlockPos above = pos.above();
		if (hangingVariant.canSurvive(level, pos) && level.getBlockState(above).isFaceSturdy(level, above, Direction.DOWN)) {
			return new LanternPlacement(pos, hangingVariant);
		}

		return null;
	}

	@Nullable
	private LanternBlock selectLanternBlock(ServerLevel level) {
		RandomSource random = level.getRandom();
		LanternBlock bronzeLantern = ModBlocks.BRONZE_LANTERN instanceof LanternBlock lantern ? lantern : null;
		LanternBlock copperLantern = BuiltInRegistries.BLOCK.getOptional(ResourceLocation.fromNamespaceAndPath(MOD_ID, "copper_lantern"))
			.filter(LanternBlock.class::isInstance)
			.map(LanternBlock.class::cast)
			.orElse(null);

		if (bronzeLantern == null && copperLantern == null) {
			return (LanternBlock) Blocks.LANTERN;
		}

		LanternBlock[] options;
		if (bronzeLantern != null && copperLantern != null) {
			options = new LanternBlock[] { (LanternBlock) Blocks.LANTERN, bronzeLantern, copperLantern };
		} else if (bronzeLantern != null) {
			options = new LanternBlock[] { (LanternBlock) Blocks.LANTERN, bronzeLantern };
		} else {
			options = new LanternBlock[] { (LanternBlock) Blocks.LANTERN, copperLantern };
		}

		return options[random.nextInt(options.length)];
	}

	private record LanternPlacement(BlockPos pos, BlockState state) {}

	private void primeLanternFuel(ServerLevel level, BlockPos pos) {
		BlockEntity blockEntity = level.getBlockEntity(pos);
		if (blockEntity instanceof LanternBlockEntity lanternEntity) {
			lanternEntity.setItem(LanternBlockEntity.SLOT_FUEL, new ItemStack(ModItems.ANIMAL_FAT, 16));
			lanternEntity.setChanged();
		}
	}

	private void tryIgniteAttachedBlock(ServerLevel level, BlockPos pos, BlockState state, long elapsedTicks) {
		if (elapsedTicks <= 0L || isCopperTorch(state)) {
			return;
		}

		long gameTime = level.getGameTime();
		if (lastIgnitionAttemptGameTime >= 0L && gameTime - lastIgnitionAttemptGameTime < IGNITION_CHECK_INTERVAL_TICKS) {
			return;
		}

		Direction ignitionDirection = resolveIgnitionDirection(state);
		BlockPos supportPos = resolveAttachedBlockPos(pos, state, ignitionDirection);
		if (supportPos == null) {
			return;
		}

		BlockState supportState = level.getBlockState(supportPos);
		if (!isSupportFlammable(supportState)) {
			return;
		}

		lastIgnitionAttemptGameTime = gameTime;

		long ignitionTargetTicks = determineIgnitionTargetTicks(supportState);
		if (ignitionTargetTicks <= 0L) {
			return;
		}

		if (elapsedTicks >= ignitionTargetTicks) {
			igniteTorchPosition(level, pos, state);
			return;
		}

		float progress = Math.min(1.0f, (float) elapsedTicks / (float) ignitionTargetTicks);
		float chance = 0.05f + (0.45f * progress);
		if (level.getRandom().nextFloat() < chance) {
			igniteTorchPosition(level, pos, state);
		}
	}

	private long determineIgnitionTargetTicks(BlockState supportState) {
		if (supportState.is(BlockTags.COAL_ORES)) {
			return COAL_IGNITION_TICKS;
		}

		if (supportState.is(BlockTags.PLANKS)
			|| supportState.is(BlockTags.LOGS_THAT_BURN)
			|| supportState.is(BlockTags.WOODEN_SLABS)
			|| supportState.is(BlockTags.WOODEN_STAIRS)
			|| supportState.is(BlockTags.WOODEN_FENCES)
			|| supportState.is(BlockTags.FENCE_GATES)
			|| supportState.is(BlockTags.WOODEN_DOORS)
			|| supportState.is(BlockTags.WOODEN_TRAPDOORS)
			|| supportState.is(BlockTags.WOODEN_BUTTONS)
			|| supportState.is(BlockTags.WOODEN_PRESSURE_PLATES)) {
			return FAST_IGNITION_TICKS;
		}

		return DEFAULT_FLAMMABLE_IGNITION_TICKS;
	}

	private static void igniteTorchPosition(ServerLevel level, BlockPos pos, BlockState state) {
		BlockState fireState = BaseFireBlock.getState(level, pos);
		if (fireState == null) {
			fireState = Blocks.FIRE.defaultBlockState();
		}

		int flags = Block.UPDATE_ALL | Block.UPDATE_INVISIBLE | Block.UPDATE_NEIGHBORS;
		level.setBlock(pos, fireState, flags);
		level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 0.8f, 0.9f + (level.getRandom().nextFloat() * 0.2f));
	}

	@Nullable
	private BlockPos resolveAttachedBlockPos(BlockPos pos, BlockState state, Direction ignitionDirection) {
		if (state.getBlock() instanceof WallTorchBlock) {
			return pos.relative(ignitionDirection.getOpposite());
		}

		return pos.below();
	}

	private Direction resolveIgnitionDirection(BlockState state) {
		if (state.getBlock() instanceof WallTorchBlock) {
			return state.getValue(WallTorchBlock.FACING);
		}

		return Direction.UP;
	}

	private boolean isSupportFlammable(BlockState state) {
		if (state.isAir()) {
			return false;
		}

		return state.is(BlockTags.COAL_ORES)
			|| state.is(BlockTags.PLANKS)
			|| state.is(BlockTags.LOGS_THAT_BURN)
			|| state.is(BlockTags.WOODEN_SLABS)
			|| state.is(BlockTags.WOODEN_STAIRS)
			|| state.is(BlockTags.WOODEN_FENCES)
			|| state.is(BlockTags.FENCE_GATES)
			|| state.is(BlockTags.WOODEN_DOORS)
			|| state.is(BlockTags.WOODEN_TRAPDOORS)
			|| state.is(BlockTags.WOODEN_BUTTONS)
			|| state.is(BlockTags.WOODEN_PRESSURE_PLATES)
			|| state.is(BlockTags.SAPLINGS)
			|| state.is(BlockTags.LEAVES)
			|| state.is(BlockTags.WOOL)
			|| state.is(BlockTags.WOOL_CARPETS)
			|| state.is(BlockTags.MINEABLE_WITH_AXE);
	}

	public void markPlayerPlaced() {
		if (!playerPlaced) {
			playerPlaced = true;
			setChanged();
		}
	}

	@Override
	protected void loadAdditional(ValueInput tag) {
		super.loadAdditional(tag);
		placedGameTime = tag.getLongOr("PlacedGameTime", 0L);
		lifespanTicks = tag.getLongOr("LifespanTicks", DEFAULT_LIFESPAN_TICKS);
		playerPlaced = tag.getBooleanOr("PlayerPlaced", false);
	}

	@Override
	protected void saveAdditional(ValueOutput tag) {
		super.saveAdditional(tag);
		tag.putLong("PlacedGameTime", placedGameTime);
		tag.putLong("LifespanTicks", lifespanTicks);
		tag.putBoolean("PlayerPlaced", playerPlaced);
	}
}
