package com.ronhelwig.ffff.mixin;

import static com.ronhelwig.ffff.FlameFuelFarmAndForge.MOD_ID;

import com.ronhelwig.ffff.registry.ModItems;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public abstract class BlockDropsMixin {

	@Inject(method = "getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;", at = @At("RETURN"), cancellable = true)
	private static void ffff$ensureOreDrops(BlockState state, ServerLevel level, BlockPos pos, BlockEntity blockEntity, Entity entity, ItemStack tool, CallbackInfoReturnable<List<ItemStack>> cir) {
		List<ItemStack> drops = cir.getReturnValue();
		if (drops != null && !drops.isEmpty()) {
			return;
		}

		ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());
		if (blockId == null) {
			return;
		}

		boolean isTinOre = blockId.getNamespace().equals(MOD_ID) && "tin_ore".equals(blockId.getPath());
		boolean isZincOre = blockId.getNamespace().equals(MOD_ID) && "zinc_ore".equals(blockId.getPath());
		if (!isTinOre && !isZincOre) {
			return;
		}

		Holder<Enchantment> silkTouch = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH);
		boolean hasSilkTouch = tool != null && EnchantmentHelper.getItemEnchantmentLevel(silkTouch, tool) > 0;
		if (hasSilkTouch) {
			cir.setReturnValue(List.of(new ItemStack(state.getBlock())));
			return;
		}

		Holder<Enchantment> fortune = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE);
		int fortuneLevel = tool != null ? EnchantmentHelper.getItemEnchantmentLevel(fortune, tool) : 0;
		int baseCount = level.random.nextInt(4) + 2; // 2-5 like copper ore
		int multiplierBonus = 0;
		if (fortuneLevel > 0) {
			int bonus = level.random.nextInt(fortuneLevel + 2) - 1;
			multiplierBonus = Math.max(0, bonus);
		}
		int totalCount = baseCount * (multiplierBonus + 1);
		ItemStack drop = new ItemStack(isTinOre ? ModItems.RAW_TIN : ModItems.RAW_ZINC, totalCount);
		cir.setReturnValue(List.of(drop));
	}
}
