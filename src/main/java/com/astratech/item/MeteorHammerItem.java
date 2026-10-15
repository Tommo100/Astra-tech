package com.astratech.item;

import com.astratech.entity.MeteorEntity;
import com.astratech.material.AstralToolMaterial;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector3f;

/**
 * Meteor Hammer: a slow, heavy melee weapon. Hold right-click to charge; while charging the impact zone is
 * marked where you look. Release when fully charged to call down three meteors.
 */
public class MeteorHammerItem extends SwordItem {
	public static final int CHARGE_TICKS = 40;
	private static final double RANGE = 48.0;
	private static final double SPREAD = 3.6;

	public MeteorHammerItem(Settings settings) {
		super(AstralToolMaterial.INSTANCE, 9, -3.1f, settings);
	}

	@Override
	public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		boolean result = super.postHit(stack, target, attacker);
		target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 40, 3));
		target.takeKnockback(0.8, attacker.getX() - target.getX(), attacker.getZ() - target.getZ());
		return result;
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

	/** Where the player is aiming on the ground, or null. */
	private static Vec3d aimPoint(LivingEntity user) {
		HitResult hit = user.raycast(RANGE, 1.0f, false);
		if (hit instanceof BlockHitResult block && hit.getType() == HitResult.Type.BLOCK) {
			return block.getPos();
		}
		return null;
	}

	private static Vec3d[] impacts(Vec3d center, float yaw) {
		Vec3d[] out = new Vec3d[3];
		for (int i = 0; i < 3; i++) {
			double a = Math.toRadians(yaw) + i * Math.PI * 2.0 / 3.0;
			out[i] = center.add(Math.cos(a) * SPREAD, 0.0, Math.sin(a) * SPREAD);
		}
		return out;
	}

	@Override
	public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
		int charge = this.getMaxUseTime(stack) - remainingUseTicks;
		boolean ready = charge >= CHARGE_TICKS;
		if (ready && charge == CHARGE_TICKS) {
			world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS, 1.0f, 0.7f);
		} else if (!ready && charge % 8 == 0) {
			world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.BLOCK_ANVIL_PLACE, SoundCategory.PLAYERS, 0.3f, 0.5f + charge * 0.02f);
		}
		if (!world.isClient) {
			return;
		}
		// Client-side targeting preview, only the wielder sees it
		Vec3d center = aimPoint(user);
		if (center == null) {
			return;
		}
		float progress = Math.min(1.0f, charge / (float) CHARGE_TICKS);
		Vector3f color = ready ? new Vector3f(1.0f, 0.9f, 0.3f) : new Vector3f(1.0f, 0.25f + 0.3f * progress, 0.1f);
		DustParticleEffect dust = new DustParticleEffect(color, 1.2f);
		double ringRadius = SPREAD + 2.0;
		int points = 28;
		for (int i = 0; i < points; i++) {
			double a = i * Math.PI * 2.0 / points + charge * 0.05;
			world.addParticle(dust, center.x + Math.cos(a) * ringRadius, center.y + 0.15, center.z + Math.sin(a) * ringRadius, 0.0, 0.0, 0.0);
		}
		for (Vec3d impact : impacts(center, user.getYaw())) {
			for (int i = 0; i < 10; i++) {
				double a = i * Math.PI * 2.0 / 10.0;
				world.addParticle(dust, impact.x + Math.cos(a) * 1.6, impact.y + 0.15, impact.z + Math.sin(a) * 1.6, 0.0, 0.0, 0.0);
			}
			if (ready) {
				world.addParticle(ParticleTypes.FLAME, impact.x, impact.y + 0.2, impact.z, 0.0, 0.05, 0.0);
			}
		}
	}

	@Override
	public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
		if (!(user instanceof PlayerEntity player) || world.isClient) {
			return;
		}
		int charge = this.getMaxUseTime(stack) - remainingUseTicks;
		if (charge < CHARGE_TICKS) {
			return;
		}
		Vec3d center = aimPoint(user);
		if (center == null) {
			world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.BLOCK_BEACON_DEACTIVATE, SoundCategory.PLAYERS, 1.0f, 0.8f);
			player.getItemCooldownManager().set(this, 20);
			return;
		}
		Hand hand = player.getActiveHand();
		Vec3d[] targets = impacts(center, user.getYaw());
		for (int i = 0; i < 3; i++) {
			Vec3d target = targets[i];
			double height = 32.0 + i * 14.0;
			Vec3d start = new Vec3d(target.x + 9.0 + i * 3.0, Math.min(target.y + height, 240.0), target.z - 7.0 + i * 5.0);
			Vec3d dir = target.subtract(start).normalize();
			MeteorEntity meteor = new MeteorEntity(world, player);
			meteor.setPosition(start.x, start.y, start.z);
			meteor.setDamage(34.0f);
			meteor.setBlastDamage(34.0f);
			meteor.setVelocity(dir.x, dir.y, dir.z, 1.8f, 0.0f);
			world.spawnEntity(meteor);
		}
		world.playSound(null, center.x, center.y, center.z, SoundEvents.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.PLAYERS, 1.5f, 0.6f);
		world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.PLAYERS, 1.5f, 0.5f);
		stack.damage(8, player, p -> p.sendToolBreakStatus(hand));
		player.getItemCooldownManager().set(this, 200);
	}
}
