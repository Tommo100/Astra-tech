package com.astratech.client;

import com.astratech.client.model.AstroBotModel;
import com.astratech.client.model.LunarHopperModel;
import com.astratech.client.model.ModModelLayers;
import com.astratech.client.model.NovaOvermindModel;
import com.astratech.client.model.PlasmaDroneModel;
import com.astratech.client.render.AstroBotRenderer;
import com.astratech.client.render.LunarHopperRenderer;
import com.astratech.client.render.LunarSentinelRenderer;
import com.astratech.client.render.NovaOvermindRenderer;
import com.astratech.client.render.PlasmaDroneRenderer;
import com.astratech.client.render.VoidStalkerRenderer;
import com.astratech.client.screen.CodexScreen;
import com.astratech.client.sky.MoonEffects;
import com.astratech.client.sky.MoonSky;
import com.astratech.item.AstraCodexItem;
import com.astratech.logic.ArmorAbilities;
import com.astratech.net.ModNetworking;
import com.astratech.registry.ModBlocks;
import com.astratech.registry.ModEntities;
import com.astratech.registry.ModWorlds;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.DimensionRenderingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class AstraTechClient implements ClientModInitializer {
	private static KeyBinding boostKey;

	@Override
	public void onInitializeClient() {
		// Model layers
		EntityModelLayerRegistry.registerModelLayer(ModModelLayers.PLASMA_DRONE, PlasmaDroneModel::getTexturedModelData);
		EntityModelLayerRegistry.registerModelLayer(ModModelLayers.ASTRO_BOT, AstroBotModel::getTexturedModelData);
		EntityModelLayerRegistry.registerModelLayer(ModModelLayers.LUNAR_HOPPER, LunarHopperModel::getTexturedModelData);
		EntityModelLayerRegistry.registerModelLayer(ModModelLayers.ASTRAL_GEAR, com.astratech.client.model.AstralGearModel::getTexturedModelData);
		net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) -> {
			if (entityRenderer instanceof net.minecraft.client.render.entity.PlayerEntityRenderer || entityRenderer instanceof net.minecraft.client.render.entity.ArmorStandEntityRenderer) {
				registerGear(entityRenderer, registrationHelper, context);
			}
		});
		EntityModelLayerRegistry.registerModelLayer(ModModelLayers.NOVA_OVERMIND, NovaOvermindModel::getTexturedModelData);

		// Entity renderers
		EntityRendererRegistry.register(ModEntities.LUNAR_SENTINEL, LunarSentinelRenderer::new);
		EntityRendererRegistry.register(ModEntities.VOID_STALKER, VoidStalkerRenderer::new);
		EntityRendererRegistry.register(ModEntities.PLASMA_DRONE, PlasmaDroneRenderer::new);
		EntityRendererRegistry.register(ModEntities.ASTRO_BOT, AstroBotRenderer::new);
		EntityRendererRegistry.register(ModEntities.LUNAR_HOPPER, LunarHopperRenderer::new);
		EntityRendererRegistry.register(ModEntities.NOVA_OVERMIND, NovaOvermindRenderer::new);
		EntityRendererRegistry.register(ModEntities.METEOR, context -> new FlyingItemEntityRenderer<>(context, 5.0f, true));
		EntityRendererRegistry.register(ModEntities.THROWN_NOVA_CORE, context -> new FlyingItemEntityRenderer<>(context, 1.4f, true));
		EntityRendererRegistry.register(ModEntities.BLACK_HOLE, net.minecraft.client.render.entity.EmptyEntityRenderer::new);
		EntityRendererRegistry.register(ModEntities.PLASMA_ORB, context -> new FlyingItemEntityRenderer<>(context, 4.5f, true));
		EntityRendererRegistry.register(ModEntities.PLASMA_BOLT, context -> new FlyingItemEntityRenderer<>(context, 1.6f, true));

		// Blocks
		BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.LUNAR_PORTAL, RenderLayer.getTranslucent());
		for (net.minecraft.block.Block crystalBud : new net.minecraft.block.Block[]{ModBlocks.SMALL_PLASMA_BUD, ModBlocks.MEDIUM_PLASMA_BUD, ModBlocks.LARGE_PLASMA_BUD, ModBlocks.PLASMA_CRYSTAL_CLUSTER}) {
			BlockRenderLayerMap.INSTANCE.putBlock(crystalBud, RenderLayer.getCutout());
		}
		BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.GRAY_LUNAR_BERRY_BUSH, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.PURPLE_LUNAR_BERRY_BUSH, RenderLayer.getCutout());

		// Moon sky
		DimensionRenderingRegistry.registerDimensionEffects(new Identifier("astratech", "moon"), new MoonEffects());
		DimensionRenderingRegistry.registerSkyRenderer(ModWorlds.MOON, new MoonSky());
		DimensionRenderingRegistry.registerCloudRenderer(ModWorlds.MOON, context -> {
		});
		DimensionRenderingRegistry.registerWeatherRenderer(ModWorlds.MOON, context -> {
		});

		// Codex
		AstraCodexItem.clientOpener = () -> MinecraftClient.getInstance().setScreen(new CodexScreen());

		// Forward boost key (default R): hold to fly where you look.
		boostKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.astratech.boost", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, "key.categories.astratech"));

		// Ion thrusters: hold jump in the air to climb (sneak + jump hovers); hold the boost key to thrust forward.
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			ClientPlayerEntity player = client.player;
			if (player == null || client.isPaused() || client.world == null) {
				return;
			}
			if (ArmorAbilities.hasChest(player) && client.options.jumpKey.isPressed() && !player.isOnGround()
				&& !player.isTouchingWater() && !player.getAbilities().flying && !player.hasVehicle() && !player.isFallFlying()) {
				Vec3d velocity = player.getVelocity();
				double vy = player.isSneaking() ? 0.0 : Math.min(velocity.y + 0.1, 0.42);
				player.setVelocity(velocity.x, vy, velocity.z);
				player.fallDistance = 0.0f;
				double yawRad = Math.toRadians(player.getYaw());
				double sideX = -Math.cos(yawRad) * 0.22;
				double sideZ = -Math.sin(yawRad) * 0.22;
				for (int side = -1; side <= 1; side += 2) {
					client.world.addParticle(ParticleTypes.END_ROD, player.getX() + sideX * side, player.getY() + 0.8, player.getZ() + sideZ * side, 0.0, -0.25, 0.0);
				}
				client.world.addParticle(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + 0.2, player.getZ(), 0.0, -0.2, 0.0);
				ClientPlayNetworking.send(ModNetworking.THRUST, PacketByteBufs.create());
			}
			if (ArmorAbilities.hasChest(player) && boostKey.isPressed() && !player.isTouchingWater() && !player.getAbilities().flying
				&& !player.hasVehicle() && !player.isFallFlying()) {
				Vec3d look = player.getRotationVec(1.0f);
				Vec3d velocity = player.getVelocity().add(look.multiply(0.16));
				if (player.isOnGround()) {
					velocity = velocity.add(0.0, 0.2, 0.0);
				}
				double max = 1.5;
				if (velocity.length() > max) {
					velocity = velocity.normalize().multiply(max);
				}
				player.setVelocity(velocity);
				player.fallDistance = 0.0f;
				Vec3d back = look.multiply(-0.6);
				for (int i = 0; i < 2; i++) {
					client.world.addParticle(ParticleTypes.END_ROD, player.getX() + back.x + (player.getRandom().nextDouble() - 0.5) * 0.3,
						player.getY() + 1.0 + back.y, player.getZ() + back.z + (player.getRandom().nextDouble() - 0.5) * 0.3, back.x * 0.5, back.y * 0.5, back.z * 0.5);
				}
				client.world.addParticle(ParticleTypes.SOUL_FIRE_FLAME, player.getX() + back.x, player.getY() + 0.9 + back.y, player.getZ() + back.z, back.x * 0.3, back.y * 0.3, back.z * 0.3);
				ClientPlayNetworking.send(ModNetworking.THRUST, PacketByteBufs.create());
			}
		});
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static void registerGear(net.minecraft.client.render.entity.LivingEntityRenderer renderer,
			net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback.RegistrationHelper helper,
			net.minecraft.client.render.entity.EntityRendererFactory.Context context) {
		helper.register(new com.astratech.client.render.AstralGearFeature(renderer, context.getModelLoader().getModelPart(ModModelLayers.ASTRAL_GEAR)));
	}
}
