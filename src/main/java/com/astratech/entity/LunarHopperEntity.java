package com.astratech.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

/** Gentle low-gravity creature that bounds across the regolith. */
public class LunarHopperEntity extends PathAwareEntity {
	public LunarHopperEntity(EntityType<? extends LunarHopperEntity> type, World world) {
		super(type, world);
	}

	public static DefaultAttributeContainer.Builder createAttributes() {
		return PathAwareEntity.createMobAttributes()
			.add(EntityAttributes.GENERIC_MAX_HEALTH, 24.0)
			.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.28)
			.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 16.0);
	}

	@Override
	protected void initGoals() {
		this.goalSelector.add(0, new SwimGoal(this));
		this.goalSelector.add(1, new EscapeDangerGoal(this, 1.6));
		this.goalSelector.add(5, new WanderAroundFarGoal(this, 0.7));
		this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 6.0f));
		this.goalSelector.add(7, new LookAroundGoal(this));
	}

	@Override
	protected float getJumpVelocity() {
		return 0.75f;
	}

	@Override
	public void tickMovement() {
		super.tickMovement();
		if (!this.getWorld().isClient && this.isOnGround()
			&& this.getVelocity().horizontalLengthSquared() > 0.0004 && this.random.nextInt(6) == 0) {
			this.getJumpControl().setActive();
		}
	}

	@Override
	public boolean canImmediatelyDespawn(double distanceSquared) {
		return false;
	}
}
