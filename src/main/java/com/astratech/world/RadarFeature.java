package com.astratech.world;

import com.astratech.registry.ModBlocks;
import com.astratech.registry.ModEntities;
import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;

/** Deep-space radar array: a bunker topped by a huge parabolic dish on a pedestal, with antenna masts around it. */
public class RadarFeature extends BaseStructureFeature {
	public RadarFeature(Codec<DefaultFeatureConfig> codec) {
		super(codec);
	}

	@Override
	protected boolean build(Builder b) {
		int base = b.maxSurface(9);
		if (base < 6 || base > 96) {
			return false;
		}
		BlockState plating = ModBlocks.TECH_PLATING.getDefaultState();
		BlockState lamp = ModBlocks.PLASMA_LAMP.getDefaultState();
		BlockState astrium = ModBlocks.ASTRIUM_BLOCK.getDefaultState();
		BlockState stone = ModBlocks.LUNAR_STONE.getDefaultState();
		BlockState white = Blocks.WHITE_CONCRETE.getDefaultState();
		BlockState air = Blocks.AIR.getDefaultState();
		int cx = b.cx;
		int cz = b.cz;

		// Pad
		for (int dx = -10; dx <= 10; dx++) {
			for (int dz = -10; dz <= 10; dz++) {
				if (dx * dx + dz * dz > 100) {
					continue;
				}
				b.foundation(cx + dx, cz + dz, base - 1, stone);
				b.set(cx + dx, base, cz + dz, plating);
				b.clearColumn(cx + dx, cz + dz, base + 1, base + 22);
			}
		}

		// Bunker
		for (int dx = -4; dx <= 4; dx++) {
			for (int dz = -4; dz <= 4; dz++) {
				boolean wall = Math.abs(dx) == 4 || Math.abs(dz) == 4;
				for (int dy = 1; dy <= 4; dy++) {
					boolean door = dx == -4 && Math.abs(dz) <= 1 && dy <= 2;
					if (door) {
						continue;
					}
					if (dy == 4) {
						b.set(cx + dx, base + dy, cz + dz, astrium);
					} else if (wall) {
						b.set(cx + dx, base + dy, cz + dz, (dy == 2 && (dx + dz) % 2 == 0) ? lamp : plating);
					} else {
						b.set(cx + dx, base + dy, cz + dz, air);
					}
				}
			}
		}
		b.chest(cx + 2, base + 1, cz + 2, "chests/outpost");
		b.chest(cx + 2, base + 1, cz - 2, "chests/outpost");
		b.set(cx, base + 1, cz, plating);
		b.set(cx, base + 2, cz, lamp);

		// Pedestal and dish
		for (int dy = 5; dy <= 10; dy++) {
			for (int dx = -1; dx <= 1; dx++) {
				for (int dz = -1; dz <= 1; dz++) {
					if (dy <= 6 || (dx == 0 && dz == 0)) {
						b.set(cx + dx, base + dy, cz + dz, dy == 8 && dx == 0 && dz == 0 ? lamp : plating);
					}
				}
			}
		}
		int y0 = base + 11;
		for (int dx = -9; dx <= 9; dx++) {
			for (int dz = -9; dz <= 9; dz++) {
				double rho = Math.sqrt(dx * dx + dz * dz);
				if (rho > 8.5) {
					continue;
				}
				int y = y0 + (int) (rho * rho / 9.0);
				BlockState mat = white;
				if (rho > 7.2) {
					mat = plating;
				} else if (rho >= 4.0 && rho < 4.9 && ((dx + dz) & 1) == 0) {
					mat = lamp;
				}
				b.set(cx + dx, y, cz + dz, mat);
			}
		}
		// Feed horn and struts
		int tipY = y0 + 9;
		for (int y = y0; y <= tipY; y++) {
			b.set(cx, y, cz, plating);
		}
		b.set(cx, tipY + 1, cz, lamp);
		for (int k = 0; k < 3; k++) {
			double a = Math.toRadians(90 + k * 120);
			int rimX = (int) Math.round(Math.cos(a) * 7);
			int rimZ = (int) Math.round(Math.sin(a) * 7);
			int rimY = y0 + (int) (49.0 / 9.0) + 1;
			for (int s = 0; s <= 10; s++) {
				double t = s / 10.0;
				b.set(cx + (int) Math.round(rimX * (1 - t)), rimY + (int) Math.round((tipY - rimY) * t), cz + (int) Math.round(rimZ * (1 - t)), plating);
			}
		}

		// Antenna masts
		for (int[] m : new int[][] {{9, 0}, {-9, 0}, {0, 9}, {0, -9}}) {
			for (int dy = 1; dy <= 8; dy++) {
				b.set(cx + m[0], base + dy, cz + m[1], plating);
			}
			b.set(cx + m[0], base + 9, cz + m[1], lamp);
		}

		if (b.random.nextFloat() < 0.6f) {
			b.spawn(ModEntities.LUNAR_SENTINEL, cx + 0.5, base + 1, cz + 0.5 + 2);
		}
		if (b.random.nextFloat() < 0.5f) {
			b.spawn(ModEntities.PLASMA_DRONE, cx + 0.5, base + 12, cz + 0.5 + 3);
		}
		return true;
	}
}
