package com.ronhelwig.ffff.block;

import com.mojang.serialization.MapCodec;
import com.ronhelwig.ffff.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import java.util.List;

/**
 * Copper Kettle: behaves like a water cauldron but holds 0-2 levels.
 * Supports water buckets/bottles, animal fat, dripstone/rain filling, and comparator output.
 */
public class CopperKettleBlock extends AbstractCauldronBlock {
	public static final int MIN_FILL_LEVEL = 0;
	public static final int MAX_FILL_LEVEL = 2;
	public static final IntegerProperty LEVEL = IntegerProperty.create("level", MIN_FILL_LEVEL, MAX_FILL_LEVEL);
	public static final EnumProperty<ContentType> CONTENT = EnumProperty.create("content", ContentType.class);
	public static final MapCodec<CopperKettleBlock> CODEC = simpleCodec(CopperKettleBlock::new);

	private static final int CAMPFIRE_CONVERT_TICKS = 20 * 60; // one minute at 20 ticks per second

	private static final double HEIGHT_PER_LEVEL = (6.0D - 2.0D) / MAX_FILL_LEVEL;

	public CopperKettleBlock(BlockBehaviour.Properties properties) {
		super(properties, CauldronInteraction.WATER);
		this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, MIN_FILL_LEVEL).setValue(CONTENT, ContentType.EMPTY));
	}

	@Override
	public boolean isFull(BlockState state) {
		return state.getValue(LEVEL) >= MAX_FILL_LEVEL;
	}

	@Override
	public boolean canReceiveStalactiteDrip(Fluid fluid) {
		return fluid == Fluids.WATER;
	}

	@Override
	public boolean isRandomlyTicking(BlockState state) {
		return !isFull(state);
	}

	@Override
	public double getContentHeight(BlockState state) {
		return state.getValue(LEVEL) * HEIGHT_PER_LEVEL;
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
		// Buckets
		if (stack.is(Items.WATER_BUCKET)) {
			return fillFromBucket(stack, state, level, pos, player, hand);
		}
		if (stack.is(ModItems.OIL_BUCKET)) {
			return fillOilFromBucket(stack, state, level, pos, player, hand);
		}
		if (stack.is(Items.BUCKET)) {
			return takeToBucket(stack, state, level, pos, player, hand);
		}

		// Bottles
		if (isWaterPotion(stack)) {
			return fillFromBottle(stack, state, level, pos, player, hand);
		}
		if (stack.is(ModItems.OIL_BOTTLE)) {
			return fillOilFromBottle(stack, state, level, pos, player, hand);
		}
		if (stack.is(Items.GLASS_BOTTLE)) {
			if (content(state) == ContentType.ANIMAL_FAT) {
				return takeAnimalFat(stack, state, level, pos, player, hand);
			}
			if (content(state) == ContentType.OIL) {
				return takeOilToBottle(stack, state, level, pos, player, hand);
			}
			return takeToBottle(stack, state, level, pos, player, hand);
		}

		if (stack.is(ModItems.ANIMAL_FAT)) {
			return addAnimalFat(stack, state, level, pos, player);
		}

		if (content(state) == ContentType.ANIMAL_FAT && stack.isEmpty()) {
			return takeAnimalFat(stack, state, level, pos, player, hand);
		}

		return InteractionResult.PASS;
	}

	private InteractionResult fillFromBucket(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand) {
		if (isFull(state)) {
			return InteractionResult.PASS;
		}

		if (!isEmpty(state) && content(state) != ContentType.WATER) {
			return InteractionResult.PASS;
		}

		if (!level.isClientSide()) {
			setLevel(level, pos, state, MAX_FILL_LEVEL, ContentType.WATER);
			player.awardStat(Stats.FILL_CAULDRON);
			if (!player.getAbilities().instabuild) {
				player.setItemInHand(hand, new ItemStack(Items.BUCKET));
			}
			level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0f, 1.0f);
		}
		return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
	}

	private InteractionResult fillOilFromBucket(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand) {
		if (isFull(state)) {
			return InteractionResult.PASS;
		}

		if (!isEmpty(state) && content(state) != ContentType.OIL) {
			return InteractionResult.PASS;
		}

		if (!level.isClientSide()) {
			setLevel(level, pos, state, MAX_FILL_LEVEL, ContentType.OIL);
			if (!player.getAbilities().instabuild) {
				player.setItemInHand(hand, new ItemStack(Items.BUCKET));
			}
			level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0f, 1.0f);
		}
		return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
	}

	private InteractionResult takeToBucket(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand) {
		if (state.getValue(LEVEL) <= MIN_FILL_LEVEL) {
			return InteractionResult.PASS;
		}

		ContentType currentContent = content(state);
		if (currentContent != ContentType.WATER && currentContent != ContentType.OIL) {
			return InteractionResult.PASS;
		}

		if (!level.isClientSide()) {
			setLevel(level, pos, state, MIN_FILL_LEVEL, ContentType.EMPTY);
			player.awardStat(Stats.USE_CAULDRON);
			if (!player.getAbilities().instabuild) {
				stack.shrink(1);
				ItemStack filled = currentContent == ContentType.OIL ? new ItemStack(ModItems.OIL_BUCKET) : new ItemStack(Items.WATER_BUCKET);
				if (stack.isEmpty()) {
					player.setItemInHand(hand, filled);
				} else if (!player.getInventory().add(filled)) {
					player.drop(filled, false);
				}
			}
			level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0f, 1.0f);
		}
		return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
	}

	private InteractionResult fillFromBottle(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand) {
		int current = state.getValue(LEVEL);
		if (current >= MAX_FILL_LEVEL) {
			return InteractionResult.PASS;
		}

		if (!isEmpty(state) && content(state) != ContentType.WATER) {
			return InteractionResult.PASS;
		}

		if (!level.isClientSide()) {
			setLevel(level, pos, state, current + 1, ContentType.WATER);
			player.awardStat(Stats.FILL_CAULDRON);
			if (!player.getAbilities().instabuild) {
				player.setItemInHand(hand, exchangeItem(stack, player, new ItemStack(Items.GLASS_BOTTLE)));
			}
			level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0f, 1.0f);
		}
		return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
	}

	private InteractionResult fillOilFromBottle(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand) {
		int current = state.getValue(LEVEL);
		if (current >= MAX_FILL_LEVEL) {
			return InteractionResult.PASS;
		}

		if (!isEmpty(state) && content(state) != ContentType.OIL) {
			return InteractionResult.PASS;
		}

		if (!level.isClientSide()) {
			setLevel(level, pos, state, current + 1, ContentType.OIL);
			player.awardStat(Stats.FILL_CAULDRON);
			if (!player.getAbilities().instabuild) {
				player.setItemInHand(hand, exchangeItem(stack, player, new ItemStack(Items.GLASS_BOTTLE)));
			}
			level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0f, 1.0f);
		}
		return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
	}

	private InteractionResult takeToBottle(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand) {
		int current = state.getValue(LEVEL);
		if (current <= MIN_FILL_LEVEL || content(state) != ContentType.WATER) {
			return InteractionResult.PASS;
		}

		if (!level.isClientSide()) {
			int newLevel = current - 1;
			setLevel(level, pos, state, newLevel, newLevel > 0 ? ContentType.WATER : ContentType.EMPTY);
			player.awardStat(Stats.USE_CAULDRON);
			if (!player.getAbilities().instabuild) {
				player.setItemInHand(hand, exchangeItem(stack, player, createWaterBottle()));
			}
			level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0f, 1.0f);
		}

		return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
	}

	private InteractionResult takeOilToBottle(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand) {
		int current = state.getValue(LEVEL);
		if (current <= MIN_FILL_LEVEL || content(state) != ContentType.OIL) {
			return InteractionResult.PASS;
		}

		if (!level.isClientSide()) {
			int newLevel = current - 1;
			setLevel(level, pos, state, newLevel, newLevel > 0 ? ContentType.OIL : ContentType.EMPTY);
			player.awardStat(Stats.USE_CAULDRON);
			if (!player.getAbilities().instabuild) {
				player.setItemInHand(hand, exchangeItem(stack, player, new ItemStack(ModItems.OIL_BOTTLE)));
			}
			level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0f, 1.0f);
		}

		return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
	}

	private InteractionResult addAnimalFat(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player) {
		if (isFull(state) || (!isEmpty(state) && content(state) != ContentType.ANIMAL_FAT)) {
			return InteractionResult.PASS;
		}

		if (!level.isClientSide()) {
			int newLevel = state.getValue(LEVEL) + 1;
			setLevel(level, pos, state, newLevel, ContentType.ANIMAL_FAT);
			if (!player.getAbilities().instabuild) {
				stack.shrink(1);
			}
			level.playSound(null, pos, SoundEvents.HONEY_DRINK.value(), SoundSource.BLOCKS, 1.0f, 1.0f);
			level.scheduleTick(pos, this, CAMPFIRE_CONVERT_TICKS);
		}

		return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
	}

	private InteractionResult takeAnimalFat(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand) {
		if (isEmpty(state) || content(state) != ContentType.ANIMAL_FAT) {
			return InteractionResult.PASS;
		}

		if (!level.isClientSide()) {
			int current = state.getValue(LEVEL);
			int newLevel = Math.max(MIN_FILL_LEVEL, current - 1);
			setLevel(level, pos, state, newLevel, newLevel > 0 ? ContentType.ANIMAL_FAT : ContentType.EMPTY);

			boolean handed = false;
			ItemStack fat = new ItemStack(ModItems.ANIMAL_FAT);
			if (!player.getAbilities().instabuild) {
				if (stack.isEmpty()) {
					player.setItemInHand(hand, fat);
					handed = true;
				} else if (stack.is(ModItems.ANIMAL_FAT) && stack.getCount() < stack.getMaxStackSize()) {
					stack.grow(1);
					handed = true;
				} else if (player.getInventory().add(fat)) {
					handed = true;
				} else {
					player.drop(fat, false);
				}
			}
			level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.4f, 1.0f);
			return handed ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
		}

		return InteractionResult.SUCCESS;
	}

	private ItemStack exchangeItem(ItemStack stack, Player player, ItemStack addition) {
		stack.shrink(1);
		if (stack.isEmpty()) {
			return addition;
		}
		if (!player.getInventory().add(addition)) {
			player.drop(addition, false);
		}
		return stack;
	}

	@Override
	public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		BlockState below = level.getBlockState(pos.below());
		boolean heated = below.getBlock() instanceof net.minecraft.world.level.block.CampfireBlock && net.minecraft.world.level.block.CampfireBlock.isLitCampfire(below);

		if (!isFull(state) && content(state) != ContentType.ANIMAL_FAT && level.getRandom().nextFloat() < 0.05f) {
			setLevel(level, pos, state, state.getValue(LEVEL) + 1, ContentType.WATER);
		}

		if (!level.isClientSide() && heated && content(state) == ContentType.ANIMAL_FAT && state.getValue(LEVEL) > 0) {
			setLevel(level, pos, state, state.getValue(LEVEL), ContentType.OIL);
			level.playSound(null, pos, SoundEvents.BUCKET_FILL_LAVA, SoundSource.BLOCKS, 0.5f, 1.2f);
		}

		if (!level.isClientSide() && content(state) == ContentType.ANIMAL_FAT && state.getValue(LEVEL) > MIN_FILL_LEVEL) {
			level.scheduleTick(pos, this, CAMPFIRE_CONVERT_TICKS);
		}
	}

	@Override
	public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
		super.onPlace(state, level, pos, oldState, isMoving);
		if (content(state) == ContentType.ANIMAL_FAT && level instanceof ServerLevel serverLevel) {
			serverLevel.scheduleTick(pos, this, CAMPFIRE_CONVERT_TICKS);
		}
	}

	@Override
	public void receiveStalactiteDrip(BlockState state, Level level, BlockPos pos, Fluid fluid) {
		if (fluid != Fluids.WATER || isFull(state) || content(state) == ContentType.ANIMAL_FAT) {
			return;
		}
		setLevel(level, pos, state, state.getValue(LEVEL) + 1, ContentType.WATER);
		level.levelEvent(1047, pos, 0);
	}

	@Override
	public void handlePrecipitation(BlockState state, Level level, BlockPos pos, net.minecraft.world.level.biome.Biome.Precipitation precipitation) {
		if (precipitation == net.minecraft.world.level.biome.Biome.Precipitation.RAIN && !isFull(state) && content(state) != ContentType.ANIMAL_FAT) {
			setLevel(level, pos, state, state.getValue(LEVEL) + 1, ContentType.WATER);
		}
	}

	@Override
	public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, net.minecraft.core.Direction direction) {
		int current = state.getValue(LEVEL);
		return Math.round((float) current / (float) MAX_FILL_LEVEL * 15.0f);
	}

	@Override
	public List<ItemStack> getDrops(BlockState state, net.minecraft.world.level.storage.loot.LootParams.Builder builder) {
		return List.of(new ItemStack(this));
	}

	@Override
	public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(LEVEL, CONTENT);
	}

	@Override
	public MapCodec<CopperKettleBlock> codec() {
		return CODEC;
	}

	public void setLevel(Level level, BlockPos pos, BlockState state, int fillLevel, ContentType contentType) {
		int clamped = Math.max(MIN_FILL_LEVEL, Math.min(MAX_FILL_LEVEL, fillLevel));
		ContentType targetContent = clamped > 0 ? contentType : ContentType.EMPTY;
		level.setBlock(pos, state.setValue(LEVEL, clamped).setValue(CONTENT, targetContent), Block.UPDATE_ALL);
	}

	@Override
	public void spawnAfterBreak(BlockState state, ServerLevel level, BlockPos pos, ItemStack stack, boolean dropExperience) {
		super.spawnAfterBreak(state, level, pos, stack, dropExperience);
		popResource(level, pos, new ItemStack(this));
	}

	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		if (!level.isClientSide() && !player.isCreative()) {
			popResource(level, pos, new ItemStack(this));
		}
		return super.playerWillDestroy(level, pos, state, player);
	}

	private boolean isWaterPotion(ItemStack stack) {
		return stack.is(Items.POTION);
	}

	private ItemStack createWaterBottle() {
		ItemStack stack = new ItemStack(Items.POTION);
		stack.set(net.minecraft.core.component.DataComponents.POTION_CONTENTS, PotionContents.EMPTY.withPotion(net.minecraft.world.item.alchemy.Potions.WATER));
		return stack;
	}

	private boolean isEmpty(BlockState state) {
		return state.getValue(LEVEL) <= 0;
	}

	private ContentType content(BlockState state) {
		return state.getValue(CONTENT);
	}

	public enum ContentType implements StringRepresentable {
		EMPTY("empty"),
		WATER("water"),
		ANIMAL_FAT("animal_fat"),
		OIL("oil");

		private final String serialized;

		ContentType(String serialized) {
			this.serialized = serialized;
		}

		@Override
		public String getSerializedName() {
			return serialized;
		}

		@Override
		public String toString() {
			return serialized;
		}
	}
}
