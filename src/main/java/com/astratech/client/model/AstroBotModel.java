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

/** Friendly white-and-cyan support robot with a visor and antenna. Texture 64x64. */
public class AstroBotModel<T extends Entity> extends SinglePartEntityModel<T> {
	private final ModelPart root;
	private final ModelPart head;
	private final ModelPart visor;
	private final ModelPart antenna;
	private final ModelPart armLeft;
	private final ModelPart armRight;
	private final ModelPart legLeft;
	private final ModelPart legRight;

	public AstroBotModel(ModelPart root) {
		this.root = root;
		this.head = root.getChild("head");
		this.visor = root.getChild("visor");
		this.antenna = root.getChild("antenna");
		this.armLeft = root.getChild("arm_l");
		this.armRight = root.getChild("arm_r");
		this.legLeft = root.getChild("leg_l");
		this.legRight = root.getChild("leg_r");
	}

	public static TexturedModelData getTexturedModelData() {
		ModelData data = new ModelData();
		ModelPartData root = data.getRoot();
		root.addChild("body", ModelPartBuilder.create().uv(0, 16).cuboid(-4.0f, -8.0f, -3.0f, 8.0f, 8.0f, 6.0f), ModelTransform.pivot(0.0f, 18.0f, 0.0f));
		root.addChild("head", ModelPartBuilder.create().uv(0, 0).cuboid(-4.0f, -7.0f, -4.0f, 8.0f, 7.0f, 8.0f), ModelTransform.pivot(0.0f, 10.0f, 0.0f));
		root.addChild("visor", ModelPartBuilder.create().uv(0, 32).cuboid(-3.0f, -5.0f, -4.5f, 6.0f, 3.0f, 1.0f), ModelTransform.pivot(0.0f, 10.0f, 0.0f));
		root.addChild("antenna", ModelPartBuilder.create().uv(0, 40).cuboid(-0.5f, -4.0f, -0.5f, 1.0f, 4.0f, 1.0f), ModelTransform.pivot(0.0f, 3.0f, 0.0f));
		root.addChild("arm_r", ModelPartBuilder.create().uv(40, 16).cuboid(-1.0f, 0.0f, -1.5f, 2.0f, 8.0f, 3.0f), ModelTransform.pivot(-5.0f, 11.0f, 0.0f));
		root.addChild("arm_l", ModelPartBuilder.create().uv(40, 16).mirrored().cuboid(-1.0f, 0.0f, -1.5f, 2.0f, 8.0f, 3.0f), ModelTransform.pivot(5.0f, 11.0f, 0.0f));
		root.addChild("leg_r", ModelPartBuilder.create().uv(32, 0).cuboid(-1.5f, 0.0f, -1.5f, 3.0f, 6.0f, 3.0f), ModelTransform.pivot(-2.0f, 18.0f, 0.0f));
		root.addChild("leg_l", ModelPartBuilder.create().uv(32, 0).mirrored().cuboid(-1.5f, 0.0f, -1.5f, 3.0f, 6.0f, 3.0f), ModelTransform.pivot(2.0f, 18.0f, 0.0f));
		return TexturedModelData.of(data, 64, 64);
	}

	@Override
	public ModelPart getPart() {
		return this.root;
	}

	@Override
	public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
		float yaw = headYaw * ((float) Math.PI / 180.0f);
		float pitch = headPitch * ((float) Math.PI / 180.0f);
		this.head.yaw = yaw;
		this.head.pitch = pitch;
		this.visor.yaw = yaw;
		this.visor.pitch = pitch;
		this.antenna.yaw = animationProgress * 0.2f;
		float swing = MathHelper.cos(limbAngle * 0.6662f) * 1.2f * limbDistance;
		this.legRight.pitch = swing;
		this.legLeft.pitch = -swing;
		this.armRight.pitch = -swing * 0.8f;
		this.armLeft.pitch = swing * 0.8f;
		this.armRight.roll = 0.05f + MathHelper.sin(animationProgress * 0.09f) * 0.05f;
		this.armLeft.roll = -0.05f - MathHelper.sin(animationProgress * 0.09f) * 0.05f;
	}
}
