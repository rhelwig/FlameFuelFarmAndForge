package com.ronhelwig.ffff.menu;

import com.ronhelwig.ffff.registry.ModItems;
import com.ronhelwig.ffff.registry.ModMenuTypes;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class LanternMenu extends AbstractContainerMenu {
	private static final int INVENTORY_SIZE = 1;
	private static final int PLAYER_INVENTORY_ROWS = 3;
	private static final int FUEL_SLOT_X = 80;
	private static final int FUEL_SLOT_Y = 20;

	private final Container lanternInventory;

	public LanternMenu(int syncId, Inventory playerInventory) {
		this(syncId, playerInventory, new SimpleContainer(INVENTORY_SIZE));
	}

	public LanternMenu(int syncId, Inventory playerInventory, Container lanternInventory) {
		this(ModMenuTypes.LANTERN, syncId, playerInventory, lanternInventory);
	}

	private LanternMenu(MenuType<?> type, int syncId, Inventory playerInventory, Container lanternInventory) {
		super(type, syncId);
		checkContainerSize(lanternInventory, INVENTORY_SIZE);
		this.lanternInventory = lanternInventory;
		lanternInventory.startOpen(playerInventory.player);

		this.addSlot(new FuelSlot(lanternInventory, 0, FUEL_SLOT_X, FUEL_SLOT_Y));

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
		return this.lanternInventory.stillValid(player);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		ItemStack newStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);
		if (slot != null && slot.hasItem()) {
			ItemStack original = slot.getItem();
			newStack = original.copy();
			if (index == 0) {
				if (!this.moveItemStackTo(original, INVENTORY_SIZE, this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else {
				if (!original.is(ModItems.ANIMAL_FAT)) {
					return ItemStack.EMPTY;
				}

				if (!this.moveItemStackTo(original, 0, INVENTORY_SIZE, false)) {
					return ItemStack.EMPTY;
				}
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
		this.lanternInventory.stopOpen(player);
	}

	private static class FuelSlot extends Slot {
		public FuelSlot(Container inventory, int index, int x, int y) {
			super(inventory, index, x, y);
		}

		@Override
		public boolean mayPlace(ItemStack stack) {
			return stack.is(ModItems.ANIMAL_FAT) || stack.is(ModItems.OIL_BOTTLE);
		}

		@Override
		public int getMaxStackSize() {
			return 16;
		}

		@Override
		public int getMaxStackSize(ItemStack stack) {
			return this.mayPlace(stack) ? 16 : super.getMaxStackSize(stack);
		}
	}
}
