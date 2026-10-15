package com.astratech.registry;

import com.astratech.entity.AstroBotEntity;
import com.astratech.entity.LunarHopperEntity;
import com.astratech.entity.LunarSentinelEntity;
import com.astratech.entity.NovaOvermindEntity;
import com.astratech.entity.PlasmaBoltEntity;
import com.astratech.entity.BlackHoleEntity;
import com.astratech.entity.MeteorEntity;
import com.astratech.entity.ThrownNovaCoreEntity;
import com.astratech.entity.PlasmaOrbEntity;
import com.astratech.entity.PlasmaDroneEntity;
import com.astratech.entity.VoidStalkerEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.Heightmap;

public final class ModEntities {
	public static final EntityType<PlasmaBoltEntity> PLASMA_BOLT = Registry.register(Registries.ENTITY_TYPE,
		new Identifier("astratech", "plasma_bolt"),
		FabricEntityTypeBuilder.<PlasmaBoltEntity>create(SpawnGroup.MISC, PlasmaBoltEntity::new)
			.dimensions(EntityDimensions.fixed(0.35f, 0.35f))
			.trackRangeBlocks(64).trackedUpdateRate(2).forceTrackedVelocityUpdates(true)
			.build());

	public static final EntityType<PlasmaOrbEntity> PLASMA_ORB = Registry.register(Registries.ENTITY_TYPE,
		new Identifier("astratech", "plasma_orb"),
		FabricEntityTypeBuilder.<PlasmaOrbEntity>create(SpawnGroup.MISC, PlasmaOrbEntity::new)
			.dimensions(EntityDimensions.fixed(0.9f, 0.9f))
			.trackRangeBlocks(64).trackedUpdateRate(2).forceTrackedVelocityUpdates(true)
			.build());

	public static final EntityType<MeteorEntity> METEOR = Registry.register(Registries.ENTITY_TYPE,
		new Identifier("astratech", "meteor"),
		FabricEntityTypeBuilder.<MeteorEntity>create(SpawnGroup.MISC, MeteorEntity::new)
			.dimensions(EntityDimensions.fixed(1.2f, 1.2f))
			.trackRangeBlocks(128).trackedUpdateRate(2).forceTrackedVelocityUpdates(true)
			.build());

	public static final EntityType<ThrownNovaCoreEntity> THROWN_NOVA_CORE = Registry.register(Registries.ENTITY_TYPE,
		new Identifier("astratech", "thrown_nova_core"),
		FabricEntityTypeBuilder.<ThrownNovaCoreEntity>create(SpawnGroup.MISC, ThrownNovaCoreEntity::new)
			.dimensions(EntityDimensions.fixed(0.4f, 0.4f))
			.trackRangeBlocks(64).trackedUpdateRate(10)
			.build());

	public static final EntityType<BlackHoleEntity> BLACK_HOLE = Registry.register(Registries.ENTITY_TYPE,
		new Identifier("astratech", "black_hole"),
		FabricEntityTypeBuilder.<BlackHoleEntity>create(SpawnGroup.MISC, BlackHoleEntity::new)
			.dimensions(EntityDimensions.fixed(1.0f, 1.0f))
			.fireImmune()
			.trackRangeBlocks(96).trackedUpdateRate(10)
			.build());

	public static final EntityType<LunarSentinelEntity> LUNAR_SENTINEL = Registry.register(Registries.ENTITY_TYPE,
		new Identifier("astratech", "lunar_sentinel"),
		FabricEntityTypeBuilder.createMob().<LunarSentinelEntity>entityFactory(LunarSentinelEntity::new)
			.spawnGroup(SpawnGroup.MONSTER)
			.dimensions(EntityDimensions.fixed(0.75f, 2.4f))
			.spawnRestriction(SpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, HostileEntity::canSpawnInDark)
			.fireImmune().trackRangeChunks(8)
			.build());

	public static final EntityType<VoidStalkerEntity> VOID_STALKER = Registry.register(Registries.ENTITY_TYPE,
		new Identifier("astratech", "void_stalker"),
		FabricEntityTypeBuilder.createMob().<VoidStalkerEntity>entityFactory(VoidStalkerEntity::new)
			.spawnGroup(SpawnGroup.MONSTER)
			.dimensions(EntityDimensions.fixed(1.6f, 1.0f))
			.spawnRestriction(SpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, HostileEntity::canSpawnInDark)
			.fireImmune().trackRangeChunks(8)
			.build());

	public static final EntityType<PlasmaDroneEntity> PLASMA_DRONE = Registry.register(Registries.ENTITY_TYPE,
		new Identifier("astratech", "plasma_drone"),
		FabricEntityTypeBuilder.createMob().<PlasmaDroneEntity>entityFactory(PlasmaDroneEntity::new)
			.spawnGroup(SpawnGroup.MONSTER)
			.dimensions(EntityDimensions.fixed(0.9f, 0.9f))
			.spawnRestriction(SpawnRestriction.Location.NO_RESTRICTIONS, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, HostileEntity::canSpawnInDark)
			.fireImmune().trackRangeChunks(8)
			.build());

	public static final EntityType<AstroBotEntity> ASTRO_BOT = Registry.register(Registries.ENTITY_TYPE,
		new Identifier("astratech", "astro_bot"),
		FabricEntityTypeBuilder.createMob().<AstroBotEntity>entityFactory(AstroBotEntity::new)
			.spawnGroup(SpawnGroup.CREATURE)
			.dimensions(EntityDimensions.fixed(0.7f, 1.4f))
			.spawnRestriction(SpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
				(type, world, reason, pos, random) -> ModBlocks.isMoonSurface(world.getBlockState(pos.down())))
			.trackRangeChunks(8)
			.build());

	public static final EntityType<LunarHopperEntity> LUNAR_HOPPER = Registry.register(Registries.ENTITY_TYPE,
		new Identifier("astratech", "lunar_hopper"),
		FabricEntityTypeBuilder.createMob().<LunarHopperEntity>entityFactory(LunarHopperEntity::new)
			.spawnGroup(SpawnGroup.CREATURE)
			.dimensions(EntityDimensions.fixed(0.7f, 0.7f))
			.spawnRestriction(SpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
				(type, world, reason, pos, random) -> ModBlocks.isMoonSurface(world.getBlockState(pos.down())))
			.trackRangeChunks(8)
			.build());

	public static final EntityType<NovaOvermindEntity> NOVA_OVERMIND = Registry.register(Registries.ENTITY_TYPE,
		new Identifier("astratech", "nova_overmind"),
		FabricEntityTypeBuilder.createMob().<NovaOvermindEntity>entityFactory(NovaOvermindEntity::new)
			.spawnGroup(SpawnGroup.MONSTER)
			.dimensions(EntityDimensions.fixed(4.0f, 5.2f))
			.fireImmune().trackRangeChunks(10)
			.build());

	private ModEntities() {
	}

	public static void registerAttributes() {
		FabricDefaultAttributeRegistry.register(LUNAR_SENTINEL, LunarSentinelEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(VOID_STALKER, VoidStalkerEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(PLASMA_DRONE, PlasmaDroneEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(ASTRO_BOT, AstroBotEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(LUNAR_HOPPER, LunarHopperEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(NOVA_OVERMIND, NovaOvermindEntity.createAttributes());
	}

	public static void init() {
	}
}
