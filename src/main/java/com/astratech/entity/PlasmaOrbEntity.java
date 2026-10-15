package com.astratech.entity;

import com.astratech.registry.ModEntities;
import com.astratech.registry.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/** Large charged projectile from the Pulse Blaster: heavy direct hit plus an area explosion (no block damage). */
public class PlasmaOrbEntity extends PlasmaBoltEntity {
	private static final double BLAST_RADIUS = 5.0;
	private float blastDamage = 30.0f;

	public PlasmaOrbEntity(EntityType<? extends PlasmaOrbEntity> type, World world) {
		super(type, world);
	}

	public PlasmaOrbEntity(World world, LivingEntity owner) {
		super(ModEntities.PLASMA_ORB, world, owner);
	}

	public void setBlastDamage(float blastDamage) {
		this.blastDamage = blastDamage;
	}

	@Override
	protected Item getDefaultItem() {
		return ModItems.PLASMA_ORB;
	}

	@Override
	public void tick() {
		super.tick();
		if (this.getWorld() instanceof ServerWorld sw) {
			sw.spawnParticles(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY(), this.getZ(), 3, 0.15, 0.15, 0.15, 0.05);
			sw.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, this.getX(), this.getY(), this.getZ(), 1, 0.1, 0.1, 0.1, 0.0);
		}
	}

	@Override
	protected void onCollision(HitResult hitResult) {
		if (this.getWorld().isClient) {
			return;
		}
		if (this.getWorld() instanceof ServerWorld sw) {
			explode(sw);
		}
		this.discard();
	}

	private void explode(ServerWorld world) {
		Vec3d c = this.getPos();
		world.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, c.x, c.y, c.z, 1, 0.0, 0.0, 0.0, 0.0);
		world.spawnParticles(ParticleTypes.FLASH, c.x, c.y, c.z, 1, 0.0, 0.0, 0.0, 0.0);
		world.spawnParticles(ParticleTypes.END_ROD, c.x, c.y, c.z, 60, 1.5, 1.5, 1.5, 0.25);
		world.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, c.x, c.y, c.z, 40, 1.2, 1.2, 1.2, 0.15);
		world.playSound(null, c.x, c.y, c.z, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 2.0f, 1.2f);
		world.playSound(null, c.x, c.y, c.z, SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.PLAYERS, 1.5f, 1.6f);
		Entity owner = this.getOwner();
		LivingEntity attacker = owner instanceof LivingEntity living ? living : null;
		Box box = new Box(c.x - BLAST_RADIUS, c.y - BLAST_RADIUS, c.z - BLAST_RADIUS, c.x + BLAST_RADIUS, c.y + BLAST_RADIUS, c.z + BLAST_RADIUS);
		for (LivingEntity victim : world.getEntitiesByClass(LivingEntity.class, box, e -> e.isAlive() && e != owner && !(e instanceof AstroBotEntity) && !(e instanceof PlayerEntity p && owner instanceof AstroBotEntity))) {
			double dist = victim.getPos().add(0.0, victim.getHeight() / 2.0, 0.0).distanceTo(c);
			if (dist > BLAST_RADIUS) {
				continue;
			}
			float scale = (float) (1.0 - 0.5 * dist / BLAST_RADIUS);
			victim.damage(this.getDamageSources().mobProjectile(this, attacker), this.blastDamage * scale);
			Vec3d push = victim.getPos().subtract(c).normalize().multiply(0.9).add(0.0, 0.4, 0.0);
			victim.addVelocity(push.x, push.y, push.z);
			victim.velocityModified = true;
		}
	}
}
