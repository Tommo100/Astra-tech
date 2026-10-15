package com.astratech.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.world.World;

/** Armoured combat mech. Resists projectiles and goes into overdrive below half health. */
public class LunarSentinelEntity extends HostileEntity {
	public LunarSentinelEntity(EntityType<? extends LunarSentinelEntity> type, World world) {
		super(type, world);
		this.experiencePoints = 45;
	}

	public static DefaultAttributeContainer.Builder createAttributes() {
		return HostileEntity.createHostileAttributes()
			.add(EntityAttributes.GENERIC_MAX_HEALTH, 80.0)
			.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 9.0)
			.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.27)
			.add(EntityAttributes.GENERIC_ARMOR, 14.0)
			.add(EntityAttributes.GENERIC_ARMOR_TOUGHNESS, 6.0)
			.add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.6)
			.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 40.0);
	}

	@Override
	protected void initGoals() {
		this.goalSelector.add(0, new SwimGoal(this));
		this.goalSelector.add(2, new MeleeAttackGoal(this, 1.1, false));
		this.goalSelector.add(5, new WanderAroundFarGoal(this, 0.8));
		this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 12.0f));
		this.goalSelector.add(7, new LookAroundGoal(this));
		this.targetSelector.add(1, new RevengeGoal(this));
		this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
		this.targetSelector.add(3, new ActiveTargetGoal<>(this, AstroBotEntity.class, true));
	}

	@Override
	public boolean damage(DamageSource source, float amount) {
		if (source.isIn(DamageTypeTags.IS_PROJECTILE)) {
			amount *= 0.4f;
		}
		return super.damage(source, amount);
	}

	@Override
	public void tickMovement() {
		super.tickMovement();
		if (!this.getWorld().isClient && this.age % 40 == 0 && this.getHealth() < this.getMaxHealth() * 0.5f) {
			this.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 80, 1, true, false));
			this.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 80, 0, true, false));
		}
	}
}
