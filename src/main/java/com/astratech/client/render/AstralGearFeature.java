package com.astratech.client.render;

import com.astratech.registry.ModItems;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

/** Draws the 3D cuboid add-ons of the Astral armour on top of the normal armour layer. */
public class AstralGearFeature<T extends LivingEntity, M extends BipedEntityModel<T>> extends FeatureRenderer<T, M> {
	private static final Identifier TEXTURE = new Identifier("astratech", "textures/entity/astral_gear.png");
	private static final int FULL_BRIGHT = 0xF000F0;
	private final ModelPart root;

	public AstralGearFeature(FeatureRendererContext<T, M> context, ModelPart root) {
		super(context);
		this.root = root;
	}

	private static boolean worn(LivingEntity entity, EquipmentSlot slot, Item item) {
		return entity.getEquippedStack(slot).isOf(item);
	}

	private void draw(MatrixStack matrices, VertexConsumer vc, int light, ModelPart attach, String... names) {
		matrices.push();
		attach.rotate(matrices);
		for (String name : names) {
			int l = name.contains("glow") || name.endsWith("_core") ? FULL_BRIGHT : light;
			this.root.getChild(name).render(matrices, vc, l, OverlayTexture.DEFAULT_UV);
		}
		matrices.pop();
	}

	@Override
	public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T entity, float limbAngle, float limbDistance,
			float tickDelta, float animationProgress, float headYaw, float headPitch) {
		if (entity.isInvisible()) {
			return;
		}
		M model = this.getContextModel();
		VertexConsumer vc = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(TEXTURE));
		if (worn(entity, EquipmentSlot.HEAD, ModItems.ASTRAL_HELMET)) {
			this.draw(matrices, vc, light, model.head, "helmet_fin", "helmet_pod_r", "helmet_pod_l", "helmet_brow",
				"helmet_glow_fin", "helmet_glow_pod_r", "helmet_glow_pod_l");
		}
		if (worn(entity, EquipmentSlot.CHEST, ModItems.ASTRAL_CHESTPLATE)) {
			this.draw(matrices, vc, light, model.body, "chest_frame", "chest_core", "chest_collar", "back_pack_l", "back_pack_r",
				"back_glow_l", "back_glow_r", "back_spine");
			this.draw(matrices, vc, light, model.rightArm, "pad_r", "pad_spike_r", "pad_glow_r");
			this.draw(matrices, vc, light, model.leftArm, "pad_l", "pad_spike_l", "pad_glow_l");
		}
		if (worn(entity, EquipmentSlot.LEGS, ModItems.ASTRAL_LEGGINGS)) {
			this.draw(matrices, vc, light, model.rightLeg, "knee", "knee_glow", "thigh_guard_r");
			this.draw(matrices, vc, light, model.leftLeg, "knee", "knee_glow", "thigh_guard_l");
		}
		if (worn(entity, EquipmentSlot.FEET, ModItems.ASTRAL_BOOTS)) {
			this.draw(matrices, vc, light, model.rightLeg, "boot_fin_r", "boot_heel", "boot_heel_glow", "boot_toe_glow");
			this.draw(matrices, vc, light, model.leftLeg, "boot_fin_l", "boot_heel", "boot_heel_glow", "boot_toe_glow");
		}
	}
}
