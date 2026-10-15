package com.astratech.net;

import com.astratech.entity.NovaOvermindEntity;
import com.astratech.logic.ArmorAbilities;
import com.astratech.registry.ModItems;
import com.astratech.world.GatewayTeleporter;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

/** Server-side packet handlers: jetpack thrust and the Codex showcase. */
public final class ModNetworking {
	public static final Identifier THRUST = new Identifier("astratech", "thrust");
	public static final Identifier SHOWCASE = new Identifier("astratech", "showcase");

	private ModNetworking() {
	}

	public static boolean canUseShowcase(ServerPlayerEntity player) {
		MinecraftServer server = player.getServer();
		return (server != null && server.isSingleplayer()) || player.isCreative() || player.hasPermissionLevel(2);
	}

	public static void register() {
		ServerPlayNetworking.registerGlobalReceiver(THRUST, (server, player, handler, buf, responseSender) -> {
			server.execute(() -> {
				if (ArmorAbilities.hasChest(player) && !player.isOnGround()) {
					player.fallDistance = 0.0f;
					if (player.age % 40 == 0) {
						ItemStack chest = player.getEquippedStack(EquipmentSlot.CHEST);
						chest.damage(1, player, p -> p.sendEquipmentBreakStatus(EquipmentSlot.CHEST));
					}
				}
			});
		});

		ServerPlayNetworking.registerGlobalReceiver(SHOWCASE, (server, player, handler, buf, responseSender) -> {
			String action = buf.readString(32);
			String id = buf.readString(128);
			server.execute(() -> handleShowcase(player, action, id));
		});
	}

	private static void handleShowcase(ServerPlayerEntity player, String action, String idString) {
		if (!canUseShowcase(player)) {
			player.sendMessage(Text.literal("Showcase spawning needs Creative mode, operator permissions, or a single-player world."), false);
			return;
		}
		Identifier id = Identifier.tryParse(idString);
		switch (action) {
			case "item" -> {
				if (id == null || !id.getNamespace().equals("astratech") || id.getPath().equals("plasma_bolt") || !Registries.ITEM.containsId(id)) {
					return;
				}
				Item item = Registries.ITEM.get(id);
				int count = item.getMaxCount() > 1 ? Math.min(16, item.getMaxCount()) : 1;
				player.getInventory().offerOrDrop(new ItemStack(item, count));
			}
			case "mob" -> {
				if (id == null || !id.getNamespace().equals("astratech") || id.getPath().equals("plasma_bolt") || !Registries.ENTITY_TYPE.containsId(id)) {
					return;
				}
				EntityType<?> type = Registries.ENTITY_TYPE.get(id);
				ServerWorld world = player.getServerWorld();
				Entity entity = type.create(world);
				if (entity == null) {
					return;
				}
				Vec3d pos = player.getPos().add(player.getRotationVec(1.0f).multiply(1.0, 0.0, 1.0).normalize().multiply(5.0));
				entity.refreshPositionAndAngles(pos.x, player.getY() + 1.0, pos.z, player.getYaw() + 180.0f, 0.0f);
				if (entity instanceof NovaOvermindEntity boss) {
					boss.beginSummoning();
					boss.refreshPositionAndAngles(pos.x, player.getY() + 6.0, pos.z, 0.0f, 0.0f);
				}
				world.spawnEntity(entity);
			}
			case "warp" -> GatewayTeleporter.travel(player);
			case "codex" -> player.getInventory().offerOrDrop(new ItemStack(ModItems.ASTRA_CODEX));
			default -> {
			}
		}
	}
}
