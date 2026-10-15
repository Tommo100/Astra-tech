package com.astratech.client.model;

import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

/** Rabbit-like low-gravity creature with antenna ears. Texture 64x32. */
public class LunarHopperModel<T extends Entity> extends SinglePartEntityModel<T> {
	private final ModelPart root;
	private final ModelPart head;
	private final ModelPart earLeft;
	private final ModelPart earRight;
	private final ModelPart hindLeft;
	private final ModelPart hindRight;
	private final ModelPart frontLeft;
	private final ModelPart frontRight;

	public LunarHopperModel(ModelPart root) {
		this.root = root;
		this.head = root.getChild("head");
		this.earLeft = root.getChild("ear_l");
		this.earRight = root.getChild("ear_r");
		this.hindLeft = root.getChild("hind_l");
		this.hindRight = root.getChild("hind_r");
		this.frontLeft = root.getChild("front_l");
		this.frontRight = root.getChild("front_r");
	}

	public static TexturedModelData getTexturedModelData() {
		ModelData data = new ModelData();
		ModelPartData root = data.getRoot();
		root.addChild("body", ModelPartBuilder.create().uv(0, 0).cuboid(-3.0f, -3.0f, -5.0f, 6.0f, 6.0f, 10.0f), ModelTransform.pivot(0.0f, 19.0f, 0.0f));
		root.addChild("head", ModelPartBuilder.create().uv(0, 16).cuboid(-2.5f, -2.5f, -4.0f, 5.0f, 5.0f, 4.0f), ModelTransform.pivot(0.0f, 17.0f, -5.0f));
		root.addChild("ear_l", ModelPartBuilder.create().uv(32, 0).cuboid(-0.5f, -6.0f, -0.5f, 1.0f, 6.0f, 1.0f), ModelTransform.pivot(1.5f, 15.0f, -6.0f));
		root.addChild("ear_r", ModelPartBuilder.create().uv(32, 0).mirrored().cuboid(-0.5f, -6.0f, -0.5f, 1.0f, 6.0f, 1.0f), ModelTransform.pivot(-1.5f, 15.0f, -6.0f));
		root.addChild("hind_l", ModelPartBuilder.create().uv(40, 0).cuboid(-1.0f, -1.0f, -1.5f, 2.0f, 3.0f, 4.0f), ModelTransform.pivot(2.5f, 22.0f, 3.0f));
		root.addChild("hind_r", ModelPartBuilder.create().uv(40, 0).mirrored().cuboid(-1.0f, -1.0f, -1.5f, 2.0f, 3.0f, 4.0f), ModelTransform.pivot(-2.5f, 22.0f, 3.0f));
		root.addChild("front_l", ModelPartBuilder.create().uv(40, 8).cuboid(-1.0f, 0.0f, -1.0f, 2.0f, 3.0f, 2.0f), ModelTransform.pivot(2.0f, 21.0f, -3.0f));
		root.addChild("front_r", ModelPartBuilder.create().uv(40, 8).mirrored().cuboid(-1.0f, 0.0f, -1.0f, 2.0f, 3.0f, 2.0f), ModelTransform.pivot(-2.0f, 21.0f, -3.0f));
		root.addChild("tail", ModelPartBuilder.create().uv(0, 26).cuboid(-1.5f, -1.5f, 0.0f, 3.0f, 3.0f, 2.0f), ModelTransform.pivot(0.0f, 18.0f, 5.0f));
		return TexturedModelData.of(data, 64, 32);
	}

	@Override
	public ModelPart getPart() {
		return this.root;
	}

	@Override
	public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
		this.head.yaw = headYaw * ((float) Math.PI / 180.0f);
		this.head.pitch = headPitch * ((float) Math.PI / 180.0f);
		float wobble = MathHelper.sin(animationProgress * 0.2f) * 0.1f;
		this.earLeft.roll = 0.2f + wobble;
		this.earRight.roll = -0.2f - wobble;
		if (entity.isOnGround()) {
			float swing = MathHelper.cos(limbAngle * 0.8f) * limbDistance * 1.2f;
			this.hindLeft.pitch = swing;
			this.hindRight.pitch = swing;
			this.frontLeft.pitch = -swing;
			this.frontRight.pitch = -swing;
		} else {
			this.hindLeft.pitch = -0.9f;
			this.hindRight.pitch = -0.9f;
			this.frontLeft.pitch = 0.7f;
			this.frontRight.pitch = 0.7f;
		}
	}
}
