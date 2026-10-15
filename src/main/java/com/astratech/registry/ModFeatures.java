package com.astratech.registry;

import com.astratech.world.AsteroidFeature;
import com.astratech.world.CraterFeature;
import com.astratech.world.OutpostFeature;
import com.astratech.world.RadarFeature;
import com.astratech.world.SanctumFeature;
import com.astratech.world.SpaceshipFeature;
import com.astratech.world.SpireFeature;
import com.astratech.world.StationFeature;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;

public final class ModFeatures {
	public static final Feature<DefaultFeatureConfig> CRATER = Registry.register(Registries.FEATURE,
		new Identifier("astratech", "crater"), new CraterFeature(DefaultFeatureConfig.CODEC));
	public static final Feature<DefaultFeatureConfig> OUTPOST = Registry.register(Registries.FEATURE,
		new Identifier("astratech", "outpost"), new OutpostFeature(DefaultFeatureConfig.CODEC));

	public static final Feature<DefaultFeatureConfig> STATION = Registry.register(Registries.FEATURE,
		new Identifier("astratech", "station"), new StationFeature(DefaultFeatureConfig.CODEC));
	public static final Feature<DefaultFeatureConfig> SPIRE = Registry.register(Registries.FEATURE,
		new Identifier("astratech", "spire"), new SpireFeature(DefaultFeatureConfig.CODEC));
	public static final Feature<DefaultFeatureConfig> SPACESHIP = Registry.register(Registries.FEATURE,
		new Identifier("astratech", "spaceship"), new SpaceshipFeature(DefaultFeatureConfig.CODEC));
	public static final Feature<DefaultFeatureConfig> SANCTUM = Registry.register(Registries.FEATURE,
		new Identifier("astratech", "sanctum"), new SanctumFeature(DefaultFeatureConfig.CODEC));
	public static final Feature<DefaultFeatureConfig> RADAR = Registry.register(Registries.FEATURE,
		new Identifier("astratech", "radar"), new RadarFeature(DefaultFeatureConfig.CODEC));

	public static final Feature<DefaultFeatureConfig> ASTEROID = Registry.register(Registries.FEATURE,
		new Identifier("astratech", "asteroid"), new AsteroidFeature(DefaultFeatureConfig.CODEC));

	private ModFeatures() {
	}

	public static void init() {
	}
}
