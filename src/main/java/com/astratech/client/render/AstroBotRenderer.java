package com.astratech.client.render;

import com.astratech.client.model.AstroBotModel;
import com.astratech.client.model.ModModelLayers;
import com.astratech.entity.AstroBotEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;

public class AstroBotRenderer extends MobEntityRenderer<AstroBotEntity, AstroBotModel<AstroBotEntity>> {
	private static final Identifier TEXTURE = new Identifier("astratech", "textures/entity/astro_bot.png");
	private static final Identifier GLOW = new Identifier("astratech", "textures/entity/astro_bot_glow.png");

	public AstroBotRenderer(EntityRendererFactory.Context context) {
		super(context, new AstroBotModel<>(context.getPart(ModModelLayers.ASTRO_BOT)), 0.4f);
		this.addFeature(new GlowFeature<>(this, GLOW));
	}

	@Override
	public Identifier getTexture(AstroBotEntity entity) {
		return TEXTURE;
	}
}
