package com.astratech.entity;

import com.astratech.registry.ModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.ProjectileAttackGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.UUID;

/** Friendly support robot: heals nearby players and shoots hostile lunar creatures. */
public class AstroBotEntity extends PathAwareEntity implements RangedAttackMob {
	private UUID ownerUuid;

	public AstroBotEntity(EntityType<? extends AstroBotEntity> type, World world) {
		super(type, world);
	}

	public UUID getOwnerUuid() {
		return this.ownerUuid;
	}

	public void setOwner(PlayerEntity owner) {
		this.ownerUuid = owner.getUuid();
		this.setPersistent();
		net.minecraft.entity.attribute.EntityAttributeInstance hp = this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
		if (hp != null) {
			hp.setBaseValue(220.0);
		}
		this.setHealth(this.getMaxHealth());
		this.setCustomName(net.minecraft.text.Text.literal(owner.getName().getString() + "'s Astro Bot"));
		this.setCustomNameVisible(false);
	}

	public PlayerEntity getCompanionOwner() {
		return this.ownerUuid == null ? null : this.getWorld().getPlayerByUuid(this.ownerUuid);
	}

	@Override
	public void writeCustomDataToNbt(NbtCompound nbt) {
		super.writeCustomDataToNbt(nbt);
		if (this.ownerUuid != null) {
			nbt.putUuid("CompanionOwner", this.ownerUuid);
		}
	}

	@Override
	public void readCustomDataFromNbt(NbtCompound nbt) {
		super.readCustomDataFromNbt(nbt);
		if (nbt.containsUuid("CompanionOwner")) {
			this.ownerUuid = nbt.getUuid("CompanionOwner");
		}
	}

	@Override
	public boolean damage(net.minecraft.entity.damage.DamageSource source, float amount) {
		if (this.ownerUuid != null && source.getAttacker() != null && source.getAttacker().getUuid().equals(this.ownerUuid)) {
			return false;
		}
		return super.damage(source, amount);
	}

	public static DefaultAttributeContainer.Builder createAttributes() {
		return PathAwareEntity.createMobAttributes()
			.add(EntityAttributes.GENERIC_MAX_HEALTH, 60.0)
			.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
			.add(EntityAttributes.GENERIC_ARMOR, 8.0)
			.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 24.0);
	}

	@Override
	protected void initGoals() {
		this.goalSelector.add(0, new SwimGoal(this));
		this.goalSelector.add(1, new ProjectileAttackGoal(this, 1.0, 20, 16.0f));
		this.goalSelector.add(3, new FollowOwnerGoal(this, 1.25, 3.0f, 14.0f));
		this.goalSelector.add(5, new WanderAroundFarGoal(this, 0.8) {
			@Override
			public boolean canStart() {
				return AstroBotEntity.this.ownerUuid == null && super.canStart();
			}
		});
		this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
		this.goalSelector.add(7, new LookAroundGoal(this));
		this.targetSelector.add(1, new DefendOwnerGoal(this));
		this.targetSelector.add(2, new AssistOwnerGoal(this));
		this.targetSelector.add(3, new ActiveTargetGoal<>(this, HostileEntity.class, 5, true, false, e -> true));
	}

	@Override
	public void tickMovement() {
		super.tickMovement();
		if (!this.getWorld().isClient && this.age % 40 == 0) {
			for (PlayerEntity player : this.getWorld().getEntitiesByClass(PlayerEntity.class, this.getBoundingBox().expand(8.0), p -> p.isAlive() && !p.isSpectator())) {
				player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 80, 1, true, true));
				if (this.getWorld() instanceof ServerWorld serverWorld) {
					serverWorld.spawnParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getBodyY(0.8), player.getZ(), 4, 0.3, 0.3, 0.3, 0.0);
				}
			}
		}
	}

	@Override
	public ActionResult interactMob(PlayerEntity player, Hand hand) {
		ItemStack stack = player.getStackInHand(hand);
		if (stack.isOf(ModItems.PLASMA_CRYSTAL)) {
			if (!this.getWorld().isClient) {
				this.heal(30.0f);
				if (!player.getAbilities().creativeMode) {
					stack.decrement(1);
				}
				if (this.getWorld() instanceof ServerWorld serverWorld) {
					serverWorld.spawnParticles(ParticleTypes.HEART, this.getX(), this.getBodyY(1.0), this.getZ(), 6, 0.4, 0.4, 0.4, 0.0);
				}
				this.getWorld().playSound(null, this.getBlockPos(), SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.NEUTRAL, 0.8f, 1.8f);
			}
			return ActionResult.success(this.getWorld().isClient);
		}
		return super.interactMob(player, hand);
	}

	@Override
	public void attack(LivingEntity target, float pullProgress) {
		PlasmaBoltEntity bolt = new PlasmaBoltEntity(this.getWorld(), this);
		bolt.setDamage(this.ownerUuid != null ? 16.0f : 10.0f);
		double dx = target.getX() - this.getX();
		double dy = target.getBodyY(0.5) - this.getEyeY();
		double dz = target.getZ() - this.getZ();
		bolt.setVelocity(dx, dy, dz, 2.6f, 0.6f);
		this.getWorld().playSound(null, this.getBlockPos(), SoundEvents.BLOCK_BEACON_DEACTIVATE, SoundCategory.NEUTRAL, 0.6f, 2.0f);
		this.getWorld().spawnEntity(bolt);
	}

	@Override
	public boolean canImmediatelyDespawn(double distanceSquared) {
		return false;
	}

	/** Keeps a companion close to its owner, teleporting to them when left far behind. */
	private static class FollowOwnerGoal extends Goal {
		private final AstroBotEntity bot;
		private final double speed;
		private final float minDistance;
		private final float maxDistance;
		private int recalc;

		FollowOwnerGoal(AstroBotEntity bot, double speed, float minDistance, float maxDistance) {
			this.bot = bot;
			this.speed = speed;
			this.minDistance = minDistance;
			this.maxDistance = maxDistance;
			this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
		}

		@Override
		public boolean canStart() {
			PlayerEntity owner = this.bot.getCompanionOwner();
			if (owner == null || owner.isSpectator() || this.bot.getTarget() != null) {
				return false;
			}
			return this.bot.squaredDistanceTo(owner) > (double) (this.maxDistance * this.maxDistance) * 0.25;
		}

		@Override
		public boolean shouldContinue() {
			PlayerEntity owner = this.bot.getCompanionOwner();
			if (owner == null || this.bot.getTarget() != null) {
				return false;
			}
			return this.bot.squaredDistanceTo(owner) > (double) (this.minDistance * this.minDistance);
		}

		@Override
		public void start() {
			this.recalc = 0;
		}

		@Override
		public void stop() {
			this.bot.getNavigation().stop();
		}

		@Override
		public void tick() {
			PlayerEntity owner = this.bot.getCompanionOwner();
			if (owner == null) {
				return;
			}
			this.bot.getLookControl().lookAt(owner, 10.0f, (float) this.bot.getMaxLookPitchChange());
			if (--this.recalc <= 0) {
				this.recalc = 10;
				if (this.bot.squaredDistanceTo(owner) >= 400.0 || owner.getWorld() != this.bot.getWorld()) {
					double angle = this.bot.getRandom().nextDouble() * Math.PI * 2.0;
					this.bot.refreshPositionAndAngles(owner.getX() + Math.cos(angle) * 2.0, owner.getY(), owner.getZ() + Math.sin(angle) * 2.0, this.bot.getYaw(), this.bot.getPitch());
					this.bot.getNavigation().stop();
				} else {
					this.bot.getNavigation().startMovingTo(owner, this.speed);
				}
			}
		}
	}

	/** Attacks whatever last hurt the owner. */
	private static class DefendOwnerGoal extends TrackTargetGoal {
		private final AstroBotEntity bot;
		private LivingEntity attacker;
		private int lastAttackedTime;

		DefendOwnerGoal(AstroBotEntity bot) {
			super(bot, false);
			this.bot = bot;
			this.setControls(EnumSet.of(Goal.Control.TARGET));
		}

		@Override
		public boolean canStart() {
			PlayerEntity owner = this.bot.getCompanionOwner();
			if (owner == null) {
				return false;
			}
			this.attacker = owner.getAttacker();
			int time = owner.getLastAttackedTime();
			return time != this.lastAttackedTime && this.attacker != null && !(this.attacker instanceof AstroBotEntity)
				&& !(this.attacker instanceof PlayerEntity) && this.canTrack(this.attacker, TargetPredicate.DEFAULT);
		}

		@Override
		public void start() {
			this.mob.setTarget(this.attacker);
			PlayerEntity owner = this.bot.getCompanionOwner();
			if (owner != null) {
				this.lastAttackedTime = owner.getLastAttackedTime();
			}
			super.start();
		}
	}

	/** Attacks whatever the owner attacks. */
	private static class AssistOwnerGoal extends TrackTargetGoal {
		private final AstroBotEntity bot;
		private LivingEntity victim;
		private int lastAttackTime;

		AssistOwnerGoal(AstroBotEntity bot) {
			super(bot, false);
			this.bot = bot;
			this.setControls(EnumSet.of(Goal.Control.TARGET));
		}

		@Override
		public boolean canStart() {
			PlayerEntity owner = this.bot.getCompanionOwner();
			if (owner == null) {
				return false;
			}
			this.victim = owner.getAttacking();
			int time = owner.getLastAttackTime();
			return time != this.lastAttackTime && this.victim != null && !(this.victim instanceof AstroBotEntity)
				&& !(this.victim instanceof PlayerEntity) && this.canTrack(this.victim, TargetPredicate.DEFAULT);
		}

		@Override
		public void start() {
			this.mob.setTarget(this.victim);
			PlayerEntity owner = this.bot.getCompanionOwner();
			if (owner != null) {
				this.lastAttackTime = owner.getLastAttackTime();
			}
			super.start();
		}
	}
}
