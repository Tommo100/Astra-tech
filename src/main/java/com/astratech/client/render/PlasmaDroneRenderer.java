package com.astratech.client.render;

import com.astratech.client.model.ModModelLayers;
import com.astratech.client.model.PlasmaDroneModel;
import com.astratech.entity.PlasmaDroneEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;

public class PlasmaDroneRenderer extends MobEntityRenderer<PlasmaDroneEntity, PlasmaDroneModel<PlasmaDroneEntity>> {
	private static final Identifier TEXTURE = new Identifier("astratech", "textures/entity/plasma_drone.png");
	private static final Identifier GLOW = new Identifier("astratech", "textures/entity/plasma_drone_glow.png");

	public PlasmaDroneRenderer(EntityRendererFactory.Context context) {
		super(context, new PlasmaDroneModel<>(context.getPart(ModModelLayers.PLASMA_DRONE)), 0.4f);
		this.addFeature(new GlowFeature<>(this, GLOW));
	}

	@Override
	public Identifier getTexture(PlasmaDroneEntity entity) {
		return TEXTURE;
	}
}
