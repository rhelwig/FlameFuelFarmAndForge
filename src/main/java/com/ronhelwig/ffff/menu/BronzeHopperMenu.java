package com.ronhelwig.ffff.menu;

import com.ronhelwig.ffff.registry.ModMenuTypes;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class BronzeHopperMenu extends AbstractContainerMenu {
	private static final int INVENTORY_SIZE = 3;
	private static final int PLAYER_INVENTORY_ROWS = 3;

	private final Container hopperInventory;

	public BronzeHopperMenu(int syncId, Inventory playerInventory) {
		this(syncId, playerInventory, new SimpleContainer(INVENTORY_SIZE));
	}

	public BronzeHopperMenu(int syncId, Inventory playerInventory, Container hopperInventory) {
		this(ModMenuTypes.BRONZE_HOPPER, syncId, playerInventory, hopperInventory);
	}

	private BronzeHopperMenu(MenuType<?> type, int syncId, Inventory playerInventory, Container hopperInventory) {
		super(type, syncId);
		checkContainerSize(hopperInventory, INVENTORY_SIZE);
		this.hopperInventory = hopperInventory;
		hopperInventory.startOpen(playerInventory.player);

		for (int slot = 0; slot < INVENTORY_SIZE; slot++) {
			this.addSlot(new Slot(hopperInventory, slot, 62 + slot * 18, 20));
		}

		for (int row = 0; row < PLAYER_INVENTORY_ROWS; row++) {
			for (int column = 0; column < 9; column++) {
				this.addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 51 + row * 18));
			}
		}

		for (int hotbarSlot = 0; hotbarSlot < 9; hotbarSlot++) {
			this.addSlot(new Slot(playerInventory, hotbarSlot, 8 + hotbarSlot * 18, 109));
		}
	}

	@Override
	public boolean stillValid(Player player) {
		return this.hopperInventory.stillValid(player);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		ItemStack newStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);
		if (slot != null && slot.hasItem()) {
			ItemStack original = slot.getItem();
			newStack = original.copy();
			if (index < INVENTORY_SIZE) {
				if (!this.moveItemStackTo(original, INVENTORY_SIZE, this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.moveItemStackTo(original, 0, INVENTORY_SIZE, false)) {
				return ItemStack.EMPTY;
			}

			if (original.isEmpty()) {
				slot.set(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}

			if (original.getCount() == newStack.getCount()) {
				return ItemStack.EMPTY;
			}

			slot.onTake(player, original);
		}

		return newStack;
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		this.hopperInventory.stopOpen(player);
	}
}
