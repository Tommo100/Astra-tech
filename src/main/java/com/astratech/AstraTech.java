package com.astratech;

import com.astratech.logic.ArmorAbilities;
import com.astratech.logic.ToolAbilities;
import com.astratech.net.ModNetworking;
import com.astratech.registry.ModBlocks;
import com.astratech.registry.ModEntities;
import com.astratech.registry.ModFeatures;
import com.astratech.registry.ModItems;
import com.astratech.world.GatewayTeleporter;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AstraTech implements ModInitializer {
	public static final String MOD_ID = "astratech";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static final String CODEX_TAG = "astratech_codex_given";

	@Override
	public void onInitialize() {
		ModBlocks.init();
		ModEntities.init();
		ModItems.init();
		ModFeatures.init();
		ModEntities.registerAttributes();
		ToolAbilities.register();
		ModNetworking.register();

		// Armour bonuses and moon low-gravity
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
				ArmorAbilities.tick(player);
			}
		});

		// Every player receives the Astra Codex the first time they spawn in
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayerEntity player = handler.getPlayer();
			if (!player.getCommandTags().contains(CODEX_TAG)) {
				player.addCommandTag(CODEX_TAG);
				player.getInventory().offerOrDrop(new ItemStack(ModItems.ASTRA_CODEX));
				player.sendMessage(Text.literal("Astra Tech: you have been given the Astra Codex. Right-click it to explore the mod."), false);
			}
		});

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
			dispatcher.register(CommandManager.literal("astratech")
				.then(CommandManager.literal("codex").executes(ctx -> {
					ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
					player.getInventory().offerOrDrop(new ItemStack(ModItems.ASTRA_CODEX));
					return 1;
				}))
				.then(CommandManager.literal("moon").requires(src -> src.hasPermissionLevel(2)).executes(ctx -> {
					GatewayTeleporter.travel(ctx.getSource().getPlayerOrThrow());
					return 1;
				}))));

		LOGGER.info("Astra Tech loaded - the Moon awaits.");
	}
}
