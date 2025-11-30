package com.ronhelwig.ffff.mixin;

import com.ronhelwig.ffff.blockentity.TimedTorchBlockEntity;
import com.ronhelwig.ffff.registry.ModBlockEntities;
import com.ronhelwig.ffff.registry.ModItems;
import com.ronhelwig.ffff.util.VanillaReferences;
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;


@Mixin(TorchBlock.class)
public abstract class TorchBlockMixin extends Block implements EntityBlock {
	private static final Int2LongOpenHashMap LAST_CONTACT_TICK = new Int2LongOpenHashMap();
	private static final Int2LongOpenHashMap LAST_PERIODIC_DAMAGE_TICK = new Int2LongOpenHashMap();
	private static final long CONTACT_RESET_TICKS = 3L;
	private static final long PERIODIC_DAMAGE_INTERVAL_TICKS = 20L;
	private static final float CONTACT_DAMAGE_CHANCE = 0.3f;

	static {
		LAST_CONTACT_TICK.defaultReturnValue(Long.MIN_VALUE / 2L);
		LAST_PERIODIC_DAMAGE_TICK.defaultReturnValue(Long.MIN_VALUE / 2L);
	}

	protected TorchBlockMixin(BlockBehaviour.Properties settings) {
		super(settings);
	}

	private static boolean isTimedTorch(BlockState state) {
		return state.is(Blocks.TORCH)
			|| state.is(Blocks.WALL_TORCH)
			|| state.is(VanillaReferences.COPPER_TORCH_BLOCK)
			|| state.is(VanillaReferences.COPPER_WALL_TORCH_BLOCK);
	}

	@Override
	public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
		if (isTimedTorch(state)) {
			return Collections.singletonList(new ItemStack(ModItems.KINDLING));
		}

		return super.getDrops(state, builder);
	}

	@Override
	public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean bl) {
		super.entityInside(state, level, pos, entity, effectApplier, bl);

		if (!isTimedTorch(state) || level.isClientSide()) {
			return;
		}

		if (!(entity instanceof LivingEntity living) || living.fireImmune()) {
			return;
		}

		if (!(level instanceof ServerLevel serverLevel)) {
			return;
		}

		applyTorchContactEffects(serverLevel, pos, living);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return isTimedTorch(state) ? new TimedTorchBlockEntity(pos, state) : null;
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		if (!isTimedTorch(state) || level.isClientSide()) {
			return null;
		}

		if (type != ModBlockEntities.TORCH_TIMER) {
			return null;
		}

		return (world, pos, blockState, blockEntity) -> {
			if (blockEntity instanceof TimedTorchBlockEntity timer) {
				BlockState currentState = world.getBlockState(pos);
				TimedTorchBlockEntity.tick(world, pos, currentState, timer);
			}
		};
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
		super.setPlacedBy(level, pos, state, placer, itemStack);

		if (level.isClientSide()) {
			return;
		}

		BlockEntity blockEntity = level.getBlockEntity(pos);
		if (blockEntity instanceof TimedTorchBlockEntity timer) {
			timer.markPlayerPlaced();
		}
	}

	private static void applyTorchContactEffects(ServerLevel level, BlockPos pos, LivingEntity entity) {
		int entityId = entity.getId();
		long gameTime = level.getGameTime();
		long lastContactTick = LAST_CONTACT_TICK.get(entityId);
		boolean newContact = gameTime - lastContactTick > CONTACT_RESET_TICKS;

		if (newContact) {
			LAST_PERIODIC_DAMAGE_TICK.put(entityId, gameTime);
			level.playSound(null, pos, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.5f, 1.4f + (level.getRandom().nextFloat() * 0.2f));
			if (level.getRandom().nextFloat() < CONTACT_DAMAGE_CHANCE) {
				applyFireDamage(level, entity);
			}
		}

		long lastPeriodic = LAST_PERIODIC_DAMAGE_TICK.get(entityId);
		if (gameTime - lastPeriodic >= PERIODIC_DAMAGE_INTERVAL_TICKS) {
			applyFireDamage(level, entity);
			LAST_PERIODIC_DAMAGE_TICK.put(entityId, gameTime);
		}

		LAST_CONTACT_TICK.put(entityId, gameTime);
	}

	private static void applyFireDamage(ServerLevel level, LivingEntity entity) {
		entity.hurt(level.damageSources().inFire(), 1.0f);
		int fireTicks = Math.max(entity.getRemainingFireTicks(), 20);
		entity.setRemainingFireTicks(fireTicks);
	}
}
