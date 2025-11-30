package com.ronhelwig.ffff.combat;

import com.ronhelwig.ffff.util.VanillaReferences;
import java.util.Map;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

/**
 * Applies extra fire effects whenever torches are swung as weapons.
 */
public final class TorchCombatHandler {
	private static final Map<Item, TorchDamageProfile> PROFILES = Map.of(
		Items.TORCH, new TorchDamageProfile(0.4f, 0.8f, 0.08f, 2),
		VanillaReferences.COPPER_TORCH_ITEM, new TorchDamageProfile(0.55f, 1.0f, 0.12f, 2),
		Items.REDSTONE_TORCH, new TorchDamageProfile(0.7f, 1.2f, 0.18f, 3),
		Items.SOUL_TORCH, new TorchDamageProfile(0.9f, 1.4f, 0.26f, 4)
	);

	private TorchCombatHandler() {}

	public static void init() {
		AttackEntityCallback.EVENT.register(TorchCombatHandler::applyTorchHitEffects);
	}

	private static InteractionResult applyTorchHitEffects(Player attacker, Level level, InteractionHand hand, Entity target, EntityHitResult hit) {
		if (level.isClientSide()) {
			return InteractionResult.PASS;
		}

		if (!(level instanceof ServerLevel serverLevel)) {
			return InteractionResult.PASS;
		}

		if (!(target instanceof LivingEntity livingTarget)) {
			return InteractionResult.PASS;
		}

		ItemStack stack = attacker.getItemInHand(hand);
		TorchDamageProfile profile = PROFILES.get(stack.getItem());
		if (profile == null) {
			return InteractionResult.PASS;
		}

		RandomSource random = serverLevel.getRandom();
		float damage = profile.randomDamage(random);
		if (damage > 0.0f) {
			livingTarget.hurt(serverLevel.damageSources().inFire(), damage);
		}

		if (!livingTarget.fireImmune() && profile.igniteSeconds() > 0 && random.nextFloat() < profile.igniteChance()) {
			int igniteTicks = Math.max(livingTarget.getRemainingFireTicks(), profile.igniteSeconds() * 20);
			livingTarget.setRemainingFireTicks(igniteTicks);
		}

		return InteractionResult.PASS;
	}

	private record TorchDamageProfile(float minDamage, float maxDamage, float igniteChance, int igniteSeconds) {
		private float randomDamage(RandomSource random) {
			if (maxDamage <= minDamage) {
				return Math.max(minDamage, 0.0f);
			}

			float range = maxDamage - minDamage;
			return Math.max(0.0f, minDamage + (random.nextFloat() * range));
		}
	}
}
