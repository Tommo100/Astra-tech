package com.astratech.registry;

import com.astratech.item.AstraCodexItem;
import com.astratech.item.AstralArmorItem;
import com.astratech.item.AstralAxeItem;
import com.astratech.item.AstralHoeItem;
import com.astratech.item.AstralPickaxeItem;
import com.astratech.item.AstralShovelItem;
import com.astratech.item.AstralSwordItem;
import com.astratech.item.IonIgniterItem;
import com.astratech.item.PulseBlasterItem;
import com.astratech.item.RemoteControllerItem;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ModItems {
	/** Everything shown in the creative tab and in the Codex showcase, in display order. */
	public static final List<Item> SHOWCASE_ITEMS = new ArrayList<>();
	/** Spawn eggs by entity name, used by the Codex showcase. */
	public static final Map<String, Item> SPAWN_EGGS = new LinkedHashMap<>();

	// Materials
	public static final Item RAW_ASTRIUM = item("raw_astrium", new Item(new Item.Settings()));
	public static final Item PLASMA_ORB = Registry.register(Registries.ITEM, new Identifier("astratech", "plasma_orb"), new Item(new Item.Settings()));
	public static final Item ASTRIUM_INGOT = item("astrium_ingot", new Item(new Item.Settings()));
	public static final Item PLASMA_CRYSTAL = item("plasma_crystal", new Item(new Item.Settings()));
	public static final Item NOVA_CORE = item("nova_core", new Item(new Item.Settings().rarity(Rarity.EPIC).fireproof()));
	public static final Item QUANTUM_CORE = item("quantum_core", new Item(new Item.Settings().rarity(Rarity.RARE)));

	// Utility
	public static final Item ION_IGNITER = item("ion_igniter", new IonIgniterItem(new Item.Settings().maxCount(1).maxDamage(64)));
	public static final Item ASTRA_CODEX = item("astra_codex", new AstraCodexItem(new Item.Settings().maxCount(1).rarity(Rarity.EPIC)));

	public static final Item REMOTE_CONTROLLER = item("remote_controller", new RemoteControllerItem(new Item.Settings().maxCount(1).rarity(Rarity.RARE)));

	// Weapons and tools
	public static final Item ASTRAL_BLADE = item("astral_blade", new AstralSwordItem(new Item.Settings().rarity(Rarity.EPIC).fireproof()));
	public static final Item PULSE_BLASTER = item("pulse_blaster", new PulseBlasterItem(new Item.Settings().maxCount(1).maxDamage(2400).rarity(Rarity.EPIC).fireproof()));
	public static final Item ASTRAL_PICKAXE = item("astral_pickaxe", new AstralPickaxeItem(new Item.Settings().rarity(Rarity.EPIC).fireproof()));
	public static final Item ASTRAL_AXE = item("astral_axe", new AstralAxeItem(new Item.Settings().rarity(Rarity.EPIC).fireproof()));
	public static final Item ASTRAL_SHOVEL = item("astral_shovel", new AstralShovelItem(new Item.Settings().rarity(Rarity.EPIC).fireproof()));
	public static final Item ASTRAL_HOE = item("astral_hoe", new AstralHoeItem(new Item.Settings().rarity(Rarity.EPIC).fireproof()));

	// Armour
	public static final Item ASTRAL_HELMET = item("astral_helmet", new AstralArmorItem(ArmorItem.Type.HELMET, new Item.Settings().rarity(Rarity.EPIC).fireproof()));
	public static final Item ASTRAL_CHESTPLATE = item("astral_chestplate", new AstralArmorItem(ArmorItem.Type.CHESTPLATE, new Item.Settings().rarity(Rarity.EPIC).fireproof()));
	public static final Item ASTRAL_LEGGINGS = item("astral_leggings", new AstralArmorItem(ArmorItem.Type.LEGGINGS, new Item.Settings().rarity(Rarity.EPIC).fireproof()));
	public static final Item ASTRAL_BOOTS = item("astral_boots", new AstralArmorItem(ArmorItem.Type.BOOTS, new Item.Settings().rarity(Rarity.EPIC).fireproof()));

	// Spawn eggs
	public static final Item LUNAR_SENTINEL_SPAWN_EGG = egg("lunar_sentinel", new SpawnEggItem(ModEntities.LUNAR_SENTINEL, 0x3b4a5a, 0x00e5ff, new Item.Settings()));
	public static final Item VOID_STALKER_SPAWN_EGG = egg("void_stalker", new SpawnEggItem(ModEntities.VOID_STALKER, 0x1a1a2e, 0xb400ff, new Item.Settings()));
	public static final Item PLASMA_DRONE_SPAWN_EGG = egg("plasma_drone", new SpawnEggItem(ModEntities.PLASMA_DRONE, 0x9aa7b5, 0xff3d6e, new Item.Settings()));
	public static final Item ASTRO_BOT_SPAWN_EGG = egg("astro_bot", new SpawnEggItem(ModEntities.ASTRO_BOT, 0xeaf2f8, 0x37e07a, new Item.Settings()));
	public static final Item LUNAR_HOPPER_SPAWN_EGG = egg("lunar_hopper", new SpawnEggItem(ModEntities.LUNAR_HOPPER, 0xc9ccd1, 0x7fd8ff, new Item.Settings()));
	public static final Item NOVA_OVERMIND_SPAWN_EGG = egg("nova_overmind", new SpawnEggItem(ModEntities.NOVA_OVERMIND, 0x0a0f24, 0x00e5ff, new Item.Settings().rarity(Rarity.EPIC)));

	// Hidden projectile item (renders the plasma bolt entity)
	public static final Item PLASMA_BOLT = Registry.register(Registries.ITEM, new Identifier("astratech", "plasma_bolt"), new Item(new Item.Settings()));

	public static final ItemGroup GROUP = FabricItemGroup.builder()
		.icon(() -> new ItemStack(ASTRA_CODEX))
		.displayName(Text.translatable("itemGroup.astratech"))
		.entries((context, entries) -> {
			for (Item item : SHOWCASE_ITEMS) {
				entries.add(item);
			}
			entries.add(ModBlocks.LUNAR_REGOLITH);
			entries.add(ModBlocks.LUNAR_STONE);
			entries.add(ModBlocks.ASTRIUM_ORE);
			entries.add(ModBlocks.PLASMA_CRYSTAL_ORE);
			entries.add(ModBlocks.ASTRIUM_BLOCK);
			entries.add(ModBlocks.TECH_PLATING);
			entries.add(ModBlocks.PLASMA_LAMP);
			entries.add(ModBlocks.GATEWAY_FRAME);
			entries.add(ModBlocks.SUMMONING_TERMINAL);
		})
		.build();

	private ModItems() {
	}

	private static Item item(String name, Item item) {
		Registry.register(Registries.ITEM, new Identifier("astratech", name), item);
		SHOWCASE_ITEMS.add(item);
		return item;
	}

	private static Item egg(String entityName, Item item) {
		Registry.register(Registries.ITEM, new Identifier("astratech", entityName + "_spawn_egg"), item);
		SPAWN_EGGS.put(entityName, item);
		SHOWCASE_ITEMS.add(item);
		return item;
	}

	public static void init() {
		Registry.register(Registries.ITEM_GROUP, new Identifier("astratech", "main"), GROUP);
	}
}
