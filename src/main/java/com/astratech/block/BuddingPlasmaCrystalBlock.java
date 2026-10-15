package com.astratech.block;

import com.astratech.registry.ModBlocks;
import net.minecraft.block.AmethystClusterBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluids;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;

/** Slowly grows Plasma Crystal buds and clusters on its faces, like budding amethyst. */
public class BuddingPlasmaCrystalBlock extends Block {
	private static final Direction[] DIRECTIONS = Direction.values();

	public BuddingPlasmaCrystalBlock(Settings settings) {
		super(settings);
	}

	@Override
	public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		if (random.nextInt(5) != 0) {
			return;
		}
		Direction direction = DIRECTIONS[random.nextInt(DIRECTIONS.length)];
		BlockPos target = pos.offset(direction);
		BlockState current = world.getBlockState(target);
		Block next = null;
		if (canGrowIn(current)) {
			next = ModBlocks.SMALL_PLASMA_BUD;
		} else if (current.isOf(ModBlocks.SMALL_PLASMA_BUD) && current.get(AmethystClusterBlock.FACING) == direction) {
			next = ModBlocks.MEDIUM_PLASMA_BUD;
		} else if (current.isOf(ModBlocks.MEDIUM_PLASMA_BUD) && current.get(AmethystClusterBlock.FACING) == direction) {
			next = ModBlocks.LARGE_PLASMA_BUD;
		} else if (current.isOf(ModBlocks.LARGE_PLASMA_BUD) && current.get(AmethystClusterBlock.FACING) == direction) {
			next = ModBlocks.PLASMA_CRYSTAL_CLUSTER;
		}
		if (next != null) {
			BlockState grown = next.getDefaultState()
				.with(AmethystClusterBlock.FACING, direction)
				.with(AmethystClusterBlock.WATERLOGGED, current.getFluidState().getFluid() == Fluids.WATER);
			world.setBlockState(target, grown);
		}
	}

	public static boolean canGrowIn(BlockState state) {
		return state.isAir() || (state.isOf(Blocks.WATER) && state.getFluidState().getLevel() == 8);
	}
}
