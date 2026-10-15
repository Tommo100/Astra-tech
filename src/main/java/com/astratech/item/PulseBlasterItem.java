package com.astratech.item;

import com.astratech.entity.PlasmaBoltEntity;
import com.astratech.entity.PlasmaOrbEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

/** Ranged end-game weapon. Tap right-click for a fast plasma bolt; hold to charge a heavy exploding Plasma Orb. */
public class PulseBlasterItem extends Item {
	/** Ticks of holding needed for a full charge. */
	public static final int CHARGE_TICKS = 30;

	public PulseBlasterItem(Settings settings) {
		super(settings);
	}

	@Override
	public UseAction getUseAction(ItemStack stack) {
		return UseAction.NONE;
	}

	@Override
	public int getMaxUseTime(ItemStack stack) {
		return 72000;
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		ItemStack stack = player.getStackInHand(hand);
		if (player.getItemCooldownManager().isCoolingDown(this)) {
			return TypedActionResult.pass(stack);
		}
		player.setCurrentHand(hand);
		return TypedActionResult.consume(stack);
	}

	@Override
	public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
		int charge = this.getMaxUseTime(stack) - remainingUseTicks;
		if (charge == CHARGE_TICKS) {
			world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS, 0.8f, 1.8f);
		} else if (charge > 3 && charge < CHARGE_TICKS && charge % 6 == 0) {
			world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), SoundCategory.PLAYERS, 0.4f, 0.6f + charge * 0.03f);
		}
		if (!world.isClient && charge > 5 && world instanceof ServerWorld sw) {
			var look = user.getRotationVec(1.0f);
			double r = Math.min(charge, CHARGE_TICKS) / (double) CHARGE_TICKS;
			sw.spawnParticles(charge >= CHARGE_TICKS ? ParticleTypes.END_ROD : ParticleTypes.ELECTRIC_SPARK,
				user.getX() + look.x * 1.2, user.getEyeY() - 0.2 + look.y * 1.2, user.getZ() + look.z * 1.2,
				1 + (int) (3 * r), 0.15, 0.15, 0.15, 0.02);
		}
	}

	@Override
	public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
		if (!(user instanceof PlayerEntity player)) {
			return;
		}
		int charge = this.getMaxUseTime(stack) - remainingUseTicks;
		boolean heavy = charge >= CHARGE_TICKS;
		Hand hand = player.getActiveHand();
		if (!world.isClient) {
			if (heavy) {
				PlasmaOrbEntity orb = new PlasmaOrbEntity(world, player);
				orb.setDamage(40.0f);
				orb.setBlastDamage(30.0f);
				orb.setVelocity(player, player.getPitch(), player.getYaw(), 0.0f, 2.2f, 0.0f);
				world.spawnEntity(orb);
				stack.damage(4, player, p -> p.sendToolBreakStatus(hand));
			} else {
				PlasmaBoltEntity bolt = new PlasmaBoltEntity(world, player);
				bolt.setDamage(16.0f);
				bolt.setVelocity(player, player.getPitch(), player.getYaw(), 0.0f, 5.0f, 0.1f);
				world.spawnEntity(bolt);
				stack.damage(1, player, p -> p.sendToolBreakStatus(hand));
			}
		}
		if (heavy) {
			world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.PLAYERS, 0.7f, 1.6f);
		} else {
			world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_BEACON_DEACTIVATE, SoundCategory.PLAYERS, 0.6f, 2.0f);
		}
		player.getItemCooldownManager().set(this, heavy ? 40 : 7);
	}
}
