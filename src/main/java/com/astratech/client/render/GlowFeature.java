package com.astratech.client.render;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.feature.EyesFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

/** Fullbright emissive overlay: draws the opaque pixels of a "_glow" texture without lighting. */
public class GlowFeature<T extends Entity, M extends EntityModel<T>> extends EyesFeatureRenderer<T, M> {
	private final RenderLayer layer;

	public GlowFeature(FeatureRendererContext<T, M> context, Identifier glowTexture) {
		super(context);
		this.layer = RenderLayer.getEyes(glowTexture);
	}

	@Override
	public RenderLayer getEyesTexture() {
		return this.layer;
	}
}
