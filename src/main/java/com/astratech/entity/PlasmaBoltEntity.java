package com.astratech.entity;

import com.astratech.registry.ModEntities;
import com.astratech.registry.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;

/** Fast, gravity-free energy projectile used by the Pulse Blaster, drones, sentinels and the boss. */
public class PlasmaBoltEntity extends ThrownItemEntity {
	private float damage = 10.0f;

	public PlasmaBoltEntity(EntityType<? extends PlasmaBoltEntity> type, World world) {
		super(type, world);
	}

	public PlasmaBoltEntity(World world, LivingEntity owner) {
		super(ModEntities.PLASMA_BOLT, owner, world);
	}

	protected PlasmaBoltEntity(EntityType<? extends PlasmaBoltEntity> type, World world, LivingEntity owner) {
		super(type, owner, world);
	}

	public void setDamage(float damage) {
		this.damage = damage;
	}

	@Override
	protected Item getDefaultItem() {
		return ModItems.PLASMA_BOLT;
	}

	@Override
	protected float getGravity() {
		return 0.0f;
	}

	@Override
	public void tick() {
		super.tick();
		if (this.getWorld().isClient) {
			this.getWorld().addParticle(ParticleTypes.END_ROD, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
		} else if (this.age > 100) {
			this.discard();
		}
	}

	@Override
	protected boolean canHit(Entity entity) {
		Entity owner = this.getOwner();
		if ((owner instanceof PlayerEntity || owner instanceof AstroBotEntity) && entity instanceof AstroBotEntity) {
			return false;
		}
		if (owner instanceof AstroBotEntity bot && bot.getOwnerUuid() != null && entity instanceof PlayerEntity) {
			return false;
		}
		return super.canHit(entity);
	}

	@Override
	protected void onEntityHit(EntityHitResult hit) {
		super.onEntityHit(hit);
		if (this.getWorld().isClient) {
			return;
		}
		Entity target = hit.getEntity();
		Entity owner = this.getOwner();
		LivingEntity attacker = owner instanceof LivingEntity living ? living : null;
		target.damage(this.getDamageSources().mobProjectile(this, attacker), this.damage);
		if (this.getWorld() instanceof ServerWorld serverWorld) {
			serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY(), this.getZ(), 12, 0.2, 0.2, 0.2, 0.3);
		}
	}

	@Override
	protected void onBlockHit(BlockHitResult hit) {
		super.onBlockHit(hit);
		if (!this.getWorld().isClient) {
			if (this.getWorld() instanceof ServerWorld serverWorld) {
				serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY(), this.getZ(), 8, 0.1, 0.1, 0.1, 0.2);
			}
			this.discard();
		}
	}

	@Override
	protected void onCollision(net.minecraft.util.hit.HitResult hitResult) {
		super.onCollision(hitResult);
		if (!this.getWorld().isClient && hitResult.getType() == net.minecraft.util.hit.HitResult.Type.ENTITY) {
			this.discard();
		}
	}
}
