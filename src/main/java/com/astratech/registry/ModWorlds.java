package com.astratech.registry;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public final class ModWorlds {
	public static final RegistryKey<World> MOON = RegistryKey.of(RegistryKeys.WORLD, new Identifier("astratech", "moon"));

	private ModWorlds() {
	}
}
