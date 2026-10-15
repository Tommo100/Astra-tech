package com.astratech.client.render;

import com.astratech.entity.VoidStalkerEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.SpiderEntityRenderer;
import net.minecraft.client.render.entity.model.SpiderEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class VoidStalkerRenderer extends SpiderEntityRenderer<VoidStalkerEntity> {
	private static final Identifier TEXTURE = new Identifier("astratech", "textures/entity/void_stalker.png");
	private static final Identifier GLOW = new Identifier("astratech", "textures/entity/void_stalker_glow.png");

	public VoidStalkerRenderer(EntityRendererFactory.Context context) {
		super(context);
		this.addFeature(new GlowFeature<VoidStalkerEntity, SpiderEntityModel<VoidStalkerEntity>>(this, GLOW));
	}

	@Override
	public Identifier getTexture(VoidStalkerEntity entity) {
		return TEXTURE;
	}

	@Override
	protected void scale(VoidStalkerEntity entity, MatrixStack matrices, float amount) {
		matrices.scale(1.2f, 1.2f, 1.2f);
	}
}
