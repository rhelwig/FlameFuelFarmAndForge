package com.ronhelwig.ffff.mixin;

import com.ronhelwig.ffff.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(HopperBlockEntity.class)
abstract class HopperBlockEntityMixin {
	@Redirect(
		method = "pushItemsTick(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/entity/HopperBlockEntity;)V",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/HopperBlockEntity;setCooldown(I)V")
	)
	private static void ffff$slowBronzeHoppers(HopperBlockEntity blockEntity, int cooldown, Level level, BlockPos pos, BlockState state, HopperBlockEntity ignored) {
		int adjustedCooldown = cooldown;
		if (cooldown > 0 && state.is(ModBlocks.BRONZE_HOPPER)) {
			adjustedCooldown = cooldown * 2;
		}

		((HopperBlockEntityAccessor) blockEntity).ffff$setCooldownTime(adjustedCooldown);
	}
}
