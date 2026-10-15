package com.astratech.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.control.FlightMoveControl;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.FlyGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.ProjectileAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

/** Hovering gun drone that strafes and fires plasma bolts. */
public class PlasmaDroneEntity extends HostileEntity implements RangedAttackMob {
	public PlasmaDroneEntity(EntityType<? extends PlasmaDroneEntity> type, World world) {
		super(type, world);
		this.moveControl = new FlightMoveControl(this, 20, true);
		this.setNoGravity(true);
		this.experiencePoints = 35;
	}

	public static DefaultAttributeContainer.Builder createAttributes() {
		return HostileEntity.createHostileAttributes()
			.add(EntityAttributes.GENERIC_MAX_HEALTH, 40.0)
			.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
			.add(EntityAttributes.GENERIC_FLYING_SPEED, 0.6)
			.add(EntityAttributes.GENERIC_ARMOR, 6.0)
			.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 48.0);
	}

	@Override
	protected EntityNavigation createNavigation(World world) {
		BirdNavigation navigation = new BirdNavigation(this, world);
		navigation.setCanPathThroughDoors(false);
		navigation.setCanSwim(true);
		navigation.setCanEnterOpenDoors(true);
		return navigation;
	}

	@Override
	protected void initGoals() {
		this.goalSelector.add(1, new ProjectileAttackGoal(this, 1.0, 25, 24.0f));
		this.goalSelector.add(5, new FlyGoal(this, 1.0));
		this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 16.0f));
		this.targetSelector.add(1, new RevengeGoal(this));
		this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
		this.targetSelector.add(3, new ActiveTargetGoal<>(this, AstroBotEntity.class, true));
	}

	@Override
	public void attack(LivingEntity target, float pullProgress) {
		PlasmaBoltEntity bolt = new PlasmaBoltEntity(this.getWorld(), this);
		bolt.setDamage(7.0f);
		double dx = target.getX() - this.getX();
		double dy = target.getBodyY(0.5) - this.getEyeY();
		double dz = target.getZ() - this.getZ();
		bolt.setVelocity(dx, dy, dz, 2.2f, 1.0f);
		this.getWorld().playSound(null, this.getBlockPos(), SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.HOSTILE, 0.8f, 1.8f);
		this.getWorld().spawnEntity(bolt);
	}
}
