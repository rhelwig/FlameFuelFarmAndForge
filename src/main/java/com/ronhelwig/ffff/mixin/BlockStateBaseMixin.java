package com.ronhelwig.ffff.mixin;

import static com.ronhelwig.ffff.FlameFuelFarmAndForge.MOD_ID;

import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin {
	@Shadow
	public abstract Block getBlock();

	private static final Set<ResourceLocation> FFFF$SOFT_PICKAXE_BLOCK_IDS = Set.of(
		ResourceLocation.parse("minecraft:copper_ore"),
		ResourceLocation.parse("minecraft:deepslate_copper_ore"),
		ResourceLocation.fromNamespaceAndPath(MOD_ID, "tin_ore"),
		ResourceLocation.fromNamespaceAndPath(MOD_ID, "zinc_ore")
	);

	@Inject(method = "requiresCorrectToolForDrops", at = @At("HEAD"), cancellable = true)
	private void ffff$allowSimpleOres(CallbackInfoReturnable<Boolean> cir) {
		Block block = getBlock();
		ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(block);
		if (blockId != null && FFFF$SOFT_PICKAXE_BLOCK_IDS.contains(blockId)) {
			cir.setReturnValue(false);
		}
	}
}
