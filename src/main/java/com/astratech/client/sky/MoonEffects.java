package com.astratech.client.sky;

import net.minecraft.client.render.DimensionEffects;
import net.minecraft.util.math.Vec3d;

/** Dimension look for the Moon: no atmosphere, no clouds, black fog. The sky itself is drawn by MoonSky. */
public class MoonEffects extends DimensionEffects {
	public MoonEffects() {
		super(Float.NaN, false, DimensionEffects.SkyType.NONE, false, false);
	}

	@Override
	public Vec3d adjustFogColor(Vec3d color, float sunHeight) {
		return Vec3d.ZERO;
	}

	@Override
	public boolean useThickFog(int camX, int camY) {
		return false;
	}
}
