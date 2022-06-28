package rbasamoyai.industrialwarfare.client.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;

import net.minecraft.Util;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class SelectionRendering {

	private static final float[][] POINTS_TABLE = Util.make(new float[16][2], table -> {
		for (int i = 0; i < table.length; ++i) {
			float f = (float) Math.PI * 2.0f * (float) i / (float) table.length;
			table[i] = new float[] {Mth.sin(f), Mth.cos(f)};
		}
	});
	
	public static void renderSelectionCircle(Entity entity, PoseStack stack, MultiBufferSource buf) {
		VertexConsumer builder = buf.getBuffer(SelectionRenderType.TYPE);
		float radius = Mth.SQRT_OF_TWO * entity.getBbWidth() * 0.5f;
		
		Matrix4f lastPose = stack.last().pose();
		
		for (int i = 0; i < POINTS_TABLE.length; ++i) {
			int n = (i + 1) % POINTS_TABLE.length;
			float sx = POINTS_TABLE[i][0] * radius;
			float sz = POINTS_TABLE[i][1] * radius;
			float ex = POINTS_TABLE[n][0] * radius;
			float ez = POINTS_TABLE[n][1] * radius;
			builder
					.vertex(lastPose, sx, 0.05f, sz)
					.color(0.25f, 0.25f, 0.25f, 1.0f)
					.endVertex();
			builder
					.vertex(lastPose, ex, 0.05f, ez)
					.color(0.25f, 0.25f, 0.25f, 1.0f)
					.endVertex();
		}
		
		for (int i = 0; i < POINTS_TABLE.length; ++i) {
			int n = (i + 1) % POINTS_TABLE.length;
			float sx = POINTS_TABLE[i][0] * radius;
			float sz = POINTS_TABLE[i][1] * radius;
			float ex = POINTS_TABLE[n][0] * radius;
			float ez = POINTS_TABLE[n][1] * radius;
			builder
					.vertex(lastPose, sx, 0.1f, sz)
					.color(1.0f, 1.0f, 1.0f, 1.0f)
					.endVertex();
			builder
					.vertex(lastPose, ex, 0.1f, ez)
					.color(1.0f, 1.0f, 1.0f, 1.0f)
					.endVertex();
		}
	}
	
}
