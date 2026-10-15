package com.astratech.logic;

import com.astratech.registry.ModItems;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

/**
 * Area tools: pickaxe, spade and axe break a 3x3 area, but only blocks their tool type can mine.
 * Sneak for single-block precision. Sneaking with the pickaxe on ore vein-mines it (up to 48 blocks).
 */
public final class ToolAbilities {
	public static final TagKey<Block> ORES = TagKey.of(RegistryKeys.BLOCK, new Identifier("c", "ores"));
	private static final int MAX_VEIN = 48;

	private ToolAbilities() {
	}

	public static void register() {
		PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
			if (world.isClient || !(player instanceof ServerPlayerEntity sp)) {
				return;
			}
			ItemStack tool = sp.getMainHandStack();
			ServerWorld sw = (ServerWorld) world;
			if (tool.isOf(ModItems.ASTRAL_PICKAXE)) {
				if (sp.isSneaking() && state.isIn(ORES)) {
					veinMine(sw, sp, pos, state, tool);
				} else if (!sp.isSneaking() && state.isIn(BlockTags.PICKAXE_MINEABLE)) {
					clearArea(sw, sp, pos, tool, BlockTags.PICKAXE_MINEABLE);
				}
			} else if (!sp.isSneaking()) {
				if (tool.isOf(ModItems.ASTRAL_SHOVEL) && state.isIn(BlockTags.SHOVEL_MINEABLE)) {
					clearArea(sw, sp, pos, tool, BlockTags.SHOVEL_MINEABLE);
				} else if (tool.isOf(ModItems.ASTRAL_AXE) && state.isIn(BlockTags.AXE_MINEABLE)) {
					clearArea(sw, sp, pos, tool, BlockTags.AXE_MINEABLE);
				}
			}
		});
	}

	private static boolean breakExtra(ServerWorld world, ServerPlayerEntity player, BlockPos pos, ItemStack tool) {
		BlockState state = world.getBlockState(pos);
		if (state.isAir() || state.getHardness(world, pos) < 0.0f) {
			return false;
		}
		BlockEntity blockEntity = world.getBlockEntity(pos);
		world.breakBlock(pos, false, player);
		Block.dropStacks(state, world, pos, blockEntity, player, tool);
		state.onStacksDropped(world, pos, tool, true);
		tool.damage(1, player, p -> p.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
		return !tool.isEmpty();
	}

	private static void veinMine(ServerWorld world, ServerPlayerEntity player, BlockPos origin, BlockState state, ItemStack tool) {
		Set<BlockPos> seen = new HashSet<>();
		ArrayDeque<BlockPos> queue = new ArrayDeque<>();
		queue.add(origin);
		seen.add(origin);
		int broken = 0;
		while (!queue.isEmpty() && broken < MAX_VEIN) {
			BlockPos current = queue.poll();
			for (int dx = -1; dx <= 1; dx++) {
				for (int dy = -1; dy <= 1; dy++) {
					for (int dz = -1; dz <= 1; dz++) {
						BlockPos next = current.add(dx, dy, dz);
						if (!seen.add(next) || broken >= MAX_VEIN) {
							continue;
						}
						if (world.getBlockState(next).isOf(state.getBlock())) {
							if (!breakExtra(world, player, next, tool)) {
								return;
							}
							broken++;
							queue.add(next);
						}
					}
				}
			}
		}
	}

	private static void clearArea(ServerWorld world, ServerPlayerEntity player, BlockPos origin, ItemStack tool, TagKey<Block> mineable) {
		Direction face = Direction.getEntityFacingOrder(player)[0];
		Direction.Axis axis = face.getAxis();
		for (int a = -1; a <= 1; a++) {
			for (int b = -1; b <= 1; b++) {
				if (a == 0 && b == 0) {
					continue;
				}
				BlockPos target;
				if (axis == Direction.Axis.Y) {
					target = origin.add(a, 0, b);
				} else if (axis == Direction.Axis.X) {
					target = origin.add(0, a, b);
				} else {
					target = origin.add(a, b, 0);
				}
				if (world.getBlockState(target).isIn(mineable)) {
					if (!breakExtra(world, player, target, tool)) {
						return;
					}
				}
			}
		}
	}
}
