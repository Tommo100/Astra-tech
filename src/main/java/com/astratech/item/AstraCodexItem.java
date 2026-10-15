package com.astratech.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

/** The Astra Codex: opens the in-game guide and showcase. The GUI itself is client-only. */
public class AstraCodexItem extends Item {
	/** Set by the client initializer so this class never references client-only code. */
	public static Runnable clientOpener = null;

	public AstraCodexItem(Settings settings) {
		super(settings);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		if (world.isClient && clientOpener != null) {
			clientOpener.run();
		}
		return TypedActionResult.success(player.getStackInHand(hand), world.isClient);
	}
}
