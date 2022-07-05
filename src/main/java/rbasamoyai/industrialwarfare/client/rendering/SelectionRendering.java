package rbasamoyai.industrialwarfare.client.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;

import net.minecraft.Util;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class SelectionRendering {

	private static final Vector3f[] POINTS_TABLE = Util.make(new Vector3f[16], table -> {
		for (int i = 0; i < table.length; ++i) {
			float f = (float) Math.PI * 2.0f * (float) i / (float) table.length;
			table[i] = new Vector3f(Mth.sin(f), 0.0f, Mth.cos(f));
		}
	});
	
	private static final Vector3f[] NORMALS_TABLE = Util.make(new Vector3f[POINTS_TABLE.length], table -> {
		for (int i = 0 ; i < table.length; ++i) {
			int n = (i + 1) % POINTS_TABLE.length;
			Vector3f start = POINTS_TABLE[i].copy();
			Vector3f end = POINTS_TABLE[n].copy();
			float x = end.x() - start.x();
			float y = end.y() - start.y();
			float z = end.z() - start.z();
			float l = 1.0f / Mth.sqrt(x * x + y * y + z * z);
			table[i] = new Vector3f(x * l, y * l, z *l);
		}
	});
	
	public static void renderSelectionCircle(Entity entity, PoseStack stack, MultiBufferSource buf) {
		VertexConsumer builder = buf.getBuffer(SelectionRenderType.TYPE);
		float radius = Mth.SQRT_OF_TWO * entity.getBbWidth() * 0.5f;
		
		Matrix4f lastPose = stack.last().pose();
		Matrix3f normalMat = stack.last().normal();
		
		for (int i = 0; i < POINTS_TABLE.length; ++i) {
			int n = (i + 1) % POINTS_TABLE.length;
			Vector3f start = POINTS_TABLE[i].copy();
			Vector3f end = POINTS_TABLE[n].copy();
			start.mul(radius);
			end.mul(radius);
			
			Vector3f normalVec = NORMALS_TABLE[i];
			
			builder
					.vertex(lastPose, start.x(), 0.05f, start.z())
					.color(0.25f, 0.25f, 0.25f, 1.0f)
					.normal(normalMat, normalVec.x(), normalVec.y(), normalVec.z())
					.endVertex();
			builder
					.vertex(lastPose, end.x(), 0.05f, end.z())
					.color(0.25f, 0.25f, 0.25f, 1.0f)
					.normal(normalMat, normalVec.x(), normalVec.y(), normalVec.z())
					.endVertex();
		}
		
		for (int i = 0; i < POINTS_TABLE.length; ++i) {
			int n = (i + 1) % POINTS_TABLE.length;
			Vector3f start = POINTS_TABLE[i].copy();
			Vector3f end = POINTS_TABLE[n].copy();
			start.mul(radius);
			end.mul(radius);
			
			Vector3f normalVec = NORMALS_TABLE[i];
			
			builder
					.vertex(lastPose, start.x(), 0.1f, start.z())
					.color(1.0f, 1.0f, 1.0f, 1.0f)
					.normal(normalMat, normalVec.x(), normalVec.y(), normalVec.z())
					.endVertex();
			builder
					.vertex(lastPose, end.x(), 0.1f, end.z())
					.color(1.0f, 1.0f, 1.0f, 1.0f)
					.normal(normalMat, normalVec.x(), normalVec.y(), normalVec.z())
					.endVertex();
		}
	}
	
}
