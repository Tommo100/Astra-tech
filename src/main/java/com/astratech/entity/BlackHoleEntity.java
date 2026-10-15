package com.astratech.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.UUID;

/** A short-lived singularity: drags every nearby mob inward and crushes them. Players are never affected. */
public class BlackHoleEntity extends Entity {
	private static final int LIFETIME = 140;
	private static final double RADIUS = 12.0;
	private UUID ownerUuid;

	public BlackHoleEntity(EntityType<? extends BlackHoleEntity> type, World world) {
		super(type, world);
		this.noClip = true;
		this.setNoGravity(true);
	}

	public void setOwnerUuid(UUID uuid) {
		this.ownerUuid = uuid;
	}

	@Override
	protected void initDataTracker() {
	}

	@Override
	public void tick() {
		super.tick();
		if (!(this.getWorld() instanceof ServerWorld world)) {
			return;
		}
		Vec3d c = this.getPos();
		if (this.age == 1) {
			world.playSound(null, c.x, c.y, c.z, SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS, 1.5f, 0.5f);
		}
		if (this.age % 40 == 0 && this.age < LIFETIME - 20) {
			world.playSound(null, c.x, c.y, c.z, SoundEvents.BLOCK_BEACON_AMBIENT, SoundCategory.PLAYERS, 0.8f, 0.5f);
		}
		// visuals: dark core, inward-spiralling dust
		world.spawnParticles(ParticleTypes.SQUID_INK, c.x, c.y, c.z, 6, 0.3, 0.3, 0.3, 0.0);
		world.spawnParticles(ParticleTypes.REVERSE_PORTAL, c.x, c.y, c.z, 10, 3.0, 3.0, 3.0, 0.3);
		for (int i = 0; i < 6; i++) {
			double a = this.age * 0.4 + i * Math.PI / 3.0;
			double r = 2.5 + (i % 3);
			world.spawnParticles(ParticleTypes.SMOKE, c.x + Math.cos(a) * r, c.y + ((i % 2) - 0.5) * 0.6, c.z + Math.sin(a) * r, 0, -Math.cos(a) * 0.15, 0.0, -Math.sin(a) * 0.15, 1.0);
		}

		LivingEntity owner = this.ownerUuid != null && world.getEntity(this.ownerUuid) instanceof LivingEntity l ? l : null;
		DamageSource source = owner != null ? this.getDamageSources().indirectMagic(this, owner) : this.getDamageSources().magic();
		Box box = new Box(c.x - RADIUS, c.y - RADIUS, c.z - RADIUS, c.x + RADIUS, c.y + RADIUS, c.z + RADIUS);
		for (LivingEntity victim : world.getEntitiesByClass(LivingEntity.class, box, e -> e.isAlive() && affects(e))) {
			Vec3d to = c.subtract(victim.getPos().add(0.0, victim.getHeight() / 2.0, 0.0));
			double dist = to.length();
			if (dist > RADIUS) {
				continue;
			}
			double strength = 0.12 + 0.25 * (1.0 - dist / RADIUS);
			Vec3d pull = dist < 0.5 ? Vec3d.ZERO : to.normalize().multiply(strength);
			Vec3d vel = victim.getVelocity().multiply(0.85).add(pull);
			victim.setVelocity(vel);
			victim.velocityModified = true;
			victim.fallDistance = 0.0f;
			if (this.age % 10 == 0 && dist < 5.0) {
				victim.damage(source, 5.0f);
			}
		}
		if (this.age >= LIFETIME) {
			world.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, c.x, c.y, c.z, 1, 0.0, 0.0, 0.0, 0.0);
			world.spawnParticles(ParticleTypes.REVERSE_PORTAL, c.x, c.y, c.z, 60, 1.0, 1.0, 1.0, 0.6);
			world.playSound(null, c.x, c.y, c.z, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 2.0f, 0.6f);
			for (LivingEntity victim : world.getEntitiesByClass(LivingEntity.class, box.contract(RADIUS - 4.0), e -> e.isAlive() && affects(e))) {
				victim.damage(source, 20.0f);
			}
			this.discard();
		}
	}

	private static boolean affects(LivingEntity e) {
		if (e instanceof PlayerEntity || e instanceof NovaOvermindEntity || e instanceof AstroBotEntity) {
			return false;
		}
		return e instanceof MobEntity;
	}

	@Override
	protected void readCustomDataFromNbt(NbtCompound nbt) {
		if (nbt.containsUuid("Owner")) {
			this.ownerUuid = nbt.getUuid("Owner");
		}
		this.age = nbt.getInt("Age");
	}

	@Override
	protected void writeCustomDataToNbt(NbtCompound nbt) {
		if (this.ownerUuid != null) {
			nbt.putUuid("Owner", this.ownerUuid);
		}
		nbt.putInt("Age", this.age);
	}

	@Override
	public Packet<ClientPlayPacketListener> createSpawnPacket() {
		return new EntitySpawnS2CPacket(this);
	}
}
