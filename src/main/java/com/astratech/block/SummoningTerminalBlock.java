package com.astratech.block;

import com.astratech.entity.NovaOvermindEntity;
import com.astratech.registry.ModEntities;
import com.astratech.registry.ModItems;
import com.astratech.registry.ModWorlds;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

/** Place on the Moon, then right-click with a Quantum Core to summon the Nova Overmind. */
public class SummoningTerminalBlock extends Block {
	public SummoningTerminalBlock(Settings settings) {
		super(settings);
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		ItemStack held = player.getStackInHand(hand);
		if (!held.isOf(ModItems.QUANTUM_CORE)) {
			if (!world.isClient) {
				player.sendMessage(Text.literal("Terminal offline. Insert a Quantum Core to begin the summoning sequence."), true);
			}
			return ActionResult.success(world.isClient);
		}
		if (world.isClient) {
			return ActionResult.SUCCESS;
		}
		if (!world.getRegistryKey().equals(ModWorlds.MOON)) {
			player.sendMessage(Text.literal("The Overmind can only be summoned on the Moon. The core is unstable here."), true);
			return ActionResult.FAIL;
		}
		ServerWorld serverWorld = (ServerWorld) world;
		Box area = new Box(pos).expand(128.0);
		if (!serverWorld.getEntitiesByClass(NovaOvermindEntity.class, area, e -> e.isAlive()).isEmpty()) {
			player.sendMessage(Text.literal("An Overmind is already active nearby."), true);
			return ActionResult.FAIL;
		}
		if (!player.getAbilities().creativeMode) {
			held.decrement(1);
		}
		serverWorld.removeBlock(pos, false);
		NovaOvermindEntity boss = ModEntities.NOVA_OVERMIND.create(serverWorld);
		if (boss != null) {
			boss.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY() + 5.0, pos.getZ() + 0.5, 0.0f, 0.0f);
			boss.beginSummoning();
			serverWorld.spawnEntity(boss);
		}
		return ActionResult.CONSUME;
	}
}
