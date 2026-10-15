package com.astratech.entity;

import com.astratech.registry.ModEntities;
import com.astratech.registry.ModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

/** A falling meteor called down by the Meteor Hammer. Explodes on impact. */
public class MeteorEntity extends PlasmaOrbEntity {
	public MeteorEntity(EntityType<? extends MeteorEntity> type, World world) {
		super(type, world);
		this.setBlastRadius(5.5);
	}

	public MeteorEntity(World world, LivingEntity owner) {
		super(ModEntities.METEOR, world, owner);
		this.setBlastRadius(5.5);
	}

	@Override
	protected Item getDefaultItem() {
		return ModItems.METEOR;
	}

	@Override
	public void tick() {
		super.tick();
		if (this.getWorld() instanceof ServerWorld sw) {
			sw.spawnParticles(ParticleTypes.FLAME, this.getX(), this.getY(), this.getZ(), 6, 0.4, 0.4, 0.4, 0.02);
			sw.spawnParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY(), this.getZ(), 3, 0.3, 0.3, 0.3, 0.01);
			if (this.age > 200) {
				this.discard();
			}
		}
	}
}
