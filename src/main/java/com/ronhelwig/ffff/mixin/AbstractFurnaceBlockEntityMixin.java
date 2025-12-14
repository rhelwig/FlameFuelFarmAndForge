package com.ronhelwig.ffff.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Ensure fuels with a crafting remainder (e.g., oil bottles) drop their empty container
 * instead of blocking the fuel slot.
 */
@Mixin(AbstractFurnaceBlockEntity.class)
abstract class AbstractFurnaceBlockEntityMixin {

	@Redirect(
		method = "serverTick",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V")
	)
	private static void ffff$dropFuelRemainderEachBurn(ItemStack fuelStack, int amount, ServerLevel level, BlockPos pos, BlockState state, AbstractFurnaceBlockEntity blockEntity) {
		ItemStack remainder = fuelStack.getItem().getCraftingRemainder();
		fuelStack.shrink(amount);

		if (remainder.isEmpty()) {
			return;
		}

		ItemStack leftover = remainder.copy();
		Container containerBelow = HopperBlockEntity.getContainerAt(level, pos.below());
		if (containerBelow != null) {
			leftover = HopperBlockEntity.addItem(blockEntity, containerBelow, leftover, Direction.UP);
		}

		if (!leftover.isEmpty()) {
			Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, leftover);
		}
	}

	@Redirect(
		method = "serverTick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/core/NonNullList;set(ILjava/lang/Object;)Ljava/lang/Object;",
			ordinal = 0
		)
	)
	private static Object ffff$divertFuelRemainder(NonNullList<ItemStack> stacks, int slot, Object remainderObj, ServerLevel level, BlockPos pos, BlockState state, AbstractFurnaceBlockEntity blockEntity) {
		if (!(remainderObj instanceof ItemStack remainder)) {
			return stacks.set(slot, (ItemStack) remainderObj);
		}

		if (slot != 1) {
			return stacks.set(slot, remainder);
		}

		// Fuel stack is already shrunk by the redirect above; just clear the slot instead of parking the remainder.
		stacks.set(slot, ItemStack.EMPTY);
		return remainder;
	}
}
