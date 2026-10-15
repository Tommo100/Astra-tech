package com.astratech.item;

import com.astratech.material.AstralToolMaterial;
import net.minecraft.item.HoeItem;

/** Terraform Hoe: end-game stats, fastest crop and foliage harvesting. */
public class AstralHoeItem extends HoeItem {
	public AstralHoeItem(Settings settings) {
		super(AstralToolMaterial.INSTANCE, -5, 0.0f, settings);
	}
}
