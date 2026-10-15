package com.astratech.client.render;

import com.astratech.client.model.ModModelLayers;
import com.astratech.client.model.NovaOvermindModel;
import com.astratech.entity.NovaOvermindEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

/** Draws the Overmind at double size, recoloured for each combat phase (blue, red, violet). */
public class NovaOvermindRenderer extends MobEntityRenderer<NovaOvermindEntity, NovaOvermindModel<NovaOvermindEntity>> {
	private static final Identifier[] TEXTURES = {
		new Identifier("astratech", "textures/entity/nova_overmind.png"),
		new Identifier("astratech", "textures/entity/nova_overmind_p2.png"),
		new Identifier("astratech", "textures/entity/nova_overmind_p3.png")
	};
	private static final Identifier[] GLOWS = {
		new Identifier("astratech", "textures/entity/nova_overmind_glow.png"),
		new Identifier("astratech", "textures/entity/nova_overmind_p2_glow.png"),
		new Identifier("astratech", "textures/entity/nova_overmind_p3_glow.png")
	};

	public NovaOvermindRenderer(EntityRendererFactory.Context context) {
		super(context, new NovaOvermindModel<>(context.getPart(ModModelLayers.NOVA_OVERMIND)), 2.4f);
		for (int i = 0; i < 3; i++) {
			final int index = i;
			this.addFeature(new PhaseGlow(this, GLOWS[i], index + 1));
		}
	}

	@Override
	protected void scale(NovaOvermindEntity entity, MatrixStack matrices, float amount) {
		matrices.scale(2.0f, 2.0f, 2.0f);
	}

	@Override
	public Identifier getTexture(NovaOvermindEntity entity) {
		return TEXTURES[entity.getPhase() - 1];
	}

	private static class PhaseGlow extends GlowFeature<NovaOvermindEntity, NovaOvermindModel<NovaOvermindEntity>> {
		private final int phase;

		PhaseGlow(NovaOvermindRenderer renderer, Identifier glow, int phase) {
			super(renderer, glow);
			this.phase = phase;
		}

		@Override
		public void render(MatrixStack matrices, net.minecraft.client.render.VertexConsumerProvider vertexConsumers, int light, NovaOvermindEntity entity,
				float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
			if (entity.getPhase() == this.phase) {
				super.render(matrices, vertexConsumers, light, entity, limbAngle, limbDistance, tickDelta, animationProgress, headYaw, headPitch);
			}
		}
	}
}
