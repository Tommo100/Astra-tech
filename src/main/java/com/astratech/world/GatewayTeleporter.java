package com.astratech.world;

import com.astratech.block.LunarPortalBlock;
import com.astratech.registry.ModBlocks;
import com.astratech.registry.ModWorlds;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Handles the "stand in the portal" timer, destination lookup and return-gateway creation. */
public final class GatewayTeleporter {
	private static final int CHARGE_TICKS = 40;
	private static final int COOLDOWN_TICKS = 100;
	private static final Map<UUID, Integer> FIRST_CONTACT = new HashMap<>();
	private static final Map<UUID, Integer> LAST_CONTACT = new HashMap<>();
	private static final Map<UUID, Integer> COOLDOWN_UNTIL = new HashMap<>();

	private GatewayTeleporter() {
	}

	public static void tickInsidePortal(ServerPlayerEntity player) {
		MinecraftServer server = player.getServer();
		if (server == null) {
			return;
		}
		int now = server.getTicks();
		UUID id = player.getUuid();
		if (COOLDOWN_UNTIL.getOrDefault(id, 0) > now) {
			LAST_CONTACT.put(id, now);
			return;
		}
		Integer last = LAST_CONTACT.get(id);
		if (last == null || now - last > 4) {
			FIRST_CONTACT.put(id, now);
		}
		LAST_CONTACT.put(id, now);
		if (now - FIRST_CONTACT.get(id) >= CHARGE_TICKS) {
			FIRST_CONTACT.remove(id);
			COOLDOWN_UNTIL.put(id, now + COOLDOWN_TICKS);
			travel(player);
		}
	}

	public static void travel(ServerPlayerEntity player) {
		MinecraftServer server = player.getServer();
		if (server == null) {
			return;
		}
		ServerWorld from = player.getServerWorld();
		ServerWorld to = from.getRegistryKey().equals(ModWorlds.MOON)
			? server.getWorld(World.OVERWORLD)
			: server.getWorld(ModWorlds.MOON);
		if (to == null) {
			player.sendMessage(Text.literal("The gateway fizzles - destination unavailable."), true);
			return;
		}
		int x = player.getBlockX();
		int z = player.getBlockZ();
		to.getChunk(x >> 4, z >> 4);

		double destX;
		double destY;
		double destZ;
		float yaw;
		BlockPos existing = findPortal(to, x, z);
		if (existing != null) {
			BlockState state = to.getBlockState(existing);
			Direction.Axis axis = state.get(LunarPortalBlock.AXIS);
			Direction side = axis == Direction.Axis.X ? Direction.SOUTH : Direction.EAST;
			BlockPos stand = existing.offset(side, 2);
			if (!to.getBlockState(stand).isAir() || !to.getBlockState(stand.up()).isAir()) {
				side = side.getOpposite();
				stand = existing.offset(side, 2);
			}
			destX = stand.getX() + 0.5;
			destY = stand.getY();
			destZ = stand.getZ() + 0.5;
			yaw = side.getOpposite().asRotation();
		} else {
			BlockPos base = buildReturnGateway(to, x, z);
			destX = base.getX() + 0.5;
			destY = base.getY() + 1;
			destZ = base.getZ() + 3.5;
			yaw = 180.0f;
		}
		from.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS, 1.0f, 1.6f);
		player.teleport(to, destX, destY, destZ, yaw, 0.0f);
		to.playSound(null, BlockPos.ofFloored(destX, destY, destZ), SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.PLAYERS, 1.0f, 1.2f);
	}

	private static BlockPos findPortal(ServerWorld world, int cx, int cz) {
		int minY = world.getBottomY();
		int maxY = world.getTopY();
		for (int dx = -6; dx <= 6; dx++) {
			for (int dz = -6; dz <= 6; dz++) {
				for (int y = maxY - 1; y >= minY; y--) {
					BlockPos pos = new BlockPos(cx + dx, y, cz + dz);
					if (world.getBlockState(pos).isOf(ModBlocks.LUNAR_PORTAL)) {
						// return the bottom block of this column of portal
						while (world.getBlockState(pos.down()).isOf(ModBlocks.LUNAR_PORTAL)) {
							pos = pos.down();
						}
						return pos;
					}
				}
			}
		}
		return null;
	}

	/** Builds a 7x7 landing pad with a working gateway on its north edge. Returns the pad centre (floor level). */
	public static BlockPos buildReturnGateway(ServerWorld world, int x, int z) {
		int floorY = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);
		floorY = Math.max(world.getBottomY() + 6, Math.min(floorY, world.getTopY() - 10));
		BlockState plating = ModBlocks.TECH_PLATING.getDefaultState();
		BlockState lamp = ModBlocks.PLASMA_LAMP.getDefaultState();
		BlockState frame = ModBlocks.GATEWAY_FRAME.getDefaultState();

		for (int dx = -3; dx <= 3; dx++) {
			for (int dz = -3; dz <= 3; dz++) {
				// supports
				for (int y = floorY - 1; y >= floorY - 8; y--) {
					BlockPos support = new BlockPos(x + dx, y, z + dz);
					BlockState s = world.getBlockState(support);
					if (s.isAir() || s.isLiquid() || s.isReplaceable()) {
						world.setBlockState(support, plating, 2);
					} else {
						break;
					}
				}
				// clear headroom
				for (int y = floorY + 1; y <= floorY + 6; y++) {
					world.setBlockState(new BlockPos(x + dx, y, z + dz), Blocks.AIR.getDefaultState(), 2);
				}
				boolean edge = Math.abs(dx) == 3 || Math.abs(dz) == 3;
				world.setBlockState(new BlockPos(x + dx, floorY, z + dz), edge && (dx + dz) % 2 == 0 ? lamp : plating, 2);
			}
		}
		// Gateway on the north edge (z - 2), interior at x..x+1, y+1..y+3, axis X.
		int gz = z - 2;
		for (int i = 0; i < 2; i++) {
			world.setBlockState(new BlockPos(x + i, floorY, gz), frame, 2);
		}
		for (int y = 1; y <= 3; y++) {
			world.setBlockState(new BlockPos(x - 1, floorY + y, gz), frame, 2);
			world.setBlockState(new BlockPos(x + 2, floorY + y, gz), frame, 2);
		}
		for (int i = 0; i < 2; i++) {
			world.setBlockState(new BlockPos(x + i, floorY + 4, gz), frame, 2);
		}
		BlockState portal = ModBlocks.LUNAR_PORTAL.getDefaultState().with(LunarPortalBlock.AXIS, Direction.Axis.X);
		for (int i = 0; i < 2; i++) {
			for (int y = 1; y <= 3; y++) {
				world.setBlockState(new BlockPos(x + i, floorY + y, gz), portal, 18);
			}
		}
		return new BlockPos(x, floorY, z);
	}
}
