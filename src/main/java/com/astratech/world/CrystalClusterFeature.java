package com.astratech.world;

import com.astratech.registry.ModBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;

/** Crystal Basin signature: a cluster of leaning Plasma Crystal ore spires thrusting out of the dust. */
public class CrystalClusterFeature extends BaseStructureFeature {
	public CrystalClusterFeature(Codec<DefaultFeatureConfig> codec) {
		super(codec);
	}

	@Override
	protected boolean build(Builder b) {
		BlockState ore = ModBlocks.PLASMA_CRYSTAL_ORE.getDefaultState();
		int count = 3 + b.random.nextInt(5);
		boolean placed = false;
		for (int i = 0; i < count; i++) {
			int x = b.cx + b.random.nextInt(13) - 6;
			int z = b.cz + b.random.nextInt(13) - 6;
			int base = b.surface(x, z);
			if (base < 2 || base > 110) {
				continue;
			}
			int h = 3 + b.random.nextInt(8);
			int leanX = b.random.nextInt(3) - 1;
			int leanZ = b.random.nextInt(3) - 1;
			for (int y = 0; y < h; y++) {
				int px = x + (y > h / 2 ? leanX : 0) + (y > (h * 3) / 4 ? leanX : 0);
				int pz = z + (y > h / 2 ? leanZ : 0) + (y > (h * 3) / 4 ? leanZ : 0);
				b.set(px, base + 1 + y, pz, ore);
				if (y < 2 && h > 5) {
					b.set(px + 1, base + 1 + y, pz, ore);
					b.set(px, base + 1 + y, pz + 1, ore);
				}
			}
			placed = true;
		}
		return placed;
	}
}
