package com.astratech.item;

import com.astratech.world.GatewayShape;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import com.astratech.registry.ModBlocks;

/** Right-click a Gateway Frame (from the inside face) to energise the portal. */
public class IonIgniterItem extends Item {
	public IonIgniterItem(Settings settings) {
		super(settings);
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		World world = context.getWorld();
		BlockPos clicked = context.getBlockPos();
		BlockState state = world.getBlockState(clicked);
		if (!state.isOf(ModBlocks.GATEWAY_FRAME)) {
			return ActionResult.PASS;
		}
		BlockPos start = clicked.offset(context.getSide());
		GatewayShape shape = GatewayShape.findAny(world, start);
		if (shape == null) {
			if (!world.isClient && context.getPlayer() != null) {
				context.getPlayer().sendMessage(Text.literal("Gateway frame incomplete: build a closed rectangle at least 2 wide and 3 tall (corners optional), then ignite from the inside."), true);
			}
			return ActionResult.FAIL;
		}
		if (!world.isClient) {
			shape.fill(world);
			world.playSound(null, clicked, SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.BLOCKS, 1.0f, 1.4f);
			ItemStack stack = context.getStack();
			if (context.getPlayer() != null) {
				stack.damage(1, context.getPlayer(), p -> p.sendToolBreakStatus(context.getHand()));
			}
		}
		return ActionResult.success(world.isClient);
	}
}
