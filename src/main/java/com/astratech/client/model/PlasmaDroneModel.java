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

/** Hovering saucer drone: armoured core, spinning plate, front lens and twin thrusters. Texture 64x64. */
public class PlasmaDroneModel<T extends Entity> extends SinglePartEntityModel<T> {
	private final ModelPart root;
	private final ModelPart body;
	private final ModelPart ring;
	private final ModelPart eye;
	private final ModelPart thrusterLeft;
	private final ModelPart thrusterRight;

	public PlasmaDroneModel(ModelPart root) {
		this.root = root;
		this.body = root.getChild("body");
		this.ring = root.getChild("ring");
		this.eye = root.getChild("eye");
		this.thrusterLeft = root.getChild("thruster_l");
		this.thrusterRight = root.getChild("thruster_r");
	}

	public static TexturedModelData getTexturedModelData() {
		ModelData data = new ModelData();
		ModelPartData root = data.getRoot();
		root.addChild("body", ModelPartBuilder.create().uv(0, 0).cuboid(-4.0f, -4.0f, -4.0f, 8.0f, 8.0f, 8.0f), ModelTransform.pivot(0.0f, 17.0f, 0.0f));
		root.addChild("ring", ModelPartBuilder.create().uv(0, 16).cuboid(-7.0f, -1.0f, -7.0f, 14.0f, 2.0f, 14.0f), ModelTransform.pivot(0.0f, 17.0f, 0.0f));
		root.addChild("eye", ModelPartBuilder.create().uv(0, 34).cuboid(-2.0f, -2.0f, -5.0f, 4.0f, 4.0f, 1.0f), ModelTransform.pivot(0.0f, 17.0f, 0.0f));
		root.addChild("thruster_l", ModelPartBuilder.create().uv(0, 40).cuboid(-1.0f, -1.0f, 0.0f, 2.0f, 2.0f, 5.0f), ModelTransform.pivot(-5.0f, 17.0f, 3.0f));
		root.addChild("thruster_r", ModelPartBuilder.create().uv(0, 40).mirrored().cuboid(-1.0f, -1.0f, 0.0f, 2.0f, 2.0f, 5.0f), ModelTransform.pivot(5.0f, 17.0f, 3.0f));
		return TexturedModelData.of(data, 64, 64);
	}

	@Override
	public ModelPart getPart() {
		return this.root;
	}

	@Override
	public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
		float bob = MathHelper.sin(animationProgress * 0.15f) * 1.2f;
		float y = 17.0f + bob;
		this.body.pivotY = y;
		this.ring.pivotY = y;
		this.eye.pivotY = y;
		this.thrusterLeft.pivotY = y;
		this.thrusterRight.pivotY = y;
		this.ring.yaw = animationProgress * 0.25f;
		this.root.yaw = 0.0f;
		this.body.yaw = headYaw * ((float) Math.PI / 180.0f);
		this.eye.yaw = this.body.yaw;
		this.body.pitch = headPitch * ((float) Math.PI / 180.0f);
		this.eye.pitch = this.body.pitch;
	}
}
