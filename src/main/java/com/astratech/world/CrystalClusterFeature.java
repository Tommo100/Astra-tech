package com.astratech.world;

import com.astratech.registry.ModBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.block.AmethystClusterBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.Direction;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;

/** Crystal Basin signature: leaning spires of lunar stone and Plasma Crystal ore, often capped with a budding crystal. */
public class CrystalClusterFeature extends BaseStructureFeature {
	public CrystalClusterFeature(Codec<DefaultFeatureConfig> codec) {
		super(codec);
	}

	/** Real ground level: craters and other features change the terrain after the heightmap was made. */
	private static int ground(Builder b, int x, int z) {
		int s = b.surface(x, z);
		for (int y = Math.min(s + 10, 120); y > s - 26 && y > 1; y--) {
			BlockState st = b.get(x, y, z);
			if (!st.isAir() && !st.isReplaceable()) {
				return y;
			}
		}
		return -1;
	}

	@Override
	protected boolean build(Builder b) {
		BlockState ore = ModBlocks.PLASMA_CRYSTAL_ORE.getDefaultState();
		BlockState stone = ModBlocks.LUNAR_STONE.getDefaultState();
		BlockState budding = ModBlocks.BUDDING_PLASMA_CRYSTAL.getDefaultState();
		int count = 3 + b.random.nextInt(5);
		boolean placed = false;
		for (int i = 0; i < count; i++) {
			int x = b.cx + b.random.nextInt(13) - 6;
			int z = b.cz + b.random.nextInt(13) - 6;
			int base = ground(b, x, z);
			if (base < 2) {
				continue;
			}
			int h = 3 + b.random.nextInt(8);
			int leanX = b.random.nextInt(3) - 1;
			int leanZ = b.random.nextInt(3) - 1;
			int topX = x;
			int topZ = z;
			for (int y = 0; y < h; y++) {
				int px = x + (y > h / 2 ? leanX : 0) + (y > (h * 3) / 4 ? leanX : 0);
				int pz = z + (y > h / 2 ? leanZ : 0) + (y > (h * 3) / 4 ? leanZ : 0);
				// keep the spire joined: fill the gap below if the lean shifted it sideways
				b.set(px, base + 1 + y, pz, b.random.nextInt(100) < 45 ? ore : stone);
				if (px != x || pz != z) {
					b.set(px - Integer.signum(px - x) * (px != x ? 1 : 0), base + 1 + y, pz, stone);
					b.set(px, base + 1 + y, pz - Integer.signum(pz - z) * (pz != z ? 1 : 0), stone);
				}
				if (y < 2 && h > 5) {
					b.set(px + 1, base + 1 + y, pz, stone);
					b.set(px, base + 1 + y, pz + 1, ore);
				}
				topX = px;
				topZ = pz;
			}
			if (b.random.nextInt(100) < 55) {
				b.set(topX, base + h, topZ, budding);
				if (b.random.nextBoolean()) {
					b.set(topX, base + h + 1, topZ, ModBlocks.SMALL_PLASMA_BUD.getDefaultState().with(AmethystClusterBlock.FACING, Direction.UP));
				}
			}
			placed = true;
		}
		return placed;
	}
}
