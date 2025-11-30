package com.ronhelwig.ffff.block;

import java.util.Collections;
import java.util.List;

import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;

/**
 * Torch variant that represents a fully burnt-out torch.
 * It emits no light and never drops items.
 */
public class BurntTorchBlock extends TorchBlock {
	public BurntTorchBlock(BlockBehaviour.Properties settings, SimpleParticleType particle) {
		super(particle, settings);
	}

	@Override
	public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
		return Collections.emptyList();
	}
}
