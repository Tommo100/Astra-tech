package com.astratech.material;

import com.astratech.registry.ModItems;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;

/** End-game tool tier: stronger than netherite. */
public class AstralToolMaterial implements ToolMaterial {
	public static final AstralToolMaterial INSTANCE = new AstralToolMaterial();

	@Override
	public int getDurability() {
		return 4200;
	}

	@Override
	public float getMiningSpeedMultiplier() {
		return 14.0f;
	}

	@Override
	public float getAttackDamage() {
		return 7.0f;
	}

	@Override
	public int getMiningLevel() {
		return 5;
	}

	@Override
	public int getEnchantability() {
		return 22;
	}

	@Override
	public Ingredient getRepairIngredient() {
		return Ingredient.ofItems(ModItems.ASTRIUM_INGOT);
	}
}
