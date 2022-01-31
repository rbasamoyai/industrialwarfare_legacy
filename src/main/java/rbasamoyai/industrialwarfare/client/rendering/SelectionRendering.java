package rbasamoyai.industrialwarfare.client.rendering;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.Entity;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;

public class SelectionRendering {

	private static final float[][] POINTS_TABLE = Util.make(new float[16][2], table -> {
		for (int i = 0; i < table.length; ++i) {
			float f = (float) Math.PI * 2.0f * (float) i / (float) table.length;
			table[i] = new float[] {MathHelper.sin(f), MathHelper.cos(f)};
		}
	});
	
	public static void renderSelectionCircle(Entity entity, MatrixStack stack, IRenderTypeBuffer buf) {
		IVertexBuilder builder = buf.getBuffer(SelectionRenderType.TYPE);
		float radius = MathHelper.SQRT_OF_TWO * entity.getBbWidth() * 0.5f;
		
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
