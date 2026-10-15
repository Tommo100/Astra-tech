package com.astratech.entity;

import com.astratech.registry.ModEntities;
import com.astratech.registry.ModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

/** A hurled Nova Core. On impact it collapses into a black hole. */
public class ThrownNovaCoreEntity extends ThrownItemEntity {
	public ThrownNovaCoreEntity(EntityType<? extends ThrownNovaCoreEntity> type, World world) {
		super(type, world);
	}

	public ThrownNovaCoreEntity(World world, LivingEntity owner) {
		super(ModEntities.THROWN_NOVA_CORE, owner, world);
	}

	@Override
	protected Item getDefaultItem() {
		return ModItems.NOVA_CORE;
	}

	@Override
	public void tick() {
		super.tick();
		if (this.getWorld() instanceof ServerWorld sw) {
			sw.spawnParticles(ParticleTypes.REVERSE_PORTAL, this.getX(), this.getY(), this.getZ(), 4, 0.15, 0.15, 0.15, 0.02);
			sw.spawnParticles(ParticleTypes.END_ROD, this.getX(), this.getY(), this.getZ(), 1, 0.05, 0.05, 0.05, 0.0);
			if (this.age > 200) {
				this.discard();
			}
		}
	}

	@Override
	protected void onCollision(HitResult hitResult) {
		super.onCollision(hitResult);
		if (this.getWorld() instanceof ServerWorld sw) {
			BlackHoleEntity hole = new BlackHoleEntity(ModEntities.BLACK_HOLE, sw);
			hole.setOwnerUuid(this.getOwner() != null ? this.getOwner().getUuid() : null);
			hole.refreshPositionAndAngles(this.getX(), this.getY() + 0.5, this.getZ(), 0.0f, 0.0f);
			sw.spawnEntity(hole);
			this.discard();
		}
	}
}
