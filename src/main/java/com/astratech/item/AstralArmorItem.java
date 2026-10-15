package com.astratech.item;

import com.astratech.material.AstralArmorMaterial;
import net.minecraft.item.ArmorItem;

/** Astral Vanguard armour piece. Bonus abilities are applied in ArmorAbilities. */
public class AstralArmorItem extends ArmorItem {
	public AstralArmorItem(Type type, Settings settings) {
		super(AstralArmorMaterial.INSTANCE, type, settings);
	}
}
