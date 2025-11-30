package com.ronhelwig.ffff.mixin;

import com.ronhelwig.ffff.blockentity.LanternBlockEntity;
import com.ronhelwig.ffff.mixin.BlockBehaviourPropertiesAccessor;
import com.ronhelwig.ffff.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LanternBlock.class)
public abstract class LanternBlockMixin extends Block implements EntityBlock {
	@Unique
	private static final BooleanProperty FFFF$LIT = BooleanProperty.create("lit");

	protected LanternBlockMixin(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Inject(method = "<init>", at = @At("TAIL"))
	private void ffff$initLantern(BlockBehaviour.Properties properties, CallbackInfo ci) {
		if (properties instanceof BlockBehaviourPropertiesAccessor accessor) {
			accessor.setLightEmission(state -> state.hasProperty(FFFF$LIT) && state.getValue(FFFF$LIT) ? 15 : 0);
		}
		this.registerDefaultState(
			this.stateDefinition.any()
				.setValue(LanternBlock.HANGING, Boolean.FALSE)
				.setValue(LanternBlock.WATERLOGGED, Boolean.FALSE)
				.setValue(FFFF$LIT, Boolean.FALSE)
		);
	}

	@Inject(method = "createBlockStateDefinition", at = @At("TAIL"))
	private void ffff$appendLitProperty(StateDefinition.Builder<Block, BlockState> builder, CallbackInfo ci) {
		builder.add(FFFF$LIT);
	}

	@Override
	public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}

		BlockEntity blockEntity = level.getBlockEntity(pos);
		if (blockEntity instanceof LanternBlockEntity lanternEntity) {
			player.openMenu(lanternEntity);
			return InteractionResult.CONSUME;
		}

		return InteractionResult.PASS;
	}

	@Override
	public void onPlace(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.is(newState.getBlock())) {
			BlockEntity blockEntity = level.getBlockEntity(pos);
			if (blockEntity instanceof LanternBlockEntity lanternEntity) {
				lanternEntity.dropContents();
				level.updateNeighbourForOutputSignal(pos, this);
			}
		}

		super.onPlace(state, level, pos, newState, isMoving);
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
		BlockEntity blockEntity = level.getBlockEntity(pos);
		if (blockEntity instanceof LanternBlockEntity lanternEntity) {
			return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(lanternEntity);
		}

		return 0;
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		super.setPlacedBy(level, pos, state, placer, stack);
		BlockEntity blockEntity = level.getBlockEntity(pos);
		if (blockEntity instanceof LanternBlockEntity lanternEntity) {
			lanternEntity.markPlayerPlaced();
		}
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new LanternBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		if (level.isClientSide()) {
			return null;
		}

		if (type != ModBlockEntities.LANTERN) {
			return null;
		}

		return (lvl, pos, blockState, blockEntity) -> {
			if (blockEntity instanceof LanternBlockEntity lantern) {
				LanternBlockEntity.serverTick(lvl, pos, blockState, lantern);
			}
		};
	}

}
