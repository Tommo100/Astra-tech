package com.astratech.world;

import com.astratech.block.LunarPortalBlock;
import com.astratech.registry.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

/** Detects a rectangular Gateway Frame (interior 2x3 up to 21x21) and fills it with portal blocks. */
public final class GatewayShape {
	public static final int MIN_WIDTH = 2;
	public static final int MIN_HEIGHT = 3;
	public static final int MAX_SIZE = 21;

	private final BlockPos bottomLeft;
	private final int width;
	private final int height;
	private final Direction.Axis axis;

	private GatewayShape(BlockPos bottomLeft, int width, int height, Direction.Axis axis) {
		this.bottomLeft = bottomLeft;
		this.width = width;
		this.height = height;
		this.axis = axis;
	}

	private static boolean isEmpty(BlockState state) {
		return state.isAir() || state.isOf(ModBlocks.LUNAR_PORTAL);
	}

	private static boolean isFrame(BlockState state) {
		return state.isOf(ModBlocks.GATEWAY_FRAME);
	}

	/** Tries to find a valid frame around {@code start} (an empty block) on the given axis. */
	public static GatewayShape find(WorldAccess world, BlockPos start, Direction.Axis axis) {
		Direction right = axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;
		Direction left = right.getOpposite();

		// Go down to the floor.
		BlockPos pos = start;
		int guard = 0;
		while (isEmpty(world.getBlockState(pos.down())) && guard++ < MAX_SIZE) {
			pos = pos.down();
		}
		if (!isFrame(world.getBlockState(pos.down()))) {
			return null;
		}

		// Go left to the left wall.
		guard = 0;
		BlockPos leftmost = pos;
		while (isEmpty(world.getBlockState(leftmost.offset(left))) && isFrame(world.getBlockState(leftmost.offset(left).down())) && guard++ < MAX_SIZE) {
			leftmost = leftmost.offset(left);
		}
		if (!isEmpty(world.getBlockState(leftmost)) || !isFrame(world.getBlockState(leftmost.offset(left)))) {
			return null;
		}

		// Measure the width along the bottom row.
		int width = 0;
		BlockPos p = leftmost;
		while (width <= MAX_SIZE && isEmpty(world.getBlockState(p)) && isFrame(world.getBlockState(p.down()))) {
			width++;
			p = p.offset(right);
		}
		if (width < MIN_WIDTH || width > MAX_SIZE || !isFrame(world.getBlockState(p))) {
			return null;
		}

		// Measure the height, checking both walls on every row.
		int height = 0;
		outer:
		while (height <= MAX_SIZE) {
			BlockPos rowStart = leftmost.up(height);
			if (!isFrame(world.getBlockState(rowStart.offset(left))) || !isFrame(world.getBlockState(rowStart.offset(right, width)))) {
				break;
			}
			for (int i = 0; i < width; i++) {
				if (!isEmpty(world.getBlockState(rowStart.offset(right, i)))) {
					break outer;
				}
			}
			height++;
		}
		if (height < MIN_HEIGHT || height > MAX_SIZE) {
			return null;
		}

		// The row above must be solid frame.
		BlockPos top = leftmost.up(height);
		for (int i = 0; i < width; i++) {
			if (!isFrame(world.getBlockState(top.offset(right, i)))) {
				return null;
			}
		}
		return new GatewayShape(leftmost, width, height, axis);
	}

	/** Finds a frame by trying both axes. */
	public static GatewayShape findAny(WorldAccess world, BlockPos start) {
		GatewayShape shape = find(world, start, Direction.Axis.X);
		if (shape == null) {
			shape = find(world, start, Direction.Axis.Z);
		}
		return shape;
	}

	public void fill(World world) {
		Direction right = axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;
		BlockState portal = ModBlocks.LUNAR_PORTAL.getDefaultState().with(LunarPortalBlock.AXIS, axis);
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				world.setBlockState(bottomLeft.offset(right, i).up(j), portal, 18);
			}
		}
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
}
