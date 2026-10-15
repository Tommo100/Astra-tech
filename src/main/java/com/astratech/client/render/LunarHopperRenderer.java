package com.astratech.client.render;

import com.astratech.client.model.LunarHopperModel;
import com.astratech.client.model.ModModelLayers;
import com.astratech.entity.LunarHopperEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;

public class LunarHopperRenderer extends MobEntityRenderer<LunarHopperEntity, LunarHopperModel<LunarHopperEntity>> {
	private static final Identifier TEXTURE = new Identifier("astratech", "textures/entity/lunar_hopper.png");
	private static final Identifier GLOW = new Identifier("astratech", "textures/entity/lunar_hopper_glow.png");

	public LunarHopperRenderer(EntityRendererFactory.Context context) {
		super(context, new LunarHopperModel<>(context.getPart(ModModelLayers.LUNAR_HOPPER)), 0.35f);
		this.addFeature(new GlowFeature<>(this, GLOW));
	}

	@Override
	public Identifier getTexture(LunarHopperEntity entity) {
		return TEXTURE;
	}
}
