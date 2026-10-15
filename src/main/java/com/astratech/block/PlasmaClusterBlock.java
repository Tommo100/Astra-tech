package com.astratech.block;

import com.astratech.registry.ModBlocks;
import net.minecraft.block.AmethystClusterBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldView;

/** Plasma crystal bud or cluster: grows from, and can only be placed on, Budding Plasma Crystal or Plasma Crystal Blocks. */
public class PlasmaClusterBlock extends AmethystClusterBlock {
	public PlasmaClusterBlock(int height, int xzOffset, Settings settings) {
		super(height, xzOffset, settings);
	}

	@Override
	public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
		Direction direction = state.get(FACING);
		BlockState support = world.getBlockState(pos.offset(direction.getOpposite()));
		return support.isOf(ModBlocks.BUDDING_PLASMA_CRYSTAL) || support.isOf(ModBlocks.PLASMA_CRYSTAL_BLOCK);
	}
}
