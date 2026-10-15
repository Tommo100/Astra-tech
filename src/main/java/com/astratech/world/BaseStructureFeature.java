package com.astratech.world;

import com.mojang.serialization.Codec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

/** Shared helpers for the large Moon structures. All writes are clamped to 14 blocks around the origin (safe for worldgen). */
public abstract class BaseStructureFeature extends Feature<DefaultFeatureConfig> {
	protected BaseStructureFeature(Codec<DefaultFeatureConfig> codec) {
		super(codec);
	}

	@Override
	public final boolean generate(FeatureContext<DefaultFeatureConfig> context) {
		return build(new Builder(context.getWorld(), context.getOrigin(), context.getRandom()));
	}

	protected abstract boolean build(Builder b);

	public static final class Builder {
		public static final int LIMIT = 14;
		public final StructureWorldAccess world;
		public final int cx;
		public final int cz;
		public final Random random;

		Builder(StructureWorldAccess world, BlockPos origin, Random random) {
			this.world = world;
			this.cx = origin.getX();
			this.cz = origin.getZ();
			this.random = random;
		}

		public int surface(int x, int z) {
			return this.world.getTopY(Heightmap.Type.WORLD_SURFACE_WG, x, z) - 1;
		}

		/** Highest ground level within a circle around the origin. */
		public int maxSurface(int radius) {
			int best = Integer.MIN_VALUE;
			for (int dx = -radius; dx <= radius; dx += 2) {
				for (int dz = -radius; dz <= radius; dz += 2) {
					if (dx * dx + dz * dz <= radius * radius) {
						best = Math.max(best, this.surface(this.cx + dx, this.cz + dz));
					}
				}
			}
			return best;
		}

		public boolean inRange(int x, int z) {
			return Math.abs(x - this.cx) <= LIMIT && Math.abs(z - this.cz) <= LIMIT;
		}

		public void set(int x, int y, int z, BlockState state) {
			if (this.inRange(x, z) && y >= 1 && y <= this.world.getTopY() - 3) {
				this.world.setBlockState(new BlockPos(x, y, z), state, Block.NOTIFY_LISTENERS);
			}
		}

		public BlockState get(int x, int y, int z) {
			return this.world.getBlockState(new BlockPos(x, y, z));
		}

		/** Fills downward from topY until existing ground is reached (at most 24 blocks). */
		public void foundation(int x, int z, int topY, BlockState state) {
			for (int y = topY; y > topY - 24 && y >= 1; y--) {
				if (!this.get(x, y, z).isAir() && !this.get(x, y, z).isReplaceable()) {
					break;
				}
				this.set(x, y, z, state);
			}
		}

		public void fill(int x0, int y0, int z0, int x1, int y1, int z1, BlockState state) {
			for (int x = Math.min(x0, x1); x <= Math.max(x0, x1); x++) {
				for (int y = Math.min(y0, y1); y <= Math.max(y0, y1); y++) {
					for (int z = Math.min(z0, z1); z <= Math.max(z0, z1); z++) {
						this.set(x, y, z, state);
					}
				}
			}
		}

		/** Clears a column to air only where something is actually there. */
		public void clearColumn(int x, int z, int fromY, int toY) {
			BlockState air = Blocks.AIR.getDefaultState();
			for (int y = fromY; y <= toY; y++) {
				if (this.inRange(x, z) && !this.get(x, y, z).isAir()) {
					this.set(x, y, z, air);
				}
			}
		}

		public void chest(int x, int y, int z, String lootPath) {
			if (!this.inRange(x, z)) {
				return;
			}
			BlockPos pos = new BlockPos(x, y, z);
			this.world.setBlockState(pos, Blocks.CHEST.getDefaultState(), Block.NOTIFY_LISTENERS);
			LootableContainerBlockEntity.setLootTable(this.world, this.random, pos, new Identifier("astratech", lootPath));
		}

		public void spawn(EntityType<? extends MobEntity> type, double x, double y, double z) {
			MobEntity mob = type.create(this.world.toServerWorld());
			if (mob != null) {
				mob.refreshPositionAndAngles(x, y, z, this.random.nextFloat() * 360.0f, 0.0f);
				mob.setPersistent();
				this.world.spawnEntityAndPassengers(mob);
			}
		}
	}
}
