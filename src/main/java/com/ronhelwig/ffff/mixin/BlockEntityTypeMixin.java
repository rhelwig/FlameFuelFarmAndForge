package com.ronhelwig.ffff.mixin;

import com.ronhelwig.ffff.blockentity.BronzeHopperBlockEntity;
import com.ronhelwig.ffff.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockEntityType.class)
abstract class BlockEntityTypeMixin {
	@Inject(method = "create", at = @At("HEAD"), cancellable = true)
	private void ffff$createBronzeHopper(BlockPos pos, BlockState state, CallbackInfoReturnable<BlockEntity> cir) {
		if ((Object) this != BlockEntityType.HOPPER) {
			return;
		}

		if (!state.is(ModBlocks.BRONZE_HOPPER)) {
			return;
		}

		cir.setReturnValue(new BronzeHopperBlockEntity(pos, state));
	}
}
