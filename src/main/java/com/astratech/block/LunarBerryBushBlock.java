package com.astratech.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.SweetBerryBushBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import com.astratech.registry.ModBlocks;

import java.util.function.Supplier;

/** A thornless berry bush that grows on the Moon's surface blocks, with no light requirement. */
public class LunarBerryBushBlock extends SweetBerryBushBlock {
	private final Supplier<Item> berry;

	public LunarBerryBushBlock(Settings settings, Supplier<Item> berry) {
		super(settings);
		this.berry = berry;
	}

	@Override
	protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
		return ModBlocks.isMoonSurface(floor);
	}

	@Override
	public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
		return new ItemStack(this.berry.get());
	}

	@Override
	public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		int age = state.get(AGE);
		if (age < 3 && random.nextInt(6) == 0) {
			world.setBlockState(pos, state.with(AGE, age + 1), 2);
		}
	}

	@Override
	public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
		// no thorns
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		int age = state.get(AGE);
		if (age > 1) {
			if (!world.isClient) {
				int count = 1 + world.random.nextInt(2) + (age == 3 ? 1 : 0);
				dropStack(world, pos, new ItemStack(this.berry.get(), count));
				world.playSound(null, pos, SoundEvents.ITEM_SWEET_BERRIES_PICK_FROM_BUSH, SoundCategory.BLOCKS, 1.0f, 0.8f + world.random.nextFloat() * 0.4f);
				world.setBlockState(pos, state.with(AGE, 1), 2);
			}
			return ActionResult.success(world.isClient);
		}
		return ActionResult.PASS;
	}
}
