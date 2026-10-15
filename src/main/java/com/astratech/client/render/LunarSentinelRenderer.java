package com.astratech.client.render;

import com.astratech.entity.LunarSentinelEntity;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class LunarSentinelRenderer extends BipedEntityRenderer<LunarSentinelEntity, BipedEntityModel<LunarSentinelEntity>> {
	private static final Identifier TEXTURE = new Identifier("astratech", "textures/entity/lunar_sentinel.png");
	private static final Identifier GLOW = new Identifier("astratech", "textures/entity/lunar_sentinel_glow.png");

	public LunarSentinelRenderer(EntityRendererFactory.Context context) {
		super(context, new BipedEntityModel<>(context.getPart(EntityModelLayers.ZOMBIE)), 0.7f);
		this.addFeature(new GlowFeature<>(this, GLOW));
	}

	@Override
	public Identifier getTexture(LunarSentinelEntity entity) {
		return TEXTURE;
	}

	@Override
	protected void scale(LunarSentinelEntity entity, MatrixStack matrices, float amount) {
		matrices.scale(1.25f, 1.25f, 1.25f);
	}
}
