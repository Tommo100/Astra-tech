package com.astratech.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.world.World;

/** Fast, wall-climbing hunter whose bite withers its prey. */
public class VoidStalkerEntity extends SpiderEntity {
	public VoidStalkerEntity(EntityType<? extends VoidStalkerEntity> type, World world) {
		super(type, world);
		this.experiencePoints = 35;
	}

	public static DefaultAttributeContainer.Builder createAttributes() {
		return SpiderEntity.createSpiderAttributes()
			.add(EntityAttributes.GENERIC_MAX_HEALTH, 50.0)
			.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 7.0)
			.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.38)
			.add(EntityAttributes.GENERIC_ARMOR, 8.0)
			.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 40.0);
	}

	@Override
	public boolean tryAttack(Entity target) {
		boolean hit = super.tryAttack(target);
		if (hit && target instanceof LivingEntity living) {
			living.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 60, 0), this);
		}
		return hit;
	}
}
