package com.ronhelwig.ffff.mixin;

import java.util.function.ToIntFunction;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockBehaviour.Properties.class)
public interface BlockBehaviourPropertiesAccessor {
	@Accessor("lightEmission")
	@Mutable
	void setLightEmission(ToIntFunction<BlockState> lightLevel);
}
