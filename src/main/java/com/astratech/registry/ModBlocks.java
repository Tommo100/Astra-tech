package com.astratech.registry;

import com.astratech.block.LunarPortalBlock;
import com.astratech.block.SummoningTerminalBlock;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public final class ModBlocks {
	public static final Block LUNAR_REGOLITH = register("lunar_regolith",
		new Block(FabricBlockSettings.create().mapColor(MapColor.STONE_GRAY).strength(1.0f).sounds(BlockSoundGroup.SAND)));
	public static final Block LUNAR_STONE = register("lunar_stone",
		new Block(FabricBlockSettings.create().mapColor(MapColor.STONE_GRAY).strength(3.0f, 8.0f).requiresTool()));
	public static final Block ASTRIUM_ORE = register("astrium_ore",
		new Block(FabricBlockSettings.create().mapColor(MapColor.STONE_GRAY).strength(6.0f, 9.0f).requiresTool()));
	public static final Block PLASMA_CRYSTAL_ORE = register("plasma_crystal_ore",
		new Block(FabricBlockSettings.create().mapColor(MapColor.STONE_GRAY).strength(6.0f, 9.0f).requiresTool().luminance(s -> 9)));
	public static final Block ASTRIUM_BLOCK = register("astrium_block",
		new Block(FabricBlockSettings.create().mapColor(MapColor.IRON_GRAY).strength(7.0f, 12.0f).requiresTool().sounds(BlockSoundGroup.NETHERITE)));
	public static final Block TECH_PLATING = register("tech_plating",
		new Block(FabricBlockSettings.create().mapColor(MapColor.IRON_GRAY).strength(5.0f, 10.0f).requiresTool().sounds(BlockSoundGroup.NETHERITE)));
	public static final Block PLASMA_LAMP = register("plasma_lamp",
		new Block(FabricBlockSettings.create().mapColor(MapColor.IRON_GRAY).strength(2.0f).luminance(s -> 15).sounds(BlockSoundGroup.GLASS)));
	public static final Block GATEWAY_FRAME = register("gateway_frame",
		new Block(FabricBlockSettings.create().mapColor(MapColor.IRON_GRAY).strength(8.0f, 12.0f).requiresTool().luminance(s -> 6).sounds(BlockSoundGroup.NETHERITE)));
	public static final Block LUNAR_PORTAL = registerNoItem("lunar_portal",
		new LunarPortalBlock(FabricBlockSettings.create().mapColor(MapColor.PURPLE).noCollision().strength(-1.0f).luminance(s -> 12).nonOpaque().dropsNothing()));
	public static final Block SUMMONING_TERMINAL = register("summoning_terminal",
		new SummoningTerminalBlock(FabricBlockSettings.create().mapColor(MapColor.IRON_GRAY).strength(6.0f, 12.0f).requiresTool().luminance(s -> 8).sounds(BlockSoundGroup.NETHERITE)));

	private ModBlocks() {
	}

	private static Block register(String name, Block block) {
		Identifier id = new Identifier("astratech", name);
		Registry.register(Registries.ITEM, id, new BlockItem(block, new Item.Settings()));
		return Registry.register(Registries.BLOCK, id, block);
	}

	private static Block registerNoItem(String name, Block block) {
		return Registry.register(Registries.BLOCK, new Identifier("astratech", name), block);
	}

	/** Forces class loading so everything is registered. */
	public static void init() {
	}
}
