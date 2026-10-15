package com.astratech.client.sky;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.DimensionRenderingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

import java.util.Random;

/** Black airless sky: dense starfield, a huge Earth, and a hard white sun. */
public class MoonSky implements DimensionRenderingRegistry.SkyRenderer {
	private static final Identifier EARTH = new Identifier("astratech", "textures/environment/earth.png");
	private static final Identifier SUN = new Identifier("astratech", "textures/environment/moon_sun.png");
	private static final int STAR_COUNT = 3500;
	private static final float[] STAR_DATA = buildStars();

	private static float[] buildStars() {
		Random random = new Random(424242L);
		float[] data = new float[STAR_COUNT * 4];
		int count = 0;
		while (count < STAR_COUNT) {
			double x = random.nextDouble() * 2.0 - 1.0;
			double y = random.nextDouble() * 2.0 - 1.0;
			double z = random.nextDouble() * 2.0 - 1.0;
			double lengthSq = x * x + y * y + z * z;
			if (lengthSq > 1.0 || lengthSq < 0.01) {
				continue;
			}
			double inv = 1.0 / Math.sqrt(lengthSq);
			data[count * 4] = (float) (x * inv);
			data[count * 4 + 1] = (float) (y * inv);
			data[count * 4 + 2] = (float) (z * inv);
			data[count * 4 + 3] = 0.05f + random.nextFloat() * random.nextFloat() * 0.18f;
			count++;
		}
		return data;
	}

	@Override
	public void render(WorldRenderContext context) {
		MatrixStack matrices = context.matrixStack();
		Matrix4f projection = context.projectionMatrix();
		float tickDelta = context.tickDelta();

		RenderSystem.disableBlend();
		RenderSystem.depthMask(false);

		// Stars (slow drift so the sky is not frozen)
		matrices.push();
		float drift = ((context.world().getTime() + tickDelta) * 0.0015f) % 360.0f;
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(drift));
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(drift * 0.4f));
		drawStars(matrices.peek().getPositionMatrix());
		matrices.pop();

		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

		// Sun: high and fierce
		drawBillboard(matrices, SUN, 70.0f, 20.0f, 36.0f);
		// Earth: big, blue and fixed in the sky (the Moon is tidally locked)
		drawBillboard(matrices, EARTH, 150.0f, 52.0f, 24.0f);

		RenderSystem.depthMask(true);
		RenderSystem.disableBlend();
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
	}

	private void drawStars(Matrix4f matrix) {
		RenderSystem.setShader(GameRenderer::getPositionColorProgram);
		RenderSystem.disableCull();
		RenderSystem.disableDepthTest();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
		for (int i = 0; i < STAR_COUNT; i++) {
			float dx = STAR_DATA[i * 4];
			float dy = STAR_DATA[i * 4 + 1];
			float dz = STAR_DATA[i * 4 + 2];
			float size = STAR_DATA[i * 4 + 3];
			// tangent = normalize(dir x up); bitangent = dir x tangent
			float tx = dy * 0.0f - dz * 1.0f;
			float ty = dz * 0.0f - dx * 0.0f;
			float tz = dx * 1.0f - dy * 0.0f;
			float tl = (float) Math.sqrt(tx * tx + ty * ty + tz * tz);
			if (tl < 0.0001f) {
				tx = 1.0f;
				ty = 0.0f;
				tz = 0.0f;
				tl = 1.0f;
			}
			tx /= tl;
			ty /= tl;
			tz /= tl;
			float bx = dy * tz - dz * ty;
			float by = dz * tx - dx * tz;
			float bz = dx * ty - dy * tx;
			float cx = dx * 100.0f;
			float cy = dy * 100.0f;
			float cz = dz * 100.0f;
			float s = size * 1.6f;
			float tint = (i * 37 % 100) / 100.0f;
			float cr = tint < 0.2f ? 1.0f : (tint < 0.5f ? 0.8f : 1.0f);
			float cg = tint < 0.2f ? 0.85f : (tint < 0.5f ? 0.9f : 1.0f);
			float cb = tint < 0.2f ? 0.7f : 1.0f;
			buffer.vertex(matrix, cx - tx * s - bx * s, cy - ty * s - by * s, cz - tz * s - bz * s).color(cr, cg, cb, 1.0f).next();
			buffer.vertex(matrix, cx + tx * s - bx * s, cy + ty * s - by * s, cz + tz * s - bz * s).color(cr, cg, cb, 1.0f).next();
			buffer.vertex(matrix, cx + tx * s + bx * s, cy + ty * s + by * s, cz + tz * s + bz * s).color(cr, cg, cb, 1.0f).next();
			buffer.vertex(matrix, cx - tx * s + bx * s, cy - ty * s + by * s, cz - tz * s + bz * s).color(cr, cg, cb, 1.0f).next();
		}
		BufferRenderer.drawWithGlobalProgram(buffer.end());
		RenderSystem.enableDepthTest();
		RenderSystem.enableCull();
	}

	/** Draws a textured square at the given elevation (degrees above the horizon-plane tilt) and azimuth. */
	private void drawBillboard(MatrixStack matrices, Identifier texture, float yaw, float tiltFromZenith, float halfSize) {
		matrices.push();
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yaw));
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(tiltFromZenith));
		Matrix4f matrix = matrices.peek().getPositionMatrix();
		RenderSystem.setShader(GameRenderer::getPositionTexProgram);
		RenderSystem.setShaderTexture(0, texture);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
		buffer.vertex(matrix, -halfSize, 100.0f, -halfSize).texture(0.0f, 0.0f).next();
		buffer.vertex(matrix, halfSize, 100.0f, -halfSize).texture(1.0f, 0.0f).next();
		buffer.vertex(matrix, halfSize, 100.0f, halfSize).texture(1.0f, 1.0f).next();
		buffer.vertex(matrix, -halfSize, 100.0f, halfSize).texture(0.0f, 1.0f).next();
		BufferRenderer.drawWithGlobalProgram(buffer.end());
		matrices.pop();
	}
}
