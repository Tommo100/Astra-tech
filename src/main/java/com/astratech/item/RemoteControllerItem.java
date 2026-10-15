package com.astratech.item;

import com.astratech.entity.AstroBotEntity;
import com.astratech.registry.ModEntities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

/**
 * Companion Remote: deploys a personal Astro Bot that follows and fights for you.
 * Use: deploy, or recall your bot to your side if it is already out. Sneak + use: dismiss it.
 */
public class RemoteControllerItem extends Item {
	public RemoteControllerItem(Settings settings) {
		super(settings);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		ItemStack stack = player.getStackInHand(hand);
		if (world.isClient || !(world instanceof ServerWorld serverWorld)) {
			return TypedActionResult.success(stack, world.isClient);
		}
		MinecraftServer server = serverWorld.getServer();
		List<AstroBotEntity> mine = new ArrayList<>();
		for (ServerWorld w : server.getWorlds()) {
			mine.addAll(w.getEntitiesByType(ModEntities.ASTRO_BOT, e -> player.getUuid().equals(e.getOwnerUuid())));
		}

		if (player.isSneaking()) {
			if (mine.isEmpty()) {
				player.sendMessage(Text.literal("No companion is deployed."), true);
			} else {
				for (AstroBotEntity bot : mine) {
					if (bot.getWorld() instanceof ServerWorld botWorld) {
						botWorld.spawnParticles(ParticleTypes.POOF, bot.getX(), bot.getBodyY(0.5), bot.getZ(), 12, 0.3, 0.5, 0.3, 0.02);
					}
					bot.discard();
				}
				player.sendMessage(Text.literal("Companion dismissed."), true);
			}
			player.getItemCooldownManager().set(this, 20);
			return TypedActionResult.success(stack, false);
		}

		// Recall an existing bot in this world.
		for (AstroBotEntity bot : mine) {
			if (bot.getWorld() == serverWorld && bot.isAlive()) {
				placeBeside(bot, player);
				bot.getNavigation().stop();
				serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK, bot.getX(), bot.getBodyY(0.5), bot.getZ(), 20, 0.4, 0.5, 0.4, 0.3);
				serverWorld.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 0.6f, 1.8f);
				player.sendMessage(Text.literal("Companion recalled."), true);
				player.getItemCooldownManager().set(this, 20);
				return TypedActionResult.success(stack, false);
			}
		}

		// Otherwise (re)deploy a fresh one, removing strays in other dimensions.
		for (AstroBotEntity bot : mine) {
			bot.discard();
		}
		AstroBotEntity bot = ModEntities.ASTRO_BOT.create(serverWorld);
		if (bot == null) {
			return TypedActionResult.fail(stack);
		}
		bot.setOwner(player);
		placeBeside(bot, player);
		serverWorld.spawnEntity(bot);
		serverWorld.spawnParticles(ParticleTypes.END_ROD, bot.getX(), bot.getBodyY(0.5), bot.getZ(), 30, 0.5, 0.8, 0.5, 0.05);
		serverWorld.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS, 0.8f, 1.6f);
		player.sendMessage(Text.literal("Astro Bot deployed. Use again to recall, sneak + use to dismiss."), true);
		player.getItemCooldownManager().set(this, 20);
		return TypedActionResult.success(stack, false);
	}

	private static void placeBeside(AstroBotEntity bot, PlayerEntity player) {
		Vec3d look = player.getRotationVec(1.0f).multiply(1.0, 0.0, 1.0);
		if (look.lengthSquared() < 1.0e-4) {
			look = new Vec3d(0.0, 0.0, 1.0);
		}
		Vec3d pos = player.getPos().add(look.normalize().multiply(2.0));
		bot.refreshPositionAndAngles(pos.x, player.getY(), pos.z, player.getYaw() + 180.0f, 0.0f);
	}
}
