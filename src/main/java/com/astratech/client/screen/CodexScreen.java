package com.astratech.client.screen;

import com.astratech.net.ModNetworking;
import com.astratech.registry.ModBlocks;
import com.astratech.registry.ModEntities;
import com.astratech.registry.ModItems;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.DefaultAttributeRegistry;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** The Astra Codex: a guide to the mod with a live Showcase that spawns items and mobs. */
public class CodexScreen extends Screen {
	private static final int CYAN = 0xFF00E5FF;
	private static final int VIOLET = 0xFFB400FF;
	private static final int TEXT = 0xFFCFE8F5;
	private static final int DIM = 0xFF7A9BB0;
	private static final int GOOD = 0xFF37E07A;
	private static final int BAD = 0xFFFF4D6D;

	private enum Tab {
		OVERVIEW("Overview"), MOON("The Moon"), MOBS("Mobs"), GEAR("Gear"), BOSS("Boss"), SHOWCASE("Showcase");

		final String label;

		Tab(String label) {
			this.label = label;
		}
	}

	private record Hit(int x, int y, int w, int h, Runnable action, boolean scrolled) {
	}

	private record MobInfo(EntityType<?> type, String name, boolean friendly, String blurb, String[] abilities, int previewSize) {
	}

	private final List<Hit> hits = new ArrayList<>();
	private final Map<EntityType<?>, LivingEntity> previews = new HashMap<>();
	private final List<MobInfo> mobs = new ArrayList<>();
	private Tab tab = Tab.OVERVIEW;
	private int scroll = 0;
	private int maxScroll = 0;
	private int mobIndex = 0;
	private int px;
	private int py;
	private int pw;
	private int ph;
	private int cx;
	private int cy;
	private int cw;
	private int ch;
	private int mouseXf;
	private int mouseYf;
	private ItemStack hoverStack = null;
	private String status = "";
	private long statusTime = 0L;

	public CodexScreen() {
		super(Text.literal("Astra Codex"));
		mobs.add(new MobInfo(ModEntities.LUNAR_SENTINEL, "Lunar Sentinel", false,
			"A heavily armoured combat mech that patrols the outposts. Slow to anger, brutal in melee.",
			new String[]{"Takes 60% less damage from projectiles", "Overdrive: Speed II and Strength below half health", "Hunts players and Astro Bots"}, 38));
		mobs.add(new MobInfo(ModEntities.VOID_STALKER, "Void Stalker", false,
			"A fast, wall-climbing hunter that moves like a spider and bites like a reactor leak.",
			new String[]{"Climbs walls and ambushes from above", "Bite inflicts Wither II", "Very fast, very tough for its size"}, 30));
		mobs.add(new MobInfo(ModEntities.PLASMA_DRONE, "Plasma Drone", false,
			"A hovering gun platform that keeps its distance and strafes with plasma fire.",
			new String[]{"Flies, ignores terrain", "Fires 12 damage plasma bolts from range", "Often deployed in groups by the Overmind"}, 40));
		mobs.add(new MobInfo(ModEntities.ASTRO_BOT, "Astro Bot", true,
			"A friendly maintenance robot. It wanders the regolith, mends explorers and opens fire on anything hostile.",
			new String[]{"220 health when deployed, Regeneration II aura within 8 blocks", "Shoots hostile creatures on sight", "Feed it a Plasma Crystal to repair it"}, 44));
		mobs.add(new MobInfo(ModEntities.LUNAR_HOPPER, "Lunar Hopper", true,
			"A gentle, long-eared creature that bounds across the dust in huge low-gravity leaps.",
			new String[]{"Completely harmless, drops like a rabbit", "Follows you if you hold a Plasma Crystal", "Feed two a Plasma Crystal to breed a baby"}, 50));
		mobs.add(new MobInfo(ModEntities.NOVA_OVERMIND, "Nova Overmind", false,
			"The ancient AI that rules the Moon. Summoned only with a Quantum Core at a Summoning Terminal.",
			new String[]{"500 health, three combat phases", "Orbital strikes, bolt fans, drone and sentinel reinforcements", "Drops Nova Cores"}, 11));
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	@Override
	protected void init() {
		pw = Math.min(width - 16, 400);
		ph = Math.min(height - 16, 250);
		px = (width - pw) / 2;
		py = (height - ph) / 2;
		cx = px + 10;
		cy = py + 52;
		cw = pw - 22;
		ch = ph - 62;
	}

	// ------------------------------------------------------------------ drawing helpers

	private void border(DrawContext ctx, int x1, int y1, int x2, int y2, int color) {
		ctx.fill(x1, y1, x2, y1 + 1, color);
		ctx.fill(x1, y2 - 1, x2, y2, color);
		ctx.fill(x1, y1, x1 + 1, y2, color);
		ctx.fill(x2 - 1, y1, x2, y2, color);
	}

	private int text(DrawContext ctx, String s, int x, int y, int width, int color) {
		List<OrderedText> lines = textRenderer.wrapLines(Text.literal(s), width);
		for (OrderedText line : lines) {
			ctx.drawText(textRenderer, line, x, y, color, false);
			y += 10;
		}
		return y;
	}

	private int heading(DrawContext ctx, String s, int x, int y, int color) {
		ctx.drawText(textRenderer, s, x, y, color, true);
		ctx.fill(x, y + 10, x + Math.min(cw, textRenderer.getWidth(s) + 20), y + 11, (color & 0x00FFFFFF) | 0x66000000);
		return y + 15;
	}

	private int iconLine(DrawContext ctx, Item item, String s, int x, int y, int width, int color) {
		ItemStack stack = new ItemStack(item);
		ctx.drawItem(stack, x, y);
		if (inContent(mouseYf) && mouseXf >= x && mouseXf < x + 16 && mouseYf >= y && mouseYf < y + 16) {
			hoverStack = stack;
		}
		int end = text(ctx, s, x + 22, y + 1, width - 22, color);
		return Math.max(y + 20, end + 4);
	}

	private boolean inContent(int my) {
		return my >= cy && my < cy + ch;
	}

	private boolean over(int x, int y, int w, int h, boolean scrolled) {
		if (scrolled && !inContent(mouseYf)) {
			return false;
		}
		return mouseXf >= x && mouseXf < x + w && mouseYf >= y && mouseYf < y + h;
	}

	private void button(DrawContext ctx, int x, int y, int w, int h, String label, int accent, boolean scrolled, Runnable action) {
		boolean hover = over(x, y, w, h, scrolled);
		ctx.fill(x, y, x + w, y + h, hover ? ((accent & 0x00FFFFFF) | 0x66000000) : 0x66101B30);
		border(ctx, x, y, x + w, y + h, hover ? accent : ((accent & 0x00FFFFFF) | 0x88000000));
		ctx.drawCenteredTextWithShadow(textRenderer, label, x + w / 2, y + (h - 8) / 2, hover ? 0xFFFFFFFF : TEXT);
		hits.add(new Hit(x, y, w, h, action, scrolled));
	}

	private void itemCell(DrawContext ctx, ItemStack stack, int x, int y, Runnable action) {
		boolean hover = over(x, y, 20, 20, true);
		ctx.fill(x, y, x + 20, y + 20, hover ? 0x8800E5FF : 0x66101B30);
		border(ctx, x, y, x + 20, y + 20, hover ? CYAN : 0x6600E5FF);
		ctx.drawItem(stack, x + 2, y + 2);
		if (hover) {
			hoverStack = stack;
		}
		hits.add(new Hit(x, y, 20, 20, action, true));
	}

	private LivingEntity preview(EntityType<?> type) {
		LivingEntity cached = previews.get(type);
		if (cached != null) {
			return cached;
		}
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.world == null) {
			return null;
		}
		Entity entity = type.create(client.world);
		if (entity instanceof LivingEntity living) {
			previews.put(type, living);
			return living;
		}
		return null;
	}

	private void send(String action, String id, String statusText) {
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeString(action);
		buf.writeString(id);
		ClientPlayNetworking.send(ModNetworking.SHOWCASE, buf);
		status = statusText;
		statusTime = Util.getMeasuringTimeMs();
	}

	// ------------------------------------------------------------------ main render

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		this.renderBackground(ctx);
		hits.clear();
		hoverStack = null;
		mouseXf = mouseX;
		mouseYf = mouseY;
		long now = Util.getMeasuringTimeMs();

		ctx.fill(px, py, px + pw, py + ph, 0xF0050A18);
		border(ctx, px, py, px + pw, py + ph, CYAN);
		border(ctx, px + 3, py + 3, px + pw - 3, py + ph - 3, 0x4400E5FF);
		for (int i = 0; i < 46; i++) {
			int sx = px + 6 + (i * 97) % (pw - 12);
			int sy = py + 6 + (i * 57) % (ph - 12);
			int alpha = 55 + (int) (45 * Math.sin(now / 420.0 + i));
			ctx.fill(sx, sy, sx + 1, sy + 1, (alpha << 24) | 0xFFFFFF);
		}

		MatrixStack matrices = ctx.getMatrices();
		matrices.push();
		matrices.translate(px + 12, py + 9, 0);
		matrices.scale(1.6f, 1.6f, 1.0f);
		ctx.drawText(textRenderer, "ASTRA CODEX", 0, 0, CYAN, true);
		matrices.pop();
		ctx.drawText(textRenderer, "Lunar Tech Archive", px + 12, py + 25, DIM, false);

		button(ctx, px + pw - 24, py + 8, 14, 14, "x", BAD, false, this::close);

		int tabW = (pw - 20) / Tab.values().length;
		for (int i = 0; i < Tab.values().length; i++) {
			Tab t = Tab.values()[i];
			int tx = px + 10 + i * tabW;
			boolean selected = t == tab;
			boolean hover = over(tx, py + 36, tabW - 2, 13, false);
			ctx.fill(tx, py + 36, tx + tabW - 2, py + 49, selected ? 0xAA0B3A55 : (hover ? 0x88102A44 : 0x66101B30));
			border(ctx, tx, py + 36, tx + tabW - 2, py + 49, selected ? CYAN : 0x6600E5FF);
			ctx.drawCenteredTextWithShadow(textRenderer, t.label, tx + (tabW - 2) / 2, py + 39, selected ? 0xFFFFFFFF : TEXT);
			hits.add(new Hit(tx, py + 36, tabW - 2, 13, () -> {
				tab = t;
				scroll = 0;
			}, false));
		}

		ctx.fill(cx - 3, cy - 2, cx + cw + 3, cy + ch + 2, 0x55000814);
		border(ctx, cx - 3, cy - 2, cx + cw + 3, cy + ch + 2, 0x4400E5FF);
		int scanY = cy + (int) ((now / 25) % Math.max(1, ch));
		ctx.fill(cx - 2, scanY, cx + cw + 2, scanY + 1, 0x2200E5FF);

		ctx.enableScissor(cx - 2, cy, cx + cw + 2, cy + ch);
		int bottom;
		switch (tab) {
			case OVERVIEW -> bottom = drawOverview(ctx);
			case MOON -> bottom = drawMoon(ctx);
			case MOBS -> bottom = drawMobs(ctx);
			case GEAR -> bottom = drawGear(ctx);
			case BOSS -> bottom = drawBoss(ctx);
			default -> bottom = drawShowcase(ctx);
		}
		ctx.disableScissor();

		int total = bottom + scroll - cy;
		maxScroll = Math.max(0, total - ch + 6);
		if (scroll > maxScroll) {
			scroll = maxScroll;
		}
		if (maxScroll > 0) {
			int barH = Math.max(14, ch * ch / (total + 6));
			int barY = cy + (int) ((ch - barH) * (scroll / (float) maxScroll));
			ctx.fill(cx + cw + 3, cy, cx + cw + 6, cy + ch, 0x44000000);
			ctx.fill(cx + cw + 3, barY, cx + cw + 6, barY + barH, 0xAA00E5FF);
		}

		super.render(ctx, mouseX, mouseY, delta);
		if (hoverStack != null) {
			ctx.drawItemTooltip(textRenderer, hoverStack, mouseX, mouseY);
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0) {
			for (int i = hits.size() - 1; i >= 0; i--) {
				Hit hit = hits.get(i);
				if (hit.scrolled() && (mouseY < cy || mouseY >= cy + ch)) {
					continue;
				}
				if (mouseX >= hit.x() && mouseX < hit.x() + hit.w() && mouseY >= hit.y() && mouseY < hit.y() + hit.h()) {
					hit.action().run();
					return true;
				}
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		scroll = Math.max(0, Math.min(maxScroll, scroll - (int) (amount * 14)));
		return true;
	}

	// ------------------------------------------------------------------ pages

	private int drawOverview(DrawContext ctx) {
		int x = cx + 4;
		int w = cw - 8;
		int y = cy - scroll + 2;
		y = heading(ctx, "Welcome, explorer", x, y, CYAN);
		y = text(ctx, "Astra Tech opens the way to the Moon: a silent, low-gravity world of impact craters, abandoned research outposts and hostile machines, rich in Astrium and Plasma Crystal. Forge end-game Astral gear, befriend Astro Bots, and challenge the Nova Overmind.", x, y, w, TEXT) + 6;

		y = heading(ctx, "Quick start", x, y, CYAN);
		y = iconLine(ctx, ModBlocks.GATEWAY_FRAME.asItem(), "1. Craft Gateway Frames (makes 4). Top and bottom rows: Iron Block, Ender Pearl, Iron Block. Middle row: Diamond, Redstone Block, Diamond.", x, y, w, TEXT);
		y = iconLine(ctx, ModItems.ION_IGNITER, "2. Craft an Ion Igniter: Diamond on top; Iron Ingot, Ender Eye, Iron Ingot in the middle; Redstone Block at the bottom.", x, y, w, TEXT);
		y = iconLine(ctx, ModBlocks.TECH_PLATING.asItem(), "3. Build a closed frame at least 2 wide and 3 tall inside (corners optional). Right-click the frame from inside with the Igniter, then stand in the gateway for two seconds.", x, y, w, TEXT);
		y = iconLine(ctx, ModBlocks.ASTRIUM_ORE.asItem(), "4. On the Moon, mine Astrium Ore and Plasma Crystal Ore. Smelt Raw Astrium in a furnace or blast furnace.", x, y, w, TEXT);
		y = iconLine(ctx, ModItems.ASTRAL_CHESTPLATE, "5. Craft Astral armour and weapons (Gear tab), then challenge the Overmind (Boss tab).", x, y, w, TEXT) + 4;

		y = heading(ctx, "Good to know", x, y, CYAN);
		y = text(ctx, "Everyone on the Moon enjoys low gravity: slow falling and extra jump height. Gateway Frames also work as a return trip: a landing pad with a gateway is built for you at your arrival point.", x, y, w, TEXT) + 4;
		y = text(ctx, "Lost your Codex? Use /astratech codex, or craft one from a Book and Redstone Dust.", x, y, w, DIM) + 4;
		return y;
	}

	private int drawMoon(DrawContext ctx) {
		int x = cx + 4;
		int w = cw - 8;
		int y = cy - scroll + 2;
		y = heading(ctx, "The Moon", x, y, CYAN);
		y = text(ctx, "No atmosphere, no clouds: only black sky, a blazing sun and Earth hanging huge overhead. Three biomes cover the surface: the grey Mare Plains, the rugged Highlands and the glittering Crystal Basin.", x, y, w, TEXT) + 4;
		y = text(ctx, "Impact craters scar the surface and hide Plasma Crystal deposits. Drones, Stalkers and Sentinels roam freely, even in daylight. Astro Bots and Lunar Hoppers are friendly.", x, y, w, TEXT) + 6;
		y = heading(ctx, "Structures", x, y, CYAN);
		y = text(ctx, "Outposts: small glass domes with an antenna mast, a loot chest and sometimes a guard.", x, y, w, TEXT) + 2;
		y = text(ctx, "Helios Stations: huge domed research bases with a reactor tower, four pods, corridors and several chests. Guarded by Sentinels.", x, y, w, TEXT) + 2;
		y = text(ctx, "Lumen Spires: vertical tech facilities shaped like trees, with a hollow trunk and a ladder up to a glass canopy full of loot and Drones.", x, y, w, TEXT) + 2;
		y = text(ctx, "Landed Spaceships: wrecked-looking but intact ships with a cockpit and a cargo bay. Look inside before whatever lives there does.", x, y, w, TEXT) + 2;
		y = text(ctx, "Radar Arrays: a bunker under a giant dish with two loot chests.", x, y, w, TEXT) + 2;
		y = text(ctx, "Overmind Sanctums: rare walled arenas with a Summoning Terminal on a dais and a chest holding a Quantum Core. Four Sentinels and a Drone defend it.", x, y, w, TEXT) + 6;

		y = heading(ctx, "Materials and blocks", x, y, CYAN);
		y = iconLine(ctx, ModBlocks.ASTRIUM_ORE.asItem(), "Astrium Ore: drops Raw Astrium. Smelt into Astrium Ingots, the backbone of all Astral gear.", x, y, w, TEXT);
		y = iconLine(ctx, ModBlocks.PLASMA_CRYSTAL_ORE.asItem(), "Plasma Crystal Ore: glowing deposits that drop Plasma Crystals (affected by Fortune).", x, y, w, TEXT);
		y = iconLine(ctx, ModItems.NOVA_CORE, "Nova Core: dropped only by the Nova Overmind. Required for the Astral chestplate, blade and Pulse Blaster. Right-click to throw it: it collapses into a black hole that drags in and crushes mobs (never players).", x, y, w, TEXT);
		y = iconLine(ctx, ModBlocks.TECH_PLATING.asItem(), "Tech Plating: armoured hull panels (2 Astrium + 2 Iron makes 4).", x, y, w, TEXT);
		y = iconLine(ctx, ModBlocks.PLASMA_LAMP.asItem(), "Plasma Lamp: the brightest light source in the mod.", x, y, w, TEXT);
		y = iconLine(ctx, ModBlocks.LUNAR_REGOLITH.asItem(), "Lunar Regolith, Dust, Gravel, Cracked and Ashen Regolith, and Mare Basalt cover the Moon's surface in patches; Lunar Stone is the bedrock.", x, y, w, TEXT);
		y = iconLine(ctx, ModItems.PURPLE_LUNAR_BERRIES, "Gray and Purple Lunar Berries grow rarely in patches on the surface. Gray restores health over time, Purple gives a long Jump Boost. Plant them on any surface block.", x, y, w, TEXT);
		y = iconLine(ctx, ModBlocks.ASTRIUM_BLOCK.asItem(), "Astrium Block: compact storage (9 ingots). Plasma Crystal Block: 9 crystals, glows. Lunar Alloy Block: found in structures, cannot be smelted into ingots.", x, y, w, TEXT);
		return y;
	}

	private String stats(EntityType<?> type) {
		try {
			@SuppressWarnings("unchecked")
			DefaultAttributeContainer container = DefaultAttributeRegistry.get((EntityType<? extends LivingEntity>) type);
			if (container == null) {
				return "";
			}
			StringBuilder sb = new StringBuilder();
			sb.append("Health ").append(Math.round(container.getValue(EntityAttributes.GENERIC_MAX_HEALTH)));
			if (container.has(EntityAttributes.GENERIC_ATTACK_DAMAGE)) {
				sb.append("   Attack ").append(Math.round(container.getValue(EntityAttributes.GENERIC_ATTACK_DAMAGE)));
			}
			if (container.has(EntityAttributes.GENERIC_ARMOR)) {
				sb.append("   Armour ").append(Math.round(container.getValue(EntityAttributes.GENERIC_ARMOR)));
			}
			return sb.toString();
		} catch (Exception e) {
			return "";
		}
	}

	private int drawMobs(DrawContext ctx) {
		int x = cx + 4;
		int w = cw - 8;
		int y = cy - scroll + 2;
		MobInfo info = mobs.get(mobIndex);

		button(ctx, x, y, 22, 14, "<", CYAN, true, () -> mobIndex = (mobIndex + mobs.size() - 1) % mobs.size());
		button(ctx, x + w - 22, y, 22, 14, ">", CYAN, true, () -> mobIndex = (mobIndex + 1) % mobs.size());
		ctx.drawCenteredTextWithShadow(textRenderer, info.name() + "  (" + (mobIndex + 1) + "/" + mobs.size() + ")", x + w / 2, y + 3, 0xFFFFFFFF);
		y += 20;

		int previewW = Math.min(110, w / 3);
		ctx.fill(x, y, x + previewW, y + 100, 0x66000814);
		border(ctx, x, y, x + previewW, y + 100, info.friendly() ? GOOD : BAD);
		LivingEntity entity = preview(info.type());
		if (entity != null) {
			entity.age = (int) (Util.getMeasuringTimeMs() / 50L);
			int ex = x + previewW / 2;
			int ey = y + 90;
			InventoryScreen.drawEntity(ctx, ex, ey, info.previewSize(), (float) (ex - mouseXf), (float) (ey - 45 - mouseYf), entity);
		}

		int tx = x + previewW + 8;
		int tw = w - previewW - 8;
		int ty = y;
		ctx.drawText(textRenderer, info.friendly() ? "FRIENDLY" : "HOSTILE", tx, ty, info.friendly() ? GOOD : BAD, true);
		ty += 12;
		String stats = stats(info.type());
		if (!stats.isEmpty()) {
			ctx.drawText(textRenderer, stats, tx, ty, CYAN, false);
			ty += 12;
		}
		ty = text(ctx, info.blurb(), tx, ty, tw, TEXT) + 4;
		for (String ability : info.abilities()) {
			ctx.drawText(textRenderer, "+", tx, ty, VIOLET, false);
			ty = text(ctx, ability, tx + 8, ty, tw - 8, DIM);
		}
		return Math.max(y + 104, ty) + 4;
	}

	private int gearEntry(DrawContext ctx, Item item, String title, String body, String recipe, int x, int y, int w) {
		ItemStack stack = new ItemStack(item);
		ctx.drawItem(stack, x, y + 2);
		if (inContent(mouseYf) && mouseXf >= x && mouseXf < x + 16 && mouseYf >= y + 2 && mouseYf < y + 18) {
			hoverStack = stack;
		}
		ctx.drawText(textRenderer, title, x + 22, y, CYAN, true);
		int end = text(ctx, body, x + 22, y + 11, w - 22, TEXT);
		if (recipe != null) {
			end = text(ctx, "Craft: " + recipe, x + 22, end, w - 22, DIM);
		}
		return end + 5;
	}

	private int drawGear(DrawContext ctx) {
		int x = cx + 4;
		int w = cw - 8;
		int y = cy - scroll + 2;
		y = heading(ctx, "Astral Vanguard armour", x, y, CYAN);
		y = text(ctx, "25 armour points, 4 toughness, 15% knockback resistance and far more durability than netherite. Every piece grants an ability; wearing all four adds a set bonus.", x, y, w, TEXT) + 4;
		y = gearEntry(ctx, ModItems.ASTRAL_HELMET, "Astral Visor", "Night Vision, Water Breathing, immune to Blindness.", "5 Astrium Ingot, 1 Plasma Crystal", x, y, w);
		y = gearEntry(ctx, ModItems.ASTRAL_CHESTPLATE, "Ion Thruster Plate", "Resistance I and jetpack thrusters: hold Jump in the air to climb, Sneak + Jump to hover, and hold R (rebindable: Jetpack Forward Boost) to fly forward where you look. Thrusting wears the plate. (Dedicated servers need allow-flight=true.)", "7 Astrium Ingot, 1 Plasma Crystal, 1 Nova Core", x, y, w);
		y = gearEntry(ctx, ModItems.ASTRAL_LEGGINGS, "Gravity Greaves", "Speed II.", "7 Astrium Ingot, 1 Plasma Crystal", x, y, w);
		y = gearEntry(ctx, ModItems.ASTRAL_BOOTS, "Magboots", "Jump Boost II and complete immunity to fall damage.", "4 Astrium Ingot, 1 Plasma Crystal", x, y, w);
		y = text(ctx, "Full set bonus: Fire Resistance, Regeneration I, Strength I and Haste I.", x, y, w, GOOD) + 6;

		y = heading(ctx, "Weapons and tools", x, y, CYAN);
		y = gearEntry(ctx, ModItems.ASTRAL_BLADE, "Plasma Blade", "Oversized plasma sword. Hits slow the target and add 4 arc damage. Right-click: Photon Lance, a piercing laser beam up to 36 blocks that deals 24 damage and sets targets alight (2.5 second cooldown).", "2 Astrium Ingot above 1 Nova Core", x, y, w);
		y = gearEntry(ctx, ModItems.METEOR_HAMMER, "Meteor Hammer", "Heavy hammer: huge damage, slows and knocks back. Hold right-click to charge (the impact zone is marked where you look), release to call down 3 meteors that explode for up to 34 damage each (10 second cooldown).", "6 Astrium Ingot, 1 Nova Core, 1 Stick", x, y, w);
		y = gearEntry(ctx, ModItems.PULSE_BLASTER, "Pulse Blaster", "Tap right-click for fast plasma bolts (16 damage). Hold right-click to charge, release for a huge Plasma Orb that deals 40 damage and explodes for 30 in a 5 block blast. 2400 charges.", "Astrium Ingots and a Plasma Crystal around 1 Nova Core", x, y, w);
		y = gearEntry(ctx, ModItems.REMOTE_CONTROLLER, "Companion Remote", "Deploys your own Astro Bot that follows you, heals you and fights whatever attacks you or that you attack. Use again to recall it, sneak + use to dismiss it.", "Astrium Ingots, Plasma Crystal, Redstone and a Nova Core", x, y, w);
		y = gearEntry(ctx, ModItems.ASTRAL_PICKAXE, "Quantum Pickaxe", "Mines faster than netherite and breaks a 3x3 area of anything a pickaxe can mine. Sneak for single blocks, or sneak on ore to vein-mine the whole deposit.", "2 Astrium Ingot, 1 Plasma Crystal, 2 Sticks", x, y, w);
		y = gearEntry(ctx, ModItems.ASTRAL_AXE, "Gravity Cleaver", "Heavy axe. Chops a 3x3 area of wood. Sneak for single blocks. Every hit launches the target with Levitation.", "2 Astrium Ingot, 1 Plasma Crystal, 2 Sticks", x, y, w);
		y = gearEntry(ctx, ModItems.ASTRAL_SHOVEL, "Excavator Spade", "Breaks a 3x3 area of dirt, grass, sand, gravel and other diggable blocks. Sneak to dig a single block.", "1 Astrium Ingot, 2 Sticks", x, y, w);
		y = gearEntry(ctx, ModItems.ASTRAL_HOE, "Terraform Hoe", "End-game durability and speed for farming.", "2 Astrium Ingot, 2 Sticks", x, y, w);
		y = text(ctx, "Repair any Astral item with Astrium Ingots on an anvil. Crafting layouts are in the recipe book.", x, y, w, DIM) + 4;
		return y;
	}

	private int drawBoss(DrawContext ctx) {
		int x = cx + 4;
		int w = cw - 8;
		int y = cy - scroll + 2;
		y = heading(ctx, "The Nova Overmind", x, y, BAD);

		MobInfo boss = mobs.get(mobs.size() - 1);
		int previewW = Math.min(110, w / 3);
		ctx.fill(x, y, x + previewW, y + 100, 0x66000814);
		border(ctx, x, y, x + previewW, y + 100, BAD);
		LivingEntity entity = preview(boss.type());
		if (entity != null) {
			entity.age = (int) (Util.getMeasuringTimeMs() / 50L);
			int ex = x + previewW / 2;
			int ey = y + 80;
			InventoryScreen.drawEntity(ctx, ex, ey, boss.previewSize(), (float) (ex - mouseXf), (float) (ey - 40 - mouseYf), entity);
		}
		int tx = x + previewW + 8;
		int tw = w - previewW - 8;
		int ty = y;
		ctx.drawText(textRenderer, stats(boss.type()), tx, ty, CYAN, false);
		ty += 12;
		ty = text(ctx, "A hovering AI core guarded by rings of plasma. It cannot be harmed while it powers up, wither-proof, and no single hit can deal more than 50 damage.", tx, ty, tw, TEXT) + 4;
		y = Math.max(y + 104, ty) + 4;

		y = heading(ctx, "Phases", x, y, BAD);
		y = text(ctx, "Phase 1: aimed plasma bolts and telegraphed orbital strikes on every player.", x, y, w, TEXT);
		y = text(ctx, "Phase 2 (below 66%, turns red): the Overmind drops its guns and charges at you in a melee rush. Watch for the wind-up flare, then dodge sideways. Strikes and Plasma Drone reinforcements continue.", x, y, w, TEXT);
		y = text(ctx, "Phase 3 (below 33%, turns violet): bolt fans, radial bolt rings, gravity pulses that hurl you away, Lunar Sentinel reinforcements and faster strikes.", x, y, w, TEXT) + 6;

		y = heading(ctx, "How to summon", x, y, CYAN);
		y = iconLine(ctx, ModItems.QUANTUM_CORE, "Quantum Core: Plasma Crystals in the corners, Astrium Ingots on the edges, a Netherite Ingot in the centre.", x, y, w, TEXT);
		y = iconLine(ctx, ModBlocks.SUMMONING_TERMINAL.asItem(), "Summoning Terminal: Tech Plating around the edge, Plasma Crystals at the top middle and sides, an Astrium Block in the centre.", x, y, w, TEXT);
		y = text(ctx, "Travel to the Moon and either place a Terminal on open ground or use the one waiting in an Overmind Sanctum, then right-click it with the Quantum Core. The Overmind awakens after a short power-up. Bring Astral armour, a Pulse Blaster, and room to run.", x, y, w, TEXT) + 4;
		y = iconLine(ctx, ModItems.NOVA_CORE, "Rewards: 3 to 5 Nova Cores, Astrium Ingots, Plasma Crystals and a lot of experience.", x, y, w, GOOD);
		return y;
	}

	private List<ItemStack> showcaseItems() {
		List<ItemStack> list = new ArrayList<>();
		for (Item item : ModItems.SHOWCASE_ITEMS) {
			if (!ModItems.SPAWN_EGGS.containsValue(item)) {
				list.add(new ItemStack(item));
			}
		}
		list.add(new ItemStack(ModBlocks.LUNAR_REGOLITH));
		list.add(new ItemStack(ModBlocks.LUNAR_DUST));
		list.add(new ItemStack(ModBlocks.LUNAR_GRAVEL));
		list.add(new ItemStack(ModBlocks.CRACKED_REGOLITH));
		list.add(new ItemStack(ModBlocks.ASHEN_REGOLITH));
		list.add(new ItemStack(ModBlocks.MARE_BASALT));
		list.add(new ItemStack(ModBlocks.LUNAR_STONE));
		list.add(new ItemStack(ModBlocks.ASTRIUM_ORE));
		list.add(new ItemStack(ModBlocks.PLASMA_CRYSTAL_ORE));
		list.add(new ItemStack(ModBlocks.ASTRIUM_BLOCK));
		list.add(new ItemStack(ModBlocks.PLASMA_CRYSTAL_BLOCK));
		list.add(new ItemStack(ModBlocks.LUNAR_ALLOY_BLOCK));
		list.add(new ItemStack(ModBlocks.TECH_PLATING));
		list.add(new ItemStack(ModBlocks.PLASMA_LAMP));
		list.add(new ItemStack(ModBlocks.GATEWAY_FRAME));
		list.add(new ItemStack(ModBlocks.SUMMONING_TERMINAL));
		return list;
	}

	private int drawShowcase(DrawContext ctx) {
		int x = cx + 4;
		int w = cw - 8;
		int y = cy - scroll + 2;
		y = heading(ctx, "Showcase", x, y, CYAN);
		y = text(ctx, "Click an item to add it to your inventory (stacks of 16 where possible). Click a mob to spawn it in front of you. Needs Creative mode, operator rights, or a single-player world.", x, y, w, TEXT) + 4;

		long age = Util.getMeasuringTimeMs() - statusTime;
		if (!status.isEmpty() && age < 4000) {
			ctx.drawText(textRenderer, status, x, y, GOOD, false);
		}
		y += 12;

		y = heading(ctx, "Items and blocks", x, y, CYAN);
		int cols = Math.max(1, w / 22);
		int i = 0;
		for (ItemStack stack : showcaseItems()) {
			int col = i % cols;
			int row = i / cols;
			int ix = x + col * 22;
			int iy = y + row * 22;
			String id = Registries.ITEM.getId(stack.getItem()).toString();
			String name = stack.getName().getString();
			itemCell(ctx, stack, ix, iy, () -> send("item", id, "Added " + name + " to your inventory."));
			i++;
		}
		int rows = (i + cols - 1) / cols;
		y += rows * 22 + 8;

		y = heading(ctx, "Mobs", x, y, CYAN);
		int bw = (w - 6) / 2;
		for (int m = 0; m < mobs.size(); m++) {
			MobInfo info = mobs.get(m);
			int bx = x + (m % 2) * (bw + 6);
			int by = y + (m / 2) * 20;
			String id = Registries.ENTITY_TYPE.getId(info.type()).toString();
			button(ctx, bx, by, bw, 17, (info.friendly() ? "+ " : "! ") + info.name(), info.friendly() ? GOOD : BAD, true,
				() -> send("mob", id, "Spawned " + info.name() + "."));
		}
		y += ((mobs.size() + 1) / 2) * 20 + 8;

		y = heading(ctx, "Utilities", x, y, CYAN);
		button(ctx, x, y, bw, 17, "Warp: Moon / Home", VIOLET, true, () -> send("warp", "", "Warping..."));
		button(ctx, x + bw + 6, y, bw, 17, "Get another Codex", VIOLET, true, () -> send("codex", "", "Codex added to your inventory."));
		y += 24;
		return y;
	}
}
