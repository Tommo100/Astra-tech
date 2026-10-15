package com.astratech.world;

import com.astratech.registry.ModBlocks;
import com.astratech.registry.ModEntities;
import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;

/** Helios Station: a large glass dome with a reactor tower, four satellite pods and connecting corridors. */
public class StationFeature extends BaseStructureFeature {
	private static final int R = 9;

	public StationFeature(Codec<DefaultFeatureConfig> codec) {
		super(codec);
	}

	@Override
	protected boolean build(Builder b) {
		int base = b.maxSurface(12);
		if (base < 6 || base > 100) {
			return false;
		}
		BlockState plating = ModBlocks.TECH_PLATING.getDefaultState();
		BlockState lamp = ModBlocks.PLASMA_LAMP.getDefaultState();
		BlockState astrium = ModBlocks.ASTRIUM_BLOCK.getDefaultState();
		BlockState stone = ModBlocks.LUNAR_STONE.getDefaultState();
		BlockState glass = Blocks.CYAN_STAINED_GLASS.getDefaultState();
		BlockState air = Blocks.AIR.getDefaultState();
		int cx = b.cx;
		int cz = b.cz;

		// Landing pad
		for (int dx = -14; dx <= 14; dx++) {
			for (int dz = -14; dz <= 14; dz++) {
				double d = Math.sqrt(dx * dx + dz * dz);
				if (d > 13.5) {
					continue;
				}
				int x = cx + dx;
				int z = cz + dz;
				b.foundation(x, z, base - 1, stone);
				BlockState top = plating;
				if (d >= 12.0 || (d >= 6.0 && d < 6.9)) {
					top = astrium;
				} else if (d >= 3.0 && d < 3.9 && ((dx + dz) & 1) == 0) {
					top = lamp;
				}
				b.set(x, base, z, top);
				b.clearColumn(x, z, base + 1, base + 19);
			}
		}

		// Central dome
		for (int dx = -R; dx <= R; dx++) {
			for (int dz = -R; dz <= R; dz++) {
				for (int dy = 1; dy <= R; dy++) {
					double d3 = Math.sqrt(dx * dx + dy * dy + dz * dz);
					if (d3 < R - 0.5 || d3 >= R + 0.5) {
						continue;
					}
					boolean door = dy <= 3 && ((Math.abs(dz) <= 1 && Math.abs(dx) >= R - 2) || (Math.abs(dx) <= 1 && Math.abs(dz) >= R - 2));
					if (door) {
						continue;
					}
					boolean rib = dx == 0 || dz == 0 || Math.abs(dx) == Math.abs(dz);
					b.set(cx + dx, base + dy, cz + dz, (dy == 1 || rib) ? plating : glass);
				}
			}
		}

		// Satellite pods and corridors in the four cardinal directions
		int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
		for (int i = 0; i < dirs.length; i++) {
			int ux = dirs[i][0];
			int uz = dirs[i][1];
			int px = cx + ux * 11;
			int pz = cz + uz * 11;
			for (int dx = -3; dx <= 3; dx++) {
				for (int dz = -3; dz <= 3; dz++) {
					for (int dy = 1; dy <= 3; dy++) {
						double d3 = Math.sqrt(dx * dx + dy * dy + dz * dz);
						if (d3 >= 2.5 && d3 < 3.5) {
							b.set(px + dx, base + dy, pz + dz, dy == 1 ? plating : glass);
						}
					}
				}
			}
			b.set(px, base + 3, pz, lamp);
			// corridor
			for (int t = R - 1; t <= 10; t++) {
				for (int p = -2; p <= 2; p++) {
					int x = cx + ux * t - uz * p;
					int z = cz + uz * t + ux * p;
					for (int dy = 1; dy <= 4; dy++) {
						boolean wall = Math.abs(p) == 2 || dy == 4;
						if (wall) {
							boolean lampSpot = dy == 4 && p == 0 && t % 3 == 0;
							b.set(x, base + dy, z, lampSpot ? lamp : plating);
						} else {
							b.set(x, base + dy, z, air);
						}
					}
				}
			}
			// pod interiors
			b.set(px, base, pz, lamp);
			if (i == 0 || i == 1 || i == 3) {
				b.chest(px, base + 1, pz, "chests/station");
			} else {
				b.set(px, base + 1, pz, astrium);
				b.set(px, base + 2, pz, lamp);
			}
		}

		// Reactor tower
		for (int dy = 1; dy <= 7; dy++) {
			for (int dx = -1; dx <= 1; dx++) {
				for (int dz = -1; dz <= 1; dz++) {
					boolean center = dx == 0 && dz == 0;
					b.set(cx + dx, base + dy, cz + dz, center ? (dy % 2 == 1 ? lamp : plating) : astrium);
				}
			}
		}
		b.set(cx, base + 8, cz, lamp);

		// Consoles and the main loot
		int[][] consoles = {{4, 0}, {-4, 0}, {0, 4}, {0, -4}, {3, 3}, {-3, -3}, {3, -3}, {-3, 3}};
		for (int[] c : consoles) {
			b.set(cx + c[0], base + 1, cz + c[1], plating);
			b.set(cx + c[0], base + 2, cz + c[1], lamp);
		}
		b.chest(cx + 3, base + 1, cz - 6, "chests/station");
		b.chest(cx - 3, base + 1, cz + 6, "chests/station");

		// Antenna mast on the dome
		for (int dy = R; dy <= R + 8; dy++) {
			b.set(cx, base + dy, cz, plating);
		}
		b.set(cx, base + R + 9, cz, lamp);
		for (int arm = -2; arm <= 2; arm++) {
			if (arm != 0) {
				b.set(cx + arm, base + R + 5, cz, plating);
				b.set(cx, base + R + 5, cz + arm, plating);
			}
		}

		// Lamp posts at the pad corners
		int[][] posts = {{9, 9}, {-9, 9}, {9, -9}, {-9, -9}};
		for (int[] p : posts) {
			for (int dy = 1; dy <= 4; dy++) {
				b.set(cx + p[0], base + dy, cz + p[1], plating);
			}
			b.set(cx + p[0], base + 5, cz + p[1], lamp);
		}

		// Guards
		b.spawn(ModEntities.LUNAR_SENTINEL, cx + 5.5, base + 1, cz + 3.5);
		b.spawn(ModEntities.LUNAR_SENTINEL, cx - 5.5, base + 1, cz - 3.5);
		if (b.random.nextFloat() < 0.6f) {
			b.spawn(ModEntities.LUNAR_SENTINEL, cx + 0.5, base + 1, cz + 7.5);
		}
		if (b.random.nextFloat() < 0.4f) {
			b.spawn(ModEntities.PLASMA_DRONE, cx + 0.5, base + 6, cz + 0.5);
		}
		if (b.random.nextFloat() < 0.3f) {
			b.spawn(ModEntities.ASTRO_BOT, cx - 11 + 0.5, base + 1, cz + 0.5);
		}
		return true;
	}
}
