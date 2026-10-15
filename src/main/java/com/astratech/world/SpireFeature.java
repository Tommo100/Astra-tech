package com.astratech.world;

import com.astratech.registry.ModBlocks;
import com.astratech.registry.ModEntities;
import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LadderBlock;
import net.minecraft.util.math.Direction;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;

/** The Lumen Spire: a vertical tech facility shaped like a tree, with roots, a hollow trunk, branches and a glass canopy. */
public class SpireFeature extends BaseStructureFeature {
	public SpireFeature(Codec<DefaultFeatureConfig> codec) {
		super(codec);
	}

	private static double trunkRadius(int t) {
		return t < 6 ? 3.6 + (6 - t) * 0.45 : 3.6;
	}

	@Override
	protected boolean build(Builder b) {
		int base = b.maxSurface(6);
		int height = Math.min(42, 120 - base);
		if (base < 6 || height < 22) {
			return false;
		}
		BlockState plating = ModBlocks.TECH_PLATING.getDefaultState();
		BlockState lamp = ModBlocks.PLASMA_LAMP.getDefaultState();
		BlockState astrium = ModBlocks.LUNAR_ALLOY_BLOCK.getDefaultState();
		BlockState stone = ModBlocks.LUNAR_STONE.getDefaultState();
		BlockState glass = Blocks.CYAN_STAINED_GLASS.getDefaultState();
		BlockState air = Blocks.AIR.getDefaultState();
		BlockState ladder = Blocks.LADDER.getDefaultState().with(LadderBlock.FACING, Direction.WEST);
		int cx = b.cx;
		int cz = b.cz;
		int top = base + height;

		// Trunk
		for (int t = 0; t <= height; t++) {
			double r = trunkRadius(t);
			int reach = (int) Math.ceil(r);
			for (int dx = -reach; dx <= reach; dx++) {
				for (int dz = -reach; dz <= reach; dz++) {
					double d = Math.sqrt(dx * dx + dz * dz);
					if (d >= r) {
						continue;
					}
					int x = cx + dx;
					int z = cz + dz;
					int y = base + t;
					if (t == 0) {
						b.foundation(x, z, y - 1, stone);
						b.set(x, y, z, d < 1.5 ? astrium : plating);
						continue;
					}
					boolean shell = d >= r - 1.0;
					boolean doorway = t <= 2 && dx <= -2 && Math.abs(dz) <= 1;
					if (shell) {
						if (doorway) {
							b.set(x, y, z, air);
						} else {
							boolean seam = (dx == 0 || dz == 0 || Math.abs(dx) == Math.abs(dz)) && t % 4 == 2;
							b.set(x, y, z, seam ? lamp : plating);
						}
					} else if (t % 8 == 0 && t >= 8 && !(dx == 2 && dz == 0)) {
						b.set(x, y, z, d < 1.2 ? astrium : plating);
					} else {
						b.set(x, y, z, air);
					}
				}
			}
		}
		for (int t = 1; t <= height - 2; t++) {
			b.set(cx + 3, base + t, cz, plating);
			b.set(cx + 2, base + t, cz, ladder);
		}
		b.chest(cx - 2, base + 9, cz, "chests/spire");
		b.chest(cx, base + 25 > top - 2 ? base + 17 : base + 25, cz - 2, "chests/spire");

		// Roots
		for (int k = 0; k < 6; k++) {
			double a = Math.toRadians(k * 60 + 20);
			for (int s = 3; s <= 11; s++) {
				int x = cx + (int) Math.round(Math.cos(a) * s);
				int z = cz + (int) Math.round(Math.sin(a) * s);
				int y = base + Math.max(0, 3 - (s - 3) / 2);
				b.set(x, y, z, plating);
				b.set(x, y + 1, z, s < 6 ? plating : air);
				b.foundation(x, z, y - 1, stone);
				if (s % 4 == 0) {
					b.set(x, y + 1, z, lamp);
				}
			}
		}

		// Branches with glowing pods
		for (int i = 0; i < 4; i++) {
			int h = top - 5 - i * 6;
			if (h < base + 10) {
				continue;
			}
			for (int k = 0; k < 3; k++) {
				double a = Math.toRadians(i * 47 + k * 120 + b.random.nextInt(25));
				int len = 6 + b.random.nextInt(4);
				int ex = cx;
				int ey = h;
				int ez = cz;
				for (int s = 3; s <= len + 3; s++) {
					ex = cx + (int) Math.round(Math.cos(a) * s);
					ez = cz + (int) Math.round(Math.sin(a) * s);
					ey = h + (int) ((s - 3) * 0.35);
					b.set(ex, ey, ez, plating);
					b.set(ex, ey - 1, ez, s % 3 == 0 ? lamp : plating);
				}
				for (int dx = -2; dx <= 2; dx++) {
					for (int dy = -2; dy <= 2; dy++) {
						for (int dz = -2; dz <= 2; dz++) {
							double d3 = Math.sqrt(dx * dx + dy * dy + dz * dz);
							if (d3 >= 1.5 && d3 < 2.5) {
								b.set(ex + dx, ey + 1 + dy, ez + dz, glass);
							} else if (d3 < 1.5) {
								b.set(ex + dx, ey + 1 + dy, ez + dz, d3 < 0.5 ? lamp : air);
							}
						}
					}
				}
			}
		}

		// Canopy observatory
		for (int dx = -7; dx <= 7; dx++) {
			for (int dz = -7; dz <= 7; dz++) {
				for (int dy = 0; dy <= 7; dy++) {
					double d3 = Math.sqrt(dx * dx + dy * dy + dz * dz);
					int x = cx + dx;
					int y = top + dy;
					int z = cz + dz;
					if (dy == 0) {
						if (d3 < 6.0 && !(dx == 2 && dz == 0)) {
							b.set(x, y, z, (d3 >= 4.0 && d3 < 5.0) ? astrium : plating);
						}
						continue;
					}
					if (d3 >= 5.5 && d3 < 6.5) {
						boolean rib = dx == 0 || dz == 0 || Math.abs(dx) == Math.abs(dz);
						b.set(x, y, z, rib ? plating : glass);
					} else if (d3 < 5.5 && !(dx == 0 && dz == 0)) {
						b.set(x, y, z, air);
					}
				}
			}
		}
		for (int dy = 1; dy <= 3; dy++) {
			b.set(cx, top + dy, cz, dy == 3 ? lamp : astrium);
		}
		b.set(cx, top + 7, cz, lamp);
		b.chest(cx + 4, top + 1, cz, "chests/spire");
		b.chest(cx - 4, top + 1, cz, "chests/spire");

		// Guardians
		b.spawn(ModEntities.LUNAR_SENTINEL, cx - 0.5 + 1, base + 1, cz + 0.5);
		b.spawn(ModEntities.PLASMA_DRONE, cx + 0.5, top + 3, cz + 3.5);
		if (b.random.nextBoolean()) {
			b.spawn(ModEntities.PLASMA_DRONE, cx + 0.5, top + 3, cz - 3.5);
		}
		return true;
	}
}
