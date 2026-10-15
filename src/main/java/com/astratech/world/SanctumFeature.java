package com.astratech.world;

import com.astratech.registry.ModBlocks;
import com.astratech.registry.ModEntities;
import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;

/** The Overmind Sanctum: a walled arena with a stepped dais carrying a Summoning Terminal, guarded by sentinels. */
public class SanctumFeature extends BaseStructureFeature {
	public SanctumFeature(Codec<DefaultFeatureConfig> codec) {
		super(codec);
	}

	@Override
	protected boolean build(Builder b) {
		int base = b.maxSurface(13);
		if (base < 6 || base > 100) {
			return false;
		}
		BlockState plating = ModBlocks.TECH_PLATING.getDefaultState();
		BlockState lamp = ModBlocks.PLASMA_LAMP.getDefaultState();
		BlockState astrium = ModBlocks.LUNAR_ALLOY_BLOCK.getDefaultState();
		BlockState stone = ModBlocks.LUNAR_STONE.getDefaultState();
		BlockState air = Blocks.AIR.getDefaultState();
		int cx = b.cx;
		int cz = b.cz;

		// Floor, wall and clearing
		for (int dx = -14; dx <= 14; dx++) {
			for (int dz = -14; dz <= 14; dz++) {
				double d = Math.sqrt(dx * dx + dz * dz);
				if (d > 14.0) {
					continue;
				}
				int x = cx + dx;
				int z = cz + dz;
				b.foundation(x, z, base - 1, stone);
				BlockState floor = plating;
				if (d < 2.5 || (d >= 4.0 && d < 5.0) || (d >= 11.5 && d < 12.5)) {
					floor = astrium;
				} else if (d >= 8.0 && d < 8.9 && ((dx + dz) & 1) == 0) {
					floor = lamp;
				}
				b.set(x, base, z, floor);
				b.clearColumn(x, z, base + 1, base + 18);
				if (d >= 13.0) {
					boolean gate = Math.abs(dx) <= 2 || Math.abs(dz) <= 2;
					for (int dy = 1; dy <= 4; dy++) {
						if (gate && dy <= 4 && d >= 12.5 && (Math.abs(dx) <= 2 && Math.abs(dz) >= 9 || Math.abs(dz) <= 2 && Math.abs(dx) >= 9)) {
							continue;
						}
						if (dy == 4 && ((dx + dz) & 1) != 0) {
							continue;
						}
						b.set(x, base + dy, z, dy == 4 ? astrium : plating);
					}
				}
			}
		}

		// Pillars
		int[][] pillars = {{10, 0}, {7, 7}, {0, 10}, {-7, 7}, {-10, 0}, {-7, -7}, {0, -10}, {7, -7}};
		for (int[] p : pillars) {
			for (int dy = 1; dy <= 9; dy++) {
				b.set(cx + p[0], base + dy, cz + p[1], dy % 3 == 0 ? astrium : plating);
			}
			b.set(cx + p[0], base + 10, cz + p[1], lamp);
			b.set(cx + p[0], base + 11, cz + p[1], astrium);
		}

		// Stepped dais
		for (int dx = -4; dx <= 4; dx++) {
			for (int dz = -4; dz <= 4; dz++) {
				int m = Math.max(Math.abs(dx), Math.abs(dz));
				b.set(cx + dx, base + 1, cz + dz, m == 4 ? astrium : plating);
				if (m <= 3) {
					b.set(cx + dx, base + 2, cz + dz, m == 3 ? astrium : plating);
				}
				if (m <= 2) {
					b.set(cx + dx, base + 3, cz + dz, m == 2 ? lamp : astrium);
				}
			}
		}
		b.set(cx, base + 4, cz, ModBlocks.SUMMONING_TERMINAL.getDefaultState());
		for (int[] c : new int[][] {{2, 2}, {-2, 2}, {2, -2}, {-2, -2}}) {
			b.set(cx + c[0], base + 4, cz + c[1], plating);
			b.set(cx + c[0], base + 5, cz + c[1], lamp);
		}

		// Corner spires
		for (int[] p : new int[][] {{9, 9}, {-9, 9}, {9, -9}, {-9, -9}}) {
			for (int dy = 1; dy <= 6; dy++) {
				b.set(cx + p[0], base + dy, cz + p[1], plating);
			}
			b.set(cx + p[0], base + 7, cz + p[1], lamp);
		}

		// Tribute chests
		b.chest(cx + 9, base + 1, cz - 4, "chests/sanctum");
		b.chest(cx - 9, base + 1, cz + 4, "chests/sanctum");
		b.set(cx + 9, base + 2, cz - 4, air);

		// Guardians
		b.spawn(ModEntities.LUNAR_SENTINEL, cx + 6.5, base + 1, cz + 6.5);
		b.spawn(ModEntities.LUNAR_SENTINEL, cx - 6.5, base + 1, cz + 6.5);
		b.spawn(ModEntities.LUNAR_SENTINEL, cx + 6.5, base + 1, cz - 6.5);
		b.spawn(ModEntities.LUNAR_SENTINEL, cx - 6.5, base + 1, cz - 6.5);
		b.spawn(ModEntities.PLASMA_DRONE, cx + 0.5, base + 9, cz + 0.5);
		return true;
	}
}
