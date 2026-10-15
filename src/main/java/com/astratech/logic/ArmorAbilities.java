package com.astratech.logic;

import com.astratech.registry.ModItems;
import com.astratech.registry.ModWorlds;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Bonus abilities of the Astral Vanguard armour, applied once per second (effects are hidden/ambient).
 *
 * Helmet:     Night Vision, Water Breathing, blindness immunity
 * Chestplate: Resistance I, Ion Thrusters (hold jump in the air; sneak+jump to hover)
 * Leggings:   Speed II
 * Boots:      Jump Boost II, no fall damage
 * Full set:   Fire Resistance, Regeneration I, Strength I, Haste I
 * On the Moon everybody (armoured or not) gets Slow Falling and Jump Boost for low-gravity hopping.
 */
public final class ArmorAbilities {
	private ArmorAbilities() {
	}

	public static boolean hasHelmet(PlayerEntity p) {
		return p.getEquippedStack(EquipmentSlot.HEAD).isOf(ModItems.ASTRAL_HELMET);
	}

	public static boolean hasChest(PlayerEntity p) {
		return p.getEquippedStack(EquipmentSlot.CHEST).isOf(ModItems.ASTRAL_CHESTPLATE);
	}

	public static boolean hasLegs(PlayerEntity p) {
		return p.getEquippedStack(EquipmentSlot.LEGS).isOf(ModItems.ASTRAL_LEGGINGS);
	}

	public static boolean hasBoots(PlayerEntity p) {
		return p.getEquippedStack(EquipmentSlot.FEET).isOf(ModItems.ASTRAL_BOOTS);
	}

	public static boolean hasFullSet(PlayerEntity p) {
		return hasHelmet(p) && hasChest(p) && hasLegs(p) && hasBoots(p);
	}

	private static void give(ServerPlayerEntity p, StatusEffect effect, int duration, int amplifier) {
		p.addStatusEffect(new StatusEffectInstance(effect, duration, amplifier, true, false, true));
	}

	public static void tick(ServerPlayerEntity p) {
		boolean boots = hasBoots(p);
		if (boots) {
			p.fallDistance = 0.0f;
		}
		if (p.age % 20 != 0) {
			return;
		}
		if (p.getWorld().getRegistryKey().equals(ModWorlds.MOON)) {
			p.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 60, 0, true, false, false));
			if (!boots) {
				p.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 60, 1, true, false, false));
			}
		}
		if (hasHelmet(p)) {
			give(p, StatusEffects.NIGHT_VISION, 320, 0);
			give(p, StatusEffects.WATER_BREATHING, 100, 0);
			p.removeStatusEffect(StatusEffects.BLINDNESS);
		}
		if (hasChest(p)) {
			give(p, StatusEffects.RESISTANCE, 100, 0);
		}
		if (hasLegs(p)) {
			give(p, StatusEffects.SPEED, 100, 1);
		}
		if (boots) {
			give(p, StatusEffects.JUMP_BOOST, 100, 1);
		}
		if (hasFullSet(p)) {
			give(p, StatusEffects.FIRE_RESISTANCE, 100, 0);
			give(p, StatusEffects.REGENERATION, 100, 0);
			give(p, StatusEffects.STRENGTH, 100, 0);
			give(p, StatusEffects.HASTE, 100, 0);
		}
	}
}
