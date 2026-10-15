package com.astratech.world;

import com.astratech.registry.ModBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

/** Carves a bowl-shaped impact crater with a raised rim and a plasma crystal deposit on the floor. */
public class CraterFeature extends Feature<DefaultFeatureConfig> {
	public CraterFeature(Codec<DefaultFeatureConfig> codec) {
		super(codec);
	}

	@Override
	public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
		StructureWorldAccess world = context.getWorld();
		BlockPos origin = context.getOrigin();
		Random random = context.getRandom();

		int radius = 5 + random.nextInt(8);
		int depth = 2 + radius / 3;
		int cx = origin.getX();
		int cz = origin.getZ();
		int centerY = world.getTopY(Heightmap.Type.WORLD_SURFACE_WG, cx, cz) - 1;

		BlockState air = Blocks.AIR.getDefaultState();
		BlockState regolith = ModBlocks.LUNAR_REGOLITH.getDefaultState();
		int reach = (int) Math.ceil(radius * 1.25);

		for (int dx = -reach; dx <= reach; dx++) {
			for (int dz = -reach; dz <= reach; dz++) {
				double d = Math.sqrt(dx * dx + dz * dz) / radius;
				if (d > 1.25) {
					continue;
				}
				int x = cx + dx;
				int z = cz + dz;
				int surface = world.getTopY(Heightmap.Type.WORLD_SURFACE_WG, x, z) - 1;
				if (d <= 0.85) {
					double t = d / 0.85;
					int floor = centerY - (int) Math.round(depth * (1.0 - t * t));
					if (floor < surface) {
						for (int y = floor + 1; y <= surface; y++) {
							world.setBlockState(new BlockPos(x, y, z), air, Block.NOTIFY_LISTENERS);
						}
						world.setBlockState(new BlockPos(x, floor, z), regolith, Block.NOTIFY_LISTENERS);
					}
				} else {
					int height = (int) Math.round(2.0 * (1.0 - Math.abs(d - 1.02) / 0.23));
					for (int h = 1; h <= height; h++) {
						world.setBlockState(new BlockPos(x, surface + h, z), regolith, Block.NOTIFY_LISTENERS);
					}
				}
			}
		}

		// Crystal deposit at the crater floor.
		if (random.nextFloat() < 0.7f) {
			BlockState crystal = ModBlocks.PLASMA_CRYSTAL_ORE.getDefaultState();
			int floorY = centerY - depth;
			for (int i = 0; i < 4; i++) {
				int ox = random.nextInt(3) - 1;
				int oz = random.nextInt(3) - 1;
				world.setBlockState(new BlockPos(cx + ox, floorY, cz + oz), crystal, Block.NOTIFY_LISTENERS);
			}
		}
		return true;
	}
}
