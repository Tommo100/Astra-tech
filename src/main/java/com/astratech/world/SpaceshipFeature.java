package com.astratech.world;

import com.astratech.registry.ModBlocks;
import com.astratech.registry.ModEntities;
import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;

/** A landed spaceship resting on four legs, with a cockpit, cargo bay, swept wings, tail fin and engine bell. */
public class SpaceshipFeature extends BaseStructureFeature {
	public SpaceshipFeature(Codec<DefaultFeatureConfig> codec) {
		super(codec);
	}

	private static double hullRadius(int lx) {
		if (lx > 6) {
			double t = (lx - 6) / 8.0;
			return 4.4 * Math.sqrt(Math.max(0.0, 1.0 - t * t));
		}
		if (lx < -9) {
			return 3.4 - (-9 - lx) * 0.1;
		}
		return 4.4;
	}

	/** Rotates local (lx, lz) into world offsets for orientation 0..3. */
	private static int rx(int lx, int lz, int r) {
		return switch (r) {
			case 1 -> -lz;
			case 2 -> -lx;
			case 3 -> lz;
			default -> lx;
		};
	}

	private static int rz(int lx, int lz, int r) {
		return switch (r) {
			case 1 -> lx;
			case 2 -> -lz;
			case 3 -> -lx;
			default -> lz;
		};
	}

	@Override
	protected boolean build(Builder b) {
		int base = b.maxSurface(10);
		if (base < 6 || base > 100) {
			return false;
		}
		int rot = b.random.nextInt(4);
		int axisY = base + 7;
		BlockState plating = ModBlocks.TECH_PLATING.getDefaultState();
		BlockState lamp = ModBlocks.PLASMA_LAMP.getDefaultState();
		BlockState astrium = ModBlocks.LUNAR_ALLOY_BLOCK.getDefaultState();
		BlockState white = Blocks.WHITE_CONCRETE.getDefaultState();
		BlockState trim = Blocks.CYAN_CONCRETE.getDefaultState();
		BlockState seat = Blocks.LIGHT_GRAY_CONCRETE.getDefaultState();
		BlockState glass = Blocks.CYAN_STAINED_GLASS.getDefaultState();
		BlockState stone = ModBlocks.LUNAR_STONE.getDefaultState();
		BlockState air = Blocks.AIR.getDefaultState();

		// Hull
		for (int lx = -13; lx <= 13; lx++) {
			for (int ly = -5; ly <= 5; ly++) {
				for (int lz = -5; lz <= 5; lz++) {
					double d = Math.sqrt(ly * ly + lz * lz);
					int x = b.cx + rx(lx, lz, rot);
					int z = b.cz + rz(lx, lz, rot);
					int y = axisY + ly;
					if (lx == -13) {
						if (d >= 1.5 && d < 2.6) {
							b.set(x, y, z, plating);
						} else if (d < 1.5) {
							b.set(x, y, z, lamp);
						}
						continue;
					}
					double r = hullRadius(lx);
					if (d > r + 0.4) {
						continue;
					}
					if (d < r - 0.9) {
						if (ly <= -3 || lx <= -11) {
							b.set(x, y, z, plating);
						} else {
							b.set(x, y, z, air);
						}
						continue;
					}
					BlockState mat;
					boolean cockpitWindow = lx >= 8 && lx <= 12 && ly >= 0 && Math.abs(lz) <= 3;
					boolean porthole = ly == 1 && Math.abs(lz) >= r - 1.2 && Math.floorMod(lx, 4) == 1 && lx >= -7 && lx <= 5;
					if (cockpitWindow || porthole) {
						mat = glass;
					} else if (ly < -1) {
						mat = plating;
					} else if (ly == 0 && Math.abs(lz) >= r - 1.2) {
						mat = trim;
					} else if (Math.floorMod(lx, 6) == 0) {
						mat = astrium;
					} else {
						mat = white;
					}
					boolean door = lx >= 0 && lx <= 1 && lz <= -3 && ly >= -2 && ly <= 0;
					b.set(x, y, z, door ? air : mat);
				}
			}
		}

		// Swept wings
		for (int side = -1; side <= 1; side += 2) {
			for (int az = 4; az <= 11; az++) {
				int lxMax = 3 - (az - 4);
				for (int lx = -9; lx <= lxMax; lx++) {
					int lz = az * side;
					boolean edge = lx == lxMax || lx == -9 || az == 11;
					BlockState mat = edge ? plating : white;
					int ly = az > 8 ? -1 : -2;
					b.set(b.cx + rx(lx, lz, rot), axisY + ly, b.cz + rz(lx, lz, rot), mat);
				}
				if (az == 11) {
					b.set(b.cx + rx(lxMax, az * side, rot), axisY, b.cz + rz(lxMax, az * side, rot), lamp);
					b.set(b.cx + rx(lxMax, az * side, rot), axisY - 1, b.cz + rz(lxMax, az * side, rot), plating);
				}
			}
		}

		// Tail fin
		for (int lx = -12; lx <= -7; lx++) {
			int h = (-7 - lx) + 2;
			for (int ly = 3; ly <= 3 + h; ly++) {
				b.set(b.cx + rx(lx, 0, rot), axisY + ly, b.cz + rz(lx, 0, rot), ly == 3 + h && lx == -12 ? lamp : white);
			}
		}

		// Landing legs
		int[][] legs = {{-7, -3}, {-7, 3}, {6, -3}, {6, 3}};
		for (int[] leg : legs) {
			int x = b.cx + rx(leg[0], leg[1], rot);
			int z = b.cz + rz(leg[0], leg[1], rot);
			for (int y = base; y <= axisY - 3; y++) {
				b.set(x, y, z, plating);
			}
			for (int dx = -1; dx <= 1; dx++) {
				for (int dz = -1; dz <= 1; dz++) {
					b.foundation(x + dx, z + dz, base - 1, stone);
					b.set(x + dx, base, z + dz, plating);
				}
			}
		}

		// Boarding ramp from the side door
		for (int k = 0; k <= 5; k++) {
			int y = axisY - 3 - k;
			if (y < base) {
				break;
			}
			for (int lx = -1; lx <= 2; lx++) {
				int lz = -(5 + k);
				int x = b.cx + rx(lx, lz, rot);
				int z = b.cz + rz(lx, lz, rot);
				b.foundation(x, z, y - 1, stone);
				b.set(x, y, z, plating);
				b.set(x, y + 1, z, air);
				b.set(x, y + 2, z, air);
			}
		}

		// Interior fittings
		for (int lx = -8; lx <= 8; lx += 4) {
			b.set(b.cx + rx(lx, 0, rot), axisY + 3, b.cz + rz(lx, 0, rot), lamp);
		}
		// cargo bay
		b.chest(b.cx + rx(-8, 2, rot), axisY - 2, b.cz + rz(-8, 2, rot), "chests/spaceship");
		b.chest(b.cx + rx(-8, -2, rot), axisY - 2, b.cz + rz(-8, -2, rot), "chests/spaceship");
		b.set(b.cx + rx(-6, 2, rot), axisY - 2, b.cz + rz(-6, 2, rot), Blocks.BARREL.getDefaultState());
		b.set(b.cx + rx(-6, -2, rot), axisY - 2, b.cz + rz(-6, -2, rot), Blocks.BARREL.getDefaultState());
		b.set(b.cx + rx(-10, 0, rot), axisY - 2, b.cz + rz(-10, 0, rot), lamp);
		// cockpit
		for (int lz = -1; lz <= 1; lz++) {
			b.set(b.cx + rx(11, lz, rot), axisY - 2, b.cz + rz(11, lz, rot), plating);
			b.set(b.cx + rx(11, lz, rot), axisY - 1, b.cz + rz(11, lz, rot), lz == 0 ? lamp : plating);
		}
		b.set(b.cx + rx(9, -1, rot), axisY - 2, b.cz + rz(9, -1, rot), seat);
		b.set(b.cx + rx(9, 1, rot), axisY - 2, b.cz + rz(9, 1, rot), seat);
		b.chest(b.cx + rx(5, 2, rot), axisY - 2, b.cz + rz(5, 2, rot), "chests/spaceship");

		// Occupants
		b.spawn(ModEntities.LUNAR_SENTINEL, b.cx + rx(2, 0, rot) + 0.5, axisY - 1, b.cz + rz(2, 0, rot) + 0.5);
		if (b.random.nextFloat() < 0.5f) {
			b.spawn(ModEntities.VOID_STALKER, b.cx + rx(-5, 0, rot) + 0.5, axisY - 1, b.cz + rz(-5, 0, rot) + 0.5);
		}
		if (b.random.nextFloat() < 0.25f) {
			b.spawn(ModEntities.ASTRO_BOT, b.cx + rx(8, 0, rot) + 0.5, axisY - 1, b.cz + rz(8, 0, rot) + 0.5);
		}
		return true;
	}
}
