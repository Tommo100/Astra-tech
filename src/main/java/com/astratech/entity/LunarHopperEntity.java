package com.astratech.entity;

import net.minecraft.entity.EntityType;
import com.astratech.registry.ModEntities;
import com.astratech.registry.ModItems;
import net.minecraft.entity.ai.goal.AnimalMateGoal;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.goal.FollowParentGoal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.server.world.ServerWorld;
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
public class LunarHopperEntity extends AnimalEntity {
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
		this.goalSelector.add(2, new AnimalMateGoal(this, 1.0));
		this.goalSelector.add(3, new TemptGoal(this, 1.1, Ingredient.ofItems(ModItems.PLASMA_CRYSTAL), false));
		this.goalSelector.add(4, new FollowParentGoal(this, 1.1));
		this.goalSelector.add(5, new WanderAroundFarGoal(this, 0.7));
		this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 6.0f));
		this.goalSelector.add(7, new LookAroundGoal(this));
	}

	@Override
	public net.minecraft.entity.EntityData initialize(net.minecraft.world.ServerWorldAccess world, net.minecraft.world.LocalDifficulty difficulty,
			net.minecraft.entity.SpawnReason reason, net.minecraft.entity.EntityData entityData, net.minecraft.nbt.NbtCompound nbt) {
		if (reason == net.minecraft.entity.SpawnReason.NATURAL && this.random.nextFloat() < 0.15f) {
			this.setBaby(true);
		}
		return super.initialize(world, difficulty, reason, entityData, nbt);
	}

	@Override
	public boolean isBreedingItem(ItemStack stack) {
		return stack.isOf(ModItems.PLASMA_CRYSTAL);
	}

	@Override
	public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
		return ModEntities.LUNAR_HOPPER.create(world);
	}

	@Override
	public boolean handleFallDamage(float fallDistance, float damageMultiplier, net.minecraft.entity.damage.DamageSource damageSource) {
		return false;
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
