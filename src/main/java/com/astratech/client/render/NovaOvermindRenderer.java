package com.astratech.client.render;

import com.astratech.client.model.ModModelLayers;
import com.astratech.client.model.NovaOvermindModel;
import com.astratech.entity.NovaOvermindEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;

public class NovaOvermindRenderer extends MobEntityRenderer<NovaOvermindEntity, NovaOvermindModel<NovaOvermindEntity>> {
	private static final Identifier TEXTURE = new Identifier("astratech", "textures/entity/nova_overmind.png");
	private static final Identifier GLOW = new Identifier("astratech", "textures/entity/nova_overmind_glow.png");

	public NovaOvermindRenderer(EntityRendererFactory.Context context) {
		super(context, new NovaOvermindModel<>(context.getPart(ModModelLayers.NOVA_OVERMIND)), 1.2f);
		this.addFeature(new GlowFeature<>(this, GLOW));
	}

	@Override
	public Identifier getTexture(NovaOvermindEntity entity) {
		return TEXTURE;
	}
}
