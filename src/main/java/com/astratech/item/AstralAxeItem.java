package com.astratech.item;

import com.astratech.material.AstralToolMaterial;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;

/** Gravity Cleaver: heavy hits that launch targets into the air. */
public class AstralAxeItem extends AxeItem {
	public AstralAxeItem(Settings settings) {
		super(AstralToolMaterial.INSTANCE, 5.0f, -3.0f, settings);
	}

	@Override
	public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		boolean result = super.postHit(stack, target, attacker);
		target.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 30, 1));
		return result;
	}
}
