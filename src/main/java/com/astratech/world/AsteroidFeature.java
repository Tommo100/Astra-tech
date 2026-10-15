package com.astratech.world;

import com.astratech.registry.ModBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;

/** A floating asteroid high above the Moon: a lumpy rock with ore, a crust of regolith and sometimes a loot chest. */
public class AsteroidFeature extends BaseStructureFeature {
	public AsteroidFeature(Codec<DefaultFeatureConfig> codec) {
		super(codec);
	}

	@Override
	protected boolean build(Builder b) {
		int top = b.world.getTopY();
		int cy = 160 + b.random.nextInt(Math.max(1, Math.min(70, top - 160 - 20)));
		BlockState stone = ModBlocks.LUNAR_STONE.getDefaultState();
		BlockState regolith = ModBlocks.LUNAR_REGOLITH.getDefaultState();
		BlockState gravel = ModBlocks.LUNAR_GRAVEL.getDefaultState();
		BlockState basalt = ModBlocks.MARE_BASALT.getDefaultState();
		BlockState astriumOre = ModBlocks.ASTRIUM_ORE.getDefaultState();
		BlockState crystalOre = ModBlocks.PLASMA_CRYSTAL_ORE.getDefaultState();
		BlockState lamp = ModBlocks.PLASMA_LAMP.getDefaultState();
		BlockState plating = ModBlocks.TECH_PLATING.getDefaultState();
		int size = 1 + b.random.nextInt(3); // 1 small, 2 medium, 3 large

		int lumps = 2 + size;
		int[] lx = new int[lumps];
		int[] ly = new int[lumps];
		int[] lz = new int[lumps];
		double[] lr = new double[lumps];
		for (int i = 0; i < lumps; i++) {
			int spread = 2 + size * 2;
			lx[i] = b.cx + (i == 0 ? 0 : b.random.nextInt(spread * 2 + 1) - spread);
			lz[i] = b.cz + (i == 0 ? 0 : b.random.nextInt(spread * 2 + 1) - spread);
			ly[i] = cy + (i == 0 ? 0 : b.random.nextInt(5) - 2);
			lr[i] = i == 0 ? 3.0 + size * 1.7 : 2.0 + size * 0.9 + b.random.nextDouble() * 1.5;
		}
		int reach = 6 + size * 3;
		int minX = b.cx - reach, maxX = b.cx + reach;
		java.util.List<int[]> solids = new java.util.ArrayList<>();
		for (int x = minX; x <= maxX; x++) {
			for (int z = b.cz - reach; z <= b.cz + reach; z++) {
				for (int y = cy - reach; y <= cy + reach; y++) {
					if (!b.inRange(x, z)) {
						continue;
					}
					boolean inside = false;
					for (int i = 0; i < lumps && !inside; i++) {
						double dx = x - lx[i];
						// flatten vertically so asteroids read as rocks, not balls
						double dy = (y - ly[i]) * 1.25;
						double dz = z - lz[i];
						double d2 = dx * dx + dy * dy + dz * dz;
						double r = lr[i] * (0.88 + 0.24 * ((x * 31 + y * 17 + z * 13) % 7) / 7.0);
						inside = d2 <= r * r;
					}
					if (inside) {
						solids.add(new int[]{x, y, z});
					}
				}
			}
		}
		if (solids.size() < 20) {
			return false;
		}
		for (int[] p : solids) {
			double roll = b.random.nextDouble();
			BlockState state = stone;
			if (roll < 0.035) {
				state = astriumOre;
			} else if (roll < 0.05) {
				state = crystalOre;
			}
			b.set(p[0], p[1], p[2], state);
		}
		// crust: swap exposed upper faces for surface blocks
		for (int[] p : solids) {
			if (b.get(p[0], p[1] + 1, p[2]).isAir() && b.get(p[0], p[1], p[2]).isOf(ModBlocks.LUNAR_STONE)) {
				int r = b.random.nextInt(10);
				b.set(p[0], p[1], p[2], r < 6 ? regolith : (r < 8 ? gravel : basalt));
			}
		}
		// loot cache on the highest block near the middle
		if (b.random.nextInt(100) < 65) {
			int bestY = Integer.MIN_VALUE;
			int bx = b.cx, bz = b.cz;
			for (int[] p : solids) {
				if (Math.abs(p[0] - b.cx) <= 3 && Math.abs(p[2] - b.cz) <= 3 && b.get(p[0], p[1] + 1, p[2]).isAir() && p[1] > bestY) {
					bestY = p[1];
					bx = p[0];
					bz = p[2];
				}
			}
			if (bestY != Integer.MIN_VALUE) {
				b.set(bx, bestY, bz, plating);
				b.chest(bx, bestY + 1, bz, "chests/asteroid");
				b.set(bx + 1, bestY + 1, bz, lamp);
				b.set(bx - 1, bestY + 1, bz, lamp);
				b.set(bx + 1, bestY, bz, plating);
				b.set(bx - 1, bestY, bz, plating);
			}
		}
		return true;
	}
}
