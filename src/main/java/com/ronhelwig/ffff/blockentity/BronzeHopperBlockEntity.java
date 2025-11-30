package com.ronhelwig.ffff.blockentity;

import com.ronhelwig.ffff.menu.BronzeHopperMenu;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;

public class BronzeHopperBlockEntity extends HopperBlockEntity {
	public static final int INVENTORY_SIZE = 3;

	private final List<ItemStack> pendingExcessDrops = new ArrayList<>();

	public BronzeHopperBlockEntity(BlockPos pos, BlockState state) {
		super(pos, state);
		ensureInventorySize(false);
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, BronzeHopperBlockEntity blockEntity) {
		HopperBlockEntity.pushItemsTick(level, pos, state, blockEntity);
	}

	@Override
	public int getContainerSize() {
		return INVENTORY_SIZE;
	}

	@Override
	protected NonNullList<ItemStack> getItems() {
		return ensureInventorySize(false);
	}

	@Override
	protected void setItems(NonNullList<ItemStack> items) {
		super.setItems(resizeInventory(items, true));
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		ensureInventorySize(true);
	}

	@Override
	protected Component getDefaultName() {
		return Component.translatable("container.flame-fuel-farm-and-forge.bronze_hopper");
	}

	@Override
	protected AbstractContainerMenu createMenu(int syncId, Inventory playerInventory) {
		return new BronzeHopperMenu(syncId, playerInventory, this);
	}

	@Override
	public void setLevel(Level level) {
		super.setLevel(level);
		if (!this.pendingExcessDrops.isEmpty() && level instanceof ServerLevel serverLevel) {
			double centerX = this.worldPosition.getX() + 0.5;
			double centerY = this.worldPosition.getY() + 0.5;
			double centerZ = this.worldPosition.getZ() + 0.5;
			for (ItemStack stack : this.pendingExcessDrops) {
				if (!stack.isEmpty()) {
					Containers.dropItemStack(serverLevel, centerX, centerY, centerZ, stack.copy());
				}
			}
		}
		this.pendingExcessDrops.clear();
	}

	private NonNullList<ItemStack> ensureInventorySize(boolean collectExcess) {
		NonNullList<ItemStack> stacks = super.getItems();
		if (stacks.size() == INVENTORY_SIZE) {
			return stacks;
		}
		NonNullList<ItemStack> resized = resizeInventory(stacks, collectExcess);
		super.setItems(resized);
		return resized;
	}

	private NonNullList<ItemStack> resizeInventory(NonNullList<ItemStack> source, boolean collectExcess) {
		NonNullList<ItemStack> resized = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
		int limit = Math.min(INVENTORY_SIZE, source.size());
		for (int i = 0; i < limit; i++) {
			resized.set(i, source.get(i));
		}

		if (collectExcess && source.size() > INVENTORY_SIZE) {
			for (int i = INVENTORY_SIZE; i < source.size(); i++) {
				ItemStack stack = source.get(i);
				if (!stack.isEmpty()) {
					this.pendingExcessDrops.add(stack.copy());
				}
			}
		}

		return resized;
	}

}
