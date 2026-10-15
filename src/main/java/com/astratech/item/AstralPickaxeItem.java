package com.astratech.item;

import com.astratech.material.AstralToolMaterial;
import net.minecraft.item.PickaxeItem;

/** Quantum Pickaxe: sneak while mining ore to vein-mine the whole deposit (see ToolAbilities). */
public class AstralPickaxeItem extends PickaxeItem {
	public AstralPickaxeItem(Settings settings) {
		super(AstralToolMaterial.INSTANCE, 1, -2.4f, settings);
	}
}
