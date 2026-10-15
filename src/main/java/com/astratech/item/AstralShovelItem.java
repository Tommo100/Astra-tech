package com.astratech.item;

import com.astratech.material.AstralToolMaterial;
import net.minecraft.item.ShovelItem;

/** Excavator Spade: sneak while digging to clear a 3x3 area (see ToolAbilities). */
public class AstralShovelItem extends ShovelItem {
	public AstralShovelItem(Settings settings) {
		super(AstralToolMaterial.INSTANCE, 1.5f, -3.0f, settings);
	}
}
