package com.astratech.entity;

import com.astratech.registry.ModEntities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

/**
 * The Nova Overmind: a hovering AI core with three phases.
 * Phase 1: aimed plasma bolts and orbital strikes. Phase 2: bolt fans and drone reinforcements.
 * Phase 3: radial bolt rings, gravity pulses, sentinel reinforcements and faster strikes.
 */
public class NovaOvermindEntity extends HostileEntity {
	private final ServerBossBar bossBar = new ServerBossBar(Text.translatable("entity.astratech.nova_overmind"), BossBar.Color.BLUE, BossBar.Style.NOTCHED_10);
	private int summonTicks = 0;
	private int boltCooldown = 60;
	private int strikeCooldown = 120;
	private int minionCooldown = 300;
	private int pulseCooldown = 140;
	private double orbitAngle = 0.0;
	// Phase 2 melee charge: 0 = stalking, 1 = wind-up, 2 = charging, 3 = recovering
	private int chargeState = 0;
	private int chargeTimer = 60;
	private Vec3d chargeDir = Vec3d.ZERO;
	private boolean chargeHit = false;
	private final List<Vec3d> strikePositions = new ArrayList<>();
	private final List<Integer> strikeTimers = new ArrayList<>();

	public NovaOvermindEntity(EntityType<? extends NovaOvermindEntity> type, World world) {
		super(type, world);
		this.experiencePoints = 600;
		this.setNoGravity(true);
		this.setPersistent();
	}

	public static DefaultAttributeContainer.Builder createAttributes() {
		return HostileEntity.createHostileAttributes()
			.add(EntityAttributes.GENERIC_MAX_HEALTH, 500.0)
			.add(EntityAttributes.GENERIC_ARMOR, 12.0)
			.add(EntityAttributes.GENERIC_ARMOR_TOUGHNESS, 8.0)
			.add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0)
			.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 12.0)
			.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.35)
			.add(EntityAttributes.GENERIC_FLYING_SPEED, 0.6)
			.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 64.0);
	}

	public void beginSummoning() {
		this.summonTicks = 120;
	}

	public boolean isSummoning() {
		return this.summonTicks > 0;
	}

	public int getPhase() {
		float fraction = this.getHealth() / this.getMaxHealth();
		if (fraction > 0.66f) {
			return 1;
		}
		return fraction > 0.33f ? 2 : 3;
	}

	@Override
	protected void initGoals() {
		this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, 0, false, false, null));
	}

	@Override
	public boolean damage(DamageSource source, float amount) {
		if (this.summonTicks > 0 || source.isOf(DamageTypes.IN_WALL) || source.isOf(DamageTypes.DROWN)) {
			return false;
		}
		return super.damage(source, Math.min(amount, 40.0f));
	}

	@Override
	public boolean canHaveStatusEffect(StatusEffectInstance effect) {
		return effect.getEffectType() != StatusEffects.WITHER && super.canHaveStatusEffect(effect);
	}

	@Override
	protected void mobTick() {
		super.mobTick();
		this.bossBar.setPercent(this.getHealth() / this.getMaxHealth());
		int barPhase = this.getPhase();
		this.bossBar.setColor(barPhase == 1 ? BossBar.Color.BLUE : (barPhase == 2 ? BossBar.Color.RED : BossBar.Color.PURPLE));
		if (!(this.getWorld() instanceof ServerWorld world)) {
			return;
		}

		if (this.summonTicks > 0) {
			this.summonSequence(world);
			return;
		}

		int phase = this.getPhase();
		LivingEntity target = this.getTarget();
		if (target == null || !target.isAlive()) {
			this.setVelocity(this.getVelocity().multiply(0.8));
			if (this.age % 20 == 0) {
				this.heal(4.0f);
			}
		} else {
			if (phase == 2) {
				this.meleePhase(world, target);
			} else {
				this.chargeState = 0;
				this.orbit(target, phase);
			}
			this.getLookControl().lookAt(target, 30.0f, 30.0f);
			this.runAttacks(world, target, phase);
		}
		this.tickStrikes(world);
	}

	private void summonSequence(ServerWorld world) {
		this.summonTicks--;
		this.setVelocity(Vec3d.ZERO);
		world.spawnParticles(ParticleTypes.END_ROD, this.getX(), this.getY() + 2.6, this.getZ(), 14, 2.0, 2.0, 2.0, 0.15);
		world.spawnParticles(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY() + 2.6, this.getZ(), 10, 1.5, 1.5, 1.5, 0.4);
		if (this.summonTicks == 119) {
			world.playSound(null, this.getBlockPos(), SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.HOSTILE, 3.0f, 1.6f);
		}
		if (this.summonTicks == 0) {
			world.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY() + 2.6, this.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
			world.playSound(null, this.getBlockPos(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 3.0f, 0.8f);
			for (PlayerEntity player : world.getEntitiesByClass(PlayerEntity.class, this.getBoundingBox().expand(10.0), p -> true)) {
				double dx = player.getX() - this.getX();
				double dz = player.getZ() - this.getZ();
				player.takeKnockback(1.5, -dx, -dz);
			}
		}
	}

	/** Phase 2: the Overmind drops the guns and hurls itself at its target. */
	private void meleePhase(ServerWorld world, LivingEntity target) {
		Vec3d center = this.getPos().add(0.0, 2.6, 0.0);
		switch (this.chargeState) {
			case 0 -> {
				// stalk: hover close above the target
				Vec3d desired = target.getPos().add(0.0, 3.5, 0.0);
				Vec3d diff = desired.subtract(this.getPos());
				Vec3d push = diff.length() > 6.0 ? diff.normalize().multiply(0.55) : Vec3d.ZERO;
				this.setVelocity(this.getVelocity().multiply(0.7).add(push.multiply(0.3)));
				if (--this.chargeTimer <= 0 && this.canSee(target)) {
					this.chargeState = 1;
					this.chargeTimer = 28;
					world.playSound(null, this.getBlockPos(), SoundEvents.ENTITY_WARDEN_ROAR, SoundCategory.HOSTILE, 3.0f, 1.3f);
				}
			}
			case 1 -> {
				// wind-up: freeze, glow and lock on
				this.setVelocity(this.getVelocity().multiply(0.3));
				world.spawnParticles(ParticleTypes.FLAME, center.x, center.y, center.z, 12, 1.8, 1.8, 1.8, 0.05);
				world.spawnParticles(ParticleTypes.ELECTRIC_SPARK, center.x, center.y, center.z, 8, 2.0, 2.0, 2.0, 0.3);
				if (--this.chargeTimer <= 0) {
					Vec3d aim = target.getPos().add(0.0, target.getHeight() * 0.5, 0.0).subtract(center);
					this.chargeDir = aim.lengthSquared() < 0.01 ? new Vec3d(0.0, 0.0, 1.0) : aim.normalize();
					this.chargeState = 2;
					this.chargeTimer = 22;
					this.chargeHit = false;
					world.playSound(null, this.getBlockPos(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 2.0f, 1.6f);
				}
			}
			case 2 -> {
				this.setVelocity(this.chargeDir.multiply(1.6));
				world.spawnParticles(ParticleTypes.FLAME, center.x, center.y, center.z, 20, 1.6, 1.6, 1.6, 0.02);
				world.spawnParticles(ParticleTypes.LARGE_SMOKE, center.x, center.y, center.z, 6, 1.4, 1.4, 1.4, 0.02);
				if (!this.chargeHit) {
					for (PlayerEntity player : world.getEntitiesByClass(PlayerEntity.class, this.getBoundingBox().expand(1.0), p -> p.isAlive() && !p.isSpectator())) {
						player.damage(this.getDamageSources().mobAttack(this), 16.0f);
						player.takeKnockback(2.2, -this.chargeDir.x, -this.chargeDir.z);
						player.velocityModified = true;
						this.chargeHit = true;
						world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 2.0f, 0.9f);
						world.spawnParticles(ParticleTypes.EXPLOSION, player.getX(), player.getBodyY(0.5), player.getZ(), 4, 0.5, 0.5, 0.5, 0.0);
					}
				}
				if (--this.chargeTimer <= 0 || this.horizontalCollision || this.verticalCollision || this.chargeHit) {
					this.chargeState = 3;
					this.chargeTimer = 35;
				}
			}
			default -> {
				this.setVelocity(this.getVelocity().multiply(0.8));
				if (--this.chargeTimer <= 0) {
					this.chargeState = 0;
					this.chargeTimer = 40;
				}
			}
		}
	}

	private void orbit(LivingEntity target, int phase) {
		this.orbitAngle += 0.025 + phase * 0.01;
		double radius = phase == 3 ? 9.0 : 12.0;
		Vec3d desired = target.getPos().add(Math.cos(this.orbitAngle) * radius, 6.0 + Math.sin(this.orbitAngle * 0.5) * 2.0, Math.sin(this.orbitAngle) * radius);
		Vec3d diff = desired.subtract(this.getPos());
		double speed = 0.5 + phase * 0.1;
		Vec3d push = diff.length() > 0.5 ? diff.normalize().multiply(Math.min(speed, diff.length() * 0.12)) : Vec3d.ZERO;
		Vec3d velocity = this.getVelocity().multiply(0.6).add(push.multiply(0.4));
		if (this.horizontalCollision) {
			velocity = velocity.add(0.0, 0.25, 0.0);
		}
		this.setVelocity(velocity);
	}

	private void runAttacks(ServerWorld world, LivingEntity target, int phase) {
		if (phase != 2 && --this.boltCooldown <= 0) {
			this.boltCooldown = phase == 1 ? 30 : (phase == 2 ? 24 : 18);
			if (this.canSee(target)) {
				this.fireBolts(world, target, phase);
			}
		}
		if (--this.strikeCooldown <= 0) {
			this.strikeCooldown = phase == 3 ? 100 : 160;
			this.markOrbitalStrikes(world, phase);
		}
		if (phase >= 2 && --this.minionCooldown <= 0) {
			this.minionCooldown = 320;
			this.spawnMinions(world, phase);
		}
		if (phase == 3 && --this.pulseCooldown <= 0) {
			this.pulseCooldown = 120;
			this.gravityPulse(world);
		}
	}

	private void fireBolts(ServerWorld world, LivingEntity target, int phase) {
		Vec3d aim = new Vec3d(target.getX() - this.getX(), target.getBodyY(0.5) - (this.getY() + 2.6), target.getZ() - this.getZ());
		world.playSound(null, this.getBlockPos(), SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.HOSTILE, 2.0f, 0.6f);
		this.shoot(world, aim, 8.0f);
		if (phase == 3) {
			this.shoot(world, aim.rotateY(0.25f), 8.0f);
			this.shoot(world, aim.rotateY(-0.25f), 8.0f);
		}
		if (phase == 3) {
			for (int i = 0; i < 8; i++) {
				this.shoot(world, new Vec3d(1.0, 0.0, 0.0).rotateY((float) (i * Math.PI / 4.0 + this.orbitAngle)), 7.0f);
			}
		}
	}

	private void shoot(ServerWorld world, Vec3d direction, float damage) {
		PlasmaBoltEntity bolt = new PlasmaBoltEntity(world, this);
		bolt.setPosition(this.getX(), this.getY() + 2.6, this.getZ());
		bolt.setDamage(damage);
		bolt.setVelocity(direction.x, direction.y, direction.z, 1.9f, 0.6f);
		world.spawnEntity(bolt);
	}

	private void markOrbitalStrikes(ServerWorld world, int phase) {
		Box area = this.getBoundingBox().expand(48.0);
		for (PlayerEntity player : world.getEntitiesByClass(PlayerEntity.class, area, p -> p.isAlive() && !p.isSpectator() && !p.isCreative())) {
			Vec3d predicted = player.getPos().add(player.getVelocity().multiply(15.0));
			this.strikePositions.add(new Vec3d(predicted.x, player.getY(), predicted.z));
			this.strikeTimers.add(35);
			if (phase >= 2) {
				for (int i = 0; i < phase; i++) {
					double angle = this.random.nextDouble() * Math.PI * 2.0;
					double dist = 3.0 + this.random.nextDouble() * 5.0;
					this.strikePositions.add(new Vec3d(player.getX() + Math.cos(angle) * dist, player.getY(), player.getZ() + Math.sin(angle) * dist));
					this.strikeTimers.add(45 + i * 10);
				}
			}
		}
	}

	private void tickStrikes(ServerWorld world) {
		for (int i = this.strikePositions.size() - 1; i >= 0; i--) {
			Vec3d pos = this.strikePositions.get(i);
			int timer = this.strikeTimers.get(i) - 1;
			if (timer > 0) {
				this.strikeTimers.set(i, timer);
				double radius = 3.5 * timer / 35.0 + 0.3;
				for (int k = 0; k < 6; k++) {
					double angle = k * Math.PI / 3.0 + timer * 0.3;
					world.spawnParticles(ParticleTypes.ELECTRIC_SPARK, pos.x + Math.cos(angle) * radius, pos.y + 0.2, pos.z + Math.sin(angle) * radius, 1, 0.0, 0.0, 0.0, 0.0);
				}
				world.spawnParticles(ParticleTypes.END_ROD, pos.x, pos.y + 4.0 + timer * 0.1, pos.z, 1, 0.05, 2.0, 0.05, 0.0);
			} else {
				world.spawnParticles(ParticleTypes.EXPLOSION, pos.x, pos.y + 0.5, pos.z, 6, 1.5, 0.3, 1.5, 0.0);
				world.spawnParticles(ParticleTypes.FLASH, pos.x, pos.y + 1.0, pos.z, 1, 0.0, 0.0, 0.0, 0.0);
				world.playSound(null, pos.x, pos.y, pos.z, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 1.5f, 1.4f);
				Box blast = new Box(pos.x - 3.5, pos.y - 1.0, pos.z - 3.5, pos.x + 3.5, pos.y + 3.0, pos.z + 3.5);
				for (LivingEntity victim : world.getEntitiesByClass(LivingEntity.class, blast, e -> e != this && !(e instanceof NovaOvermindEntity))) {
					victim.damage(this.getDamageSources().mobAttack(this), 14.0f);
				}
				this.strikePositions.remove(i);
				this.strikeTimers.remove(i);
			}
		}
	}

	private void spawnMinions(ServerWorld world, int phase) {
		Box area = this.getBoundingBox().expand(40.0);
		int drones = world.getEntitiesByClass(PlasmaDroneEntity.class, area, e -> true).size();
		if (drones < 4) {
			for (int i = 0; i < 2; i++) {
				this.spawnMinion(world, ModEntities.PLASMA_DRONE.create(world));
			}
		}
		if (phase == 3) {
			int sentinels = world.getEntitiesByClass(LunarSentinelEntity.class, area, e -> true).size();
			if (sentinels < 2) {
				this.spawnMinion(world, ModEntities.LUNAR_SENTINEL.create(world));
			}
		}
		world.playSound(null, this.getBlockPos(), SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.HOSTILE, 2.0f, 0.5f);
	}

	private void spawnMinion(ServerWorld world, Entity minion) {
		if (minion == null) {
			return;
		}
		double angle = this.random.nextDouble() * Math.PI * 2.0;
		minion.refreshPositionAndAngles(this.getX() + Math.cos(angle) * 5.0, this.getY() + 0.5, this.getZ() + Math.sin(angle) * 5.0, 0.0f, 0.0f);
		world.spawnEntity(minion);
	}

	private void gravityPulse(ServerWorld world) {
		world.spawnParticles(ParticleTypes.SONIC_BOOM, this.getX(), this.getY() + 2.6, this.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
		world.playSound(null, this.getBlockPos(), SoundEvents.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.HOSTILE, 2.0f, 0.8f);
		for (PlayerEntity player : world.getEntitiesByClass(PlayerEntity.class, this.getBoundingBox().expand(9.0), p -> p.isAlive())) {
			double dx = player.getX() - this.getX();
			double dz = player.getZ() - this.getZ();
			player.takeKnockback(1.8, -dx, -dz);
			player.damage(this.getDamageSources().mobAttack(this), 6.0f);
			player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 1));
		}
	}

	private int deathTicks = 0;

	/** Long death sequence: chained explosions while the machine powers down, then a final blast. */
	@Override
	protected void updatePostDeath() {
		if (this.deathTicks < 100) {
			this.deathTicks++;
			this.setVelocity(Vec3d.ZERO);
			if (this.getWorld() instanceof ServerWorld world) {
				if (this.deathTicks == 1) {
					world.playSound(null, this.getBlockPos(), SoundEvents.ENTITY_WARDEN_DEATH, SoundCategory.HOSTILE, 3.0f, 0.5f);
				}
				if (this.deathTicks % 4 == 0) {
					double ox = (this.random.nextDouble() - 0.5) * 5.0;
					double oy = (this.random.nextDouble() - 0.5) * 4.0;
					double oz = (this.random.nextDouble() - 0.5) * 5.0;
					double px = this.getX() + ox;
					double py = this.getY() + 2.6 + oy;
					double pz = this.getZ() + oz;
					world.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, px, py, pz, 1, 0.0, 0.0, 0.0, 0.0);
					world.spawnParticles(ParticleTypes.FLAME, px, py, pz, 20, 0.6, 0.6, 0.6, 0.08);
					world.playSound(null, px, py, pz, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 2.5f, 0.7f + this.random.nextFloat() * 0.5f);
				}
				world.spawnParticles(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY() + 2.6, this.getZ(), 10, 2.0, 2.0, 2.0, 0.3);
				world.spawnParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 2.6, this.getZ(), 4, 1.8, 1.8, 1.8, 0.02);
				if (this.deathTicks % 12 == 0) {
					// power-down whine: falling pitch
					float pitch = 1.5f - this.deathTicks * 0.012f;
					world.playSound(null, this.getBlockPos(), SoundEvents.BLOCK_BEACON_DEACTIVATE, SoundCategory.HOSTILE, 3.0f, Math.max(0.3f, pitch));
					world.playSound(null, this.getBlockPos(), SoundEvents.BLOCK_CONDUIT_DEACTIVATE, SoundCategory.HOSTILE, 3.0f, Math.max(0.3f, pitch - 0.2f));
				}
				if (this.deathTicks == 100) {
					world.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY() + 2.6, this.getZ(), 6, 2.0, 2.0, 2.0, 0.0);
					world.spawnParticles(ParticleTypes.FLASH, this.getX(), this.getY() + 2.6, this.getZ(), 3, 1.0, 1.0, 1.0, 0.0);
					world.playSound(null, this.getBlockPos(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 5.0f, 0.5f);
					world.playSound(null, this.getBlockPos(), SoundEvents.BLOCK_BEACON_DEACTIVATE, SoundCategory.HOSTILE, 5.0f, 0.3f);
					this.deathTime = 19;
				}
			}
			return;
		}
		super.updatePostDeath();
	}

	@Override
	public void onStartedTrackingBy(ServerPlayerEntity player) {
		super.onStartedTrackingBy(player);
		this.bossBar.addPlayer(player);
	}

	@Override
	public void onStoppedTrackingBy(ServerPlayerEntity player) {
		super.onStoppedTrackingBy(player);
		this.bossBar.removePlayer(player);
	}

	@Override
	public void remove(Entity.RemovalReason reason) {
		super.remove(reason);
		this.bossBar.clearPlayers();
	}

	@Override
	public void writeCustomDataToNbt(NbtCompound nbt) {
		super.writeCustomDataToNbt(nbt);
		nbt.putInt("SummonTicks", this.summonTicks);
	}

	@Override
	public void readCustomDataFromNbt(NbtCompound nbt) {
		super.readCustomDataFromNbt(nbt);
		this.summonTicks = nbt.getInt("SummonTicks");
	}
}
