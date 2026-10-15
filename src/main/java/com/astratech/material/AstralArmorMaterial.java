package com.astratech.material;

import com.astratech.registry.ModItems;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

/** End-game armour tier: stronger than netherite. */
public class AstralArmorMaterial implements ArmorMaterial {
	public static final AstralArmorMaterial INSTANCE = new AstralArmorMaterial();

	private static final int[] BASE_DURABILITY = {13, 15, 16, 11};
	private static final int[] PROTECTION = {4, 10, 7, 4};
	private static final int DURABILITY_MULTIPLIER = 60;

	@Override
	public int getDurability(ArmorItem.Type type) {
		return BASE_DURABILITY[type.ordinal()] * DURABILITY_MULTIPLIER;
	}

	@Override
	public int getProtection(ArmorItem.Type type) {
		return PROTECTION[type.ordinal()];
	}

	@Override
	public int getEnchantability() {
		return 24;
	}

	@Override
	public SoundEvent getEquipSound() {
		return SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE;
	}

	@Override
	public Ingredient getRepairIngredient() {
		return Ingredient.ofItems(ModItems.ASTRIUM_INGOT);
	}

	/** Textures live at assets/minecraft/textures/models/armor/astratech_astral_layer_{1,2}.png */
	@Override
	public String getName() {
		return "astratech_astral";
	}

	@Override
	public float getToughness() {
		return 4.0f;
	}

	@Override
	public float getKnockbackResistance() {
		return 0.15f;
	}
}
