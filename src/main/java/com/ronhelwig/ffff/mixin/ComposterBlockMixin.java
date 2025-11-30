package com.ronhelwig.ffff.mixin;

import com.ronhelwig.ffff.block.CopperKettleBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ComposterBlock.class)
abstract class ComposterBlockMixin {
	@Inject(method = "extractProduce", at = @At("RETURN"))
	private static void ffff$dropOilIfKettle(net.minecraft.world.entity.Entity entity, BlockState state, Level level, BlockPos pos, CallbackInfoReturnable<BlockState> cir) {
		if (level.isClientSide()) {
			return;
		}

		BlockPos below = pos.below();
		BlockState belowState = level.getBlockState(below);
		if (!(belowState.getBlock() instanceof CopperKettleBlock kettle)) {
			return;
		}

		int current = belowState.getValue(CopperKettleBlock.LEVEL);
		CopperKettleBlock.ContentType content = belowState.getValue(CopperKettleBlock.CONTENT);
		if (current >= CopperKettleBlock.MAX_FILL_LEVEL) {
			return;
		}

		if (content != CopperKettleBlock.ContentType.EMPTY && content != CopperKettleBlock.ContentType.OIL) {
			return;
		}

		kettle.setLevel(level, below, belowState, current + 1, CopperKettleBlock.ContentType.OIL);
	}
}
