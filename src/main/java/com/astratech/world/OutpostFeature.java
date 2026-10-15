package com.astratech.world;

import com.astratech.registry.ModBlocks;
import com.astratech.registry.ModEntities;
import com.mojang.serialization.Codec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

/** An abandoned glass-domed research outpost with an antenna mast, a loot chest and sometimes a guard. */
public class OutpostFeature extends Feature<DefaultFeatureConfig> {
	private static final int RADIUS = 6;

	public OutpostFeature(Codec<DefaultFeatureConfig> codec) {
		super(codec);
	}

	@Override
	public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
		StructureWorldAccess world = context.getWorld();
		BlockPos origin = context.getOrigin();
		Random random = context.getRandom();
		int cx = origin.getX();
		int cz = origin.getZ();
		int floorY = world.getTopY(Heightmap.Type.WORLD_SURFACE_WG, cx, cz) - 1;

		BlockState plating = ModBlocks.TECH_PLATING.getDefaultState();
		BlockState lamp = ModBlocks.PLASMA_LAMP.getDefaultState();
		BlockState glass = Blocks.CYAN_STAINED_GLASS.getDefaultState();
		BlockState air = Blocks.AIR.getDefaultState();

		for (int dx = -RADIUS; dx <= RADIUS; dx++) {
			for (int dz = -RADIUS; dz <= RADIUS; dz++) {
				double flat = Math.sqrt(dx * dx + dz * dz);
				if (flat > RADIUS + 0.5) {
					continue;
				}
				int x = cx + dx;
				int z = cz + dz;
				// foundation
				for (int y = floorY - 1; y >= floorY - 4; y--) {
					world.setBlockState(new BlockPos(x, y, z), plating, Block.NOTIFY_LISTENERS);
				}
				world.setBlockState(new BlockPos(x, floorY, z), plating, Block.NOTIFY_LISTENERS);
				// dome shell and clearing
				for (int dy = 1; dy <= RADIUS; dy++) {
					double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
					BlockPos pos = new BlockPos(x, floorY + dy, z);
					if (dist < RADIUS - 0.5) {
						world.setBlockState(pos, air, Block.NOTIFY_LISTENERS);
					} else if (dist < RADIUS + 0.5) {
						boolean doorway = dx > 0 && Math.abs(dz) <= 1 && dy <= 2;
						world.setBlockState(pos, doorway ? air : (dy == 1 ? plating : glass), Block.NOTIFY_LISTENERS);
					}
				}
			}
		}

		// Interior lighting and a central pedestal with a loot chest.
		int[][] lamps = {{3, 0}, {-3, 0}, {0, 3}, {0, -3}};
		for (int[] l : lamps) {
			world.setBlockState(new BlockPos(cx + l[0], floorY, cz + l[1]), lamp, Block.NOTIFY_LISTENERS);
		}
		world.setBlockState(new BlockPos(cx, floorY, cz), ModBlocks.LUNAR_ALLOY_BLOCK.getDefaultState(), Block.NOTIFY_LISTENERS);
		BlockPos chestPos = new BlockPos(cx, floorY + 1, cz);
		world.setBlockState(chestPos, Blocks.CHEST.getDefaultState(), Block.NOTIFY_LISTENERS);
		LootableContainerBlockEntity.setLootTable(world, random, chestPos, new Identifier("astratech", "chests/outpost"));

		// Antenna mast outside the dome.
		int mx = cx - RADIUS - 3;
		for (int dy = 0; dy <= 9; dy++) {
			world.setBlockState(new BlockPos(mx, floorY + dy, cz), plating, Block.NOTIFY_LISTENERS);
		}
		world.setBlockState(new BlockPos(mx, floorY + 10, cz), lamp, Block.NOTIFY_LISTENERS);
		for (int arm = -2; arm <= 2; arm++) {
			if (arm != 0) {
				world.setBlockState(new BlockPos(mx, floorY + 7, cz + arm), plating, Block.NOTIFY_LISTENERS);
			}
		}

		// Occasional sentinel guard.
		if (random.nextFloat() < 0.5f) {
			MobEntity guard = ModEntities.LUNAR_SENTINEL.create(world.toServerWorld());
			if (guard != null) {
				guard.refreshPositionAndAngles(cx + 0.5, floorY + 1, cz + 2.5, random.nextFloat() * 360.0f, 0.0f);
				guard.setPersistent();
				world.spawnEntityAndPassengers(guard);
			}
		}
		return true;
	}
}
