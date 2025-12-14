package com.ronhelwig.ffff.block;

import com.ronhelwig.ffff.blockentity.BronzeHopperBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.storage.loot.LootParams;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public class BronzeHopperBlock extends HopperBlock {

	public BronzeHopperBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
		List<ItemStack> drops = super.getDrops(state, params);
		if (drops == null || drops.isEmpty()) {
			return List.of(new ItemStack(this));
		}
		return drops;
	}

	@Override
	@Nullable
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new BronzeHopperBlockEntity(pos, state);
	}

	@Override
	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		if (level.isClientSide() || type != BlockEntityType.HOPPER) {
			return null;
		}

		return (lvl, pos, blockState, blockEntity) -> {
			if (blockEntity instanceof BronzeHopperBlockEntity bronze) {
				BronzeHopperBlockEntity.serverTick(lvl, pos, blockState, bronze);
			}
		};
	}
}
