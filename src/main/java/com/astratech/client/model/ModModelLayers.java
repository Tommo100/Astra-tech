package com.astratech.client.model;

import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

public final class ModModelLayers {
	public static final EntityModelLayer PLASMA_DRONE = layer("plasma_drone");
	public static final EntityModelLayer ASTRO_BOT = layer("astro_bot");
	public static final EntityModelLayer LUNAR_HOPPER = layer("lunar_hopper");
	public static final EntityModelLayer NOVA_OVERMIND = layer("nova_overmind");

	private ModModelLayers() {
	}

	private static EntityModelLayer layer(String name) {
		return new EntityModelLayer(new Identifier("astratech", name), "main");
	}
}
