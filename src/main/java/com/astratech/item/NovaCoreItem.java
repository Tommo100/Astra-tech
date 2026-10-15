package com.astratech.item;

import com.astratech.entity.ThrownNovaCoreEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

/** Nova Core: crafting component, and a throwable that collapses into a black hole. */
public class NovaCoreItem extends Item {
	public NovaCoreItem(Settings settings) {
		super(settings);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_ENDER_PEARL_THROW, SoundCategory.PLAYERS, 0.8f, 0.6f);
		user.getItemCooldownManager().set(this, 30);
		if (!world.isClient) {
			ThrownNovaCoreEntity core = new ThrownNovaCoreEntity(world, user);
			core.setItem(stack);
			core.setVelocity(user, user.getPitch(), user.getYaw(), 0.0f, 1.4f, 1.0f);
			world.spawnEntity(core);
		}
		user.incrementStat(Stats.USED.getOrCreateStat(this));
		if (!user.getAbilities().creativeMode) {
			stack.decrement(1);
		}
		return TypedActionResult.success(stack, world.isClient);
	}
}
