package rbasamoyai.industrialwarfare.client.entities.models;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.entity.model.BipedModel;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;

public class NPCModel<T extends NPCEntity> extends BipedModel<T> {
	
	public NPCModel() {
		super(0.0f, 0.0f, 64, 64);
	}

	@Override
	public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		this.head.render(matrixStack, buffer, packedLight, packedOverlay);
		this.body.render(matrixStack, buffer, packedLight, packedOverlay);
		this.leftArm.render(matrixStack, buffer, packedLight, packedOverlay);
		this.rightArm.render(matrixStack, buffer, packedLight, packedOverlay);
		this.leftLeg.render(matrixStack, buffer, packedLight, packedOverlay);
		this.rightLeg.render(matrixStack, buffer, packedLight, packedOverlay);
	}
	
}
