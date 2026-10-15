package com.astratech.client.model;

import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;

/**
 * Extra 3D armour pieces for the Astral set (crest and ear pods, chest reactor, thruster pack, shoulder pads,
 * knee plates, boot fins). Coordinates are relative to the vanilla body part each piece attaches to.
 * Texture 64x64: rows 0-23 metal (v=0), 24-39 emissive cyan (v=24), 40-63 dark gunmetal (v=40).
 */
public final class AstralGearModel {
	private AstralGearModel() {
	}

	private static void metal(ModelPartData root, String name, float x, float y, float z, float w, float h, float d) {
		root.addChild(name, ModelPartBuilder.create().uv(0, 0).cuboid(x, y, z, w, h, d), ModelTransform.NONE);
	}

	private static void glow(ModelPartData root, String name, float x, float y, float z, float w, float h, float d) {
		root.addChild(name, ModelPartBuilder.create().uv(0, 24).cuboid(x, y, z, w, h, d), ModelTransform.NONE);
	}

	private static void dark(ModelPartData root, String name, float x, float y, float z, float w, float h, float d) {
		root.addChild(name, ModelPartBuilder.create().uv(0, 40).cuboid(x, y, z, w, h, d), ModelTransform.NONE);
	}

	public static TexturedModelData getTexturedModelData() {
		ModelData data = new ModelData();
		ModelPartData root = data.getRoot();

		// Helmet (attached to head)
		metal(root, "helmet_fin", -0.8f, -12.5f, -3.0f, 1.6f, 4.0f, 8.0f);
		metal(root, "helmet_pod_r", -7.0f, -6.5f, -2.0f, 2.2f, 4.0f, 4.0f);
		metal(root, "helmet_pod_l", 4.8f, -6.5f, -2.0f, 2.2f, 4.0f, 4.0f);
		dark(root, "helmet_brow", -5.2f, -7.4f, -6.0f, 10.4f, 1.4f, 1.2f);
		glow(root, "helmet_glow_fin", -0.4f, -12.0f, -1.0f, 0.8f, 2.5f, 5.0f);
		glow(root, "helmet_glow_pod_r", -7.3f, -5.6f, -1.0f, 0.6f, 2.0f, 2.0f);
		glow(root, "helmet_glow_pod_l", 6.7f, -5.6f, -1.0f, 0.6f, 2.0f, 2.0f);

		// Chestplate (attached to body)
		metal(root, "chest_frame", -3.4f, 1.6f, -3.9f, 6.8f, 6.0f, 1.0f);
		glow(root, "chest_core", -2.0f, 2.8f, -4.4f, 4.0f, 3.6f, 0.8f);
		metal(root, "chest_collar", -5.4f, -0.8f, -3.4f, 10.8f, 1.6f, 6.8f);
		dark(root, "back_pack_l", -4.2f, 0.6f, 3.0f, 3.6f, 9.0f, 3.2f);
		dark(root, "back_pack_r", 0.6f, 0.6f, 3.0f, 3.6f, 9.0f, 3.2f);
		glow(root, "back_glow_l", -3.4f, 9.4f, 3.6f, 2.0f, 1.4f, 2.0f);
		glow(root, "back_glow_r", 1.4f, 9.4f, 3.6f, 2.0f, 1.4f, 2.0f);
		metal(root, "back_spine", -0.8f, 1.0f, 5.8f, 1.6f, 9.0f, 1.0f);

		// Shoulder pads (attached to arms; right arm centre x=-1, left arm centre x=+1)
		metal(root, "pad_r", -5.2f, -4.2f, -3.4f, 7.0f, 2.6f, 6.8f);
		metal(root, "pad_l", -1.8f, -4.2f, -3.4f, 7.0f, 2.6f, 6.8f);
		dark(root, "pad_spike_r", -3.6f, -6.6f, -1.2f, 2.8f, 2.4f, 2.4f);
		dark(root, "pad_spike_l", 0.8f, -6.6f, -1.2f, 2.8f, 2.4f, 2.4f);
		glow(root, "pad_glow_r", -4.6f, -2.0f, -3.9f, 5.8f, 0.8f, 0.6f);
		glow(root, "pad_glow_l", -1.2f, -2.0f, -3.9f, 5.8f, 0.8f, 0.6f);

		// Leggings (attached to legs)
		metal(root, "knee", -2.9f, 4.4f, -3.9f, 5.8f, 3.4f, 1.4f);
		glow(root, "knee_glow", -1.2f, 5.4f, -4.4f, 2.4f, 1.2f, 0.6f);
		dark(root, "thigh_guard_r", -3.9f, 0.4f, -2.2f, 1.6f, 4.0f, 4.4f);
		dark(root, "thigh_guard_l", 2.3f, 0.4f, -2.2f, 1.6f, 4.0f, 4.4f);

		// Boots (attached to legs)
		metal(root, "boot_fin_r", -4.2f, 8.6f, -1.8f, 1.6f, 3.0f, 4.6f);
		metal(root, "boot_fin_l", 2.6f, 8.6f, -1.8f, 1.6f, 3.0f, 4.6f);
		dark(root, "boot_heel", -2.0f, 9.0f, 2.6f, 4.0f, 3.0f, 1.8f);
		glow(root, "boot_heel_glow", -1.2f, 10.0f, 4.2f, 2.4f, 1.0f, 0.5f);
		glow(root, "boot_toe_glow", -1.4f, 11.2f, -3.4f, 2.8f, 0.8f, 0.5f);
		return TexturedModelData.of(data, 64, 64);
	}
}
