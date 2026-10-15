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

/** The Nova Overmind: armoured AI core with a front lens, twin cannons and two counter-rotating rings. Texture 128x128. */
public class NovaOvermindModel<T extends Entity> extends SinglePartEntityModel<T> {
	private final ModelPart root;
	private final ModelPart core;
	private final ModelPart eye;
	private final ModelPart ringA;
	private final ModelPart ringB;
	private final ModelPart cannonLeft;
	private final ModelPart cannonRight;
	private final ModelPart pylonTop;
	private final ModelPart pylonBottom;

	public NovaOvermindModel(ModelPart root) {
		this.root = root;
		this.core = root.getChild("core");
		this.eye = root.getChild("eye");
		this.ringA = root.getChild("ring_a");
		this.ringB = root.getChild("ring_b");
		this.cannonLeft = root.getChild("cannon_l");
		this.cannonRight = root.getChild("cannon_r");
		this.pylonTop = root.getChild("pylon_top");
		this.pylonBottom = root.getChild("pylon_bottom");
	}

	public static TexturedModelData getTexturedModelData() {
		ModelData data = new ModelData();
		ModelPartData root = data.getRoot();
		root.addChild("core", ModelPartBuilder.create().uv(0, 0).cuboid(-10.0f, -10.0f, -10.0f, 20.0f, 20.0f, 20.0f), ModelTransform.pivot(0.0f, 3.0f, 0.0f));
		root.addChild("eye", ModelPartBuilder.create().uv(80, 0).cuboid(-5.0f, -5.0f, -12.0f, 10.0f, 10.0f, 2.0f), ModelTransform.pivot(0.0f, 3.0f, 0.0f));
		root.addChild("ring_a", ModelPartBuilder.create().uv(0, 44).cuboid(-14.0f, -1.5f, -14.0f, 28.0f, 3.0f, 28.0f), ModelTransform.pivot(0.0f, 3.0f, 0.0f));
		root.addChild("ring_b", ModelPartBuilder.create().uv(0, 78).cuboid(-1.5f, -12.0f, -12.0f, 3.0f, 24.0f, 24.0f), ModelTransform.pivot(0.0f, 3.0f, 0.0f));
		root.addChild("cannon_l", ModelPartBuilder.create().uv(80, 16).cuboid(-2.0f, -2.0f, -12.0f, 4.0f, 4.0f, 12.0f), ModelTransform.pivot(-13.0f, 3.0f, -4.0f));
		root.addChild("cannon_r", ModelPartBuilder.create().uv(80, 16).mirrored().cuboid(-2.0f, -2.0f, -12.0f, 4.0f, 4.0f, 12.0f), ModelTransform.pivot(13.0f, 3.0f, -4.0f));
		root.addChild("pylon_top", ModelPartBuilder.create().uv(60, 78).cuboid(-1.5f, -8.0f, -1.5f, 3.0f, 8.0f, 3.0f), ModelTransform.pivot(0.0f, -7.0f, 0.0f));
		root.addChild("pylon_bottom", ModelPartBuilder.create().uv(60, 78).cuboid(-1.5f, 0.0f, -1.5f, 3.0f, 8.0f, 3.0f), ModelTransform.pivot(0.0f, 13.0f, 0.0f));
		return TexturedModelData.of(data, 128, 128);
	}

	@Override
	public ModelPart getPart() {
		return this.root;
	}

	@Override
	public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
		float bob = MathHelper.sin(animationProgress * 0.08f) * 1.5f;
		float y = 3.0f + bob;
		this.core.pivotY = y;
		this.eye.pivotY = y;
		this.ringA.pivotY = y;
		this.ringB.pivotY = y;
		this.cannonLeft.pivotY = y;
		this.cannonRight.pivotY = y;
		this.pylonTop.pivotY = y - 10.0f;
		this.pylonBottom.pivotY = y + 10.0f;

		this.ringA.yaw = animationProgress * 0.06f;
		this.ringB.roll = -animationProgress * 0.09f;
		float pitch = headPitch * ((float) Math.PI / 180.0f);
		this.cannonLeft.pitch = pitch;
		this.cannonRight.pitch = pitch;
		this.core.pitch = pitch * 0.4f;
		this.eye.pitch = pitch * 0.4f;
	}
}
