package com.astratech.item;

import com.astratech.material.AstralToolMaterial;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.Optional;

/** Plasma Blade: arc damage + slow on hit, right-click fires the Photon Lance, a piercing laser beam. */
public class AstralSwordItem extends SwordItem {
	private static final double RANGE = 36.0;
	private static final float BEAM_DAMAGE = 24.0f;

	public AstralSwordItem(Settings settings) {
		super(AstralToolMaterial.INSTANCE, 3, -2.2f, settings);
	}

	@Override
	public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		boolean result = super.postHit(stack, target, attacker);
		target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 2));
		if (attacker instanceof PlayerEntity player) {
			target.damage(attacker.getDamageSources().indirectMagic(attacker, attacker), 4.0f);
		}
		return result;
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		ItemStack stack = player.getStackInHand(hand);
		if (player.getItemCooldownManager().isCoolingDown(this)) {
			return TypedActionResult.pass(stack);
		}
		player.swingHand(hand);
		if (!world.isClient && world instanceof ServerWorld serverWorld) {
			fireLance(serverWorld, player);
			stack.damage(3, player, p -> p.sendToolBreakStatus(hand));
		}
		player.getItemCooldownManager().set(this, 50);
		return TypedActionResult.success(stack, world.isClient);
	}

	private static void fireLance(ServerWorld world, PlayerEntity player) {
		Vec3d start = player.getEyePos().add(0.0, -0.15, 0.0);
		Vec3d dir = player.getRotationVec(1.0f).normalize();
		Vec3d far = start.add(dir.multiply(RANGE));
		BlockHitResult blockHit = world.raycast(new RaycastContext(start, far, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, player));
		double length = blockHit.getType() == HitResult.Type.MISS ? RANGE : start.distanceTo(blockHit.getPos());
		Vec3d end = start.add(dir.multiply(length));

		// Damage everything the beam passes through.
		Box area = new Box(start, end).expand(1.5);
		for (LivingEntity target : world.getEntitiesByClass(LivingEntity.class, area, e -> e != player && e.isAlive() && !e.isTeammate(player))) {
			Optional<Vec3d> hit = target.getBoundingBox().expand(0.5).raycast(start, end);
			if (hit.isPresent()) {
				target.damage(player.getDamageSources().playerAttack(player), BEAM_DAMAGE);
				target.setOnFireFor(4);
				target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 1));
				world.spawnParticles(ParticleTypes.FLASH, hit.get().x, hit.get().y, hit.get().z, 1, 0.0, 0.0, 0.0, 0.0);
				world.spawnParticles(ParticleTypes.ELECTRIC_SPARK, hit.get().x, hit.get().y, hit.get().z, 20, 0.3, 0.3, 0.3, 0.4);
			}
		}

		// Beam visuals: bright core, soul-fire halo and a counter-rotating double helix.
		Vec3d up = Math.abs(dir.y) > 0.98 ? new Vec3d(1.0, 0.0, 0.0) : new Vec3d(0.0, 1.0, 0.0);
		Vec3d a = dir.crossProduct(up).normalize();
		Vec3d b = dir.crossProduct(a).normalize();
		for (double t = 0.8; t < length; t += 0.35) {
			Vec3d p = start.add(dir.multiply(t));
			world.spawnParticles(ParticleTypes.END_ROD, p.x, p.y, p.z, 1, 0.0, 0.0, 0.0, 0.0);
			if (((int) (t / 0.35)) % 2 == 0) {
				world.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, p.x, p.y, p.z, 1, 0.08, 0.08, 0.08, 0.0);
			}
			double ang = t * 2.2;
			for (int strand = 0; strand < 2; strand++) {
				double phase = ang + strand * Math.PI;
				Vec3d h = p.add(a.multiply(Math.cos(phase) * 0.45)).add(b.multiply(Math.sin(phase) * 0.45));
				world.spawnParticles(strand == 0 ? ParticleTypes.DRAGON_BREATH : ParticleTypes.ELECTRIC_SPARK, h.x, h.y, h.z, 1, 0.0, 0.0, 0.0, 0.0);
			}
		}
		world.spawnParticles(ParticleTypes.FLASH, end.x, end.y, end.z, 1, 0.0, 0.0, 0.0, 0.0);
		world.spawnParticles(ParticleTypes.EXPLOSION, end.x, end.y, end.z, 2, 0.4, 0.4, 0.4, 0.0);
		world.spawnParticles(ParticleTypes.ELECTRIC_SPARK, end.x, end.y, end.z, 30, 0.5, 0.5, 0.5, 0.5);
		world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.PLAYERS, 0.8f, 1.6f);
		world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.PLAYERS, 1.0f, 2.0f);
	}
}
