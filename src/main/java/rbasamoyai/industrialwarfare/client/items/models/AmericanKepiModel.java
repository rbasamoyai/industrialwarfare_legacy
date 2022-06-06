package rbasamoyai.industrialwarfare.client.items.models;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;

public class AmericanKepiModel extends BipedModel<LivingEntity> {
	
	public ModelRenderer kepi;
	public ModelRenderer cubeR1;
	
	public AmericanKepiModel(float inflate) {
		super(inflate);
		
		this.texWidth = 64;
		this.texHeight = 64;

		this.kepi = new ModelRenderer(this);
		this.kepi.setPos(0.0F, 0.75F, 0.0F);
		this.kepi.texOffs(0, 32).addBox(-4.0F, -6.0F, -6.0F, 8.0F, 1.0F, 10.0F, 0.0F, false);
		this.kepi.texOffs(0, 43).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 2.0F, 8.0F, 0.0F, false);

		this.cubeR1 = new ModelRenderer(this);
		this.cubeR1.setPos(0.0F, 0.0F, 0.0F);
		this.kepi.addChild(cubeR1);
		this.setRotationAngle(cubeR1, 0.1745F, 0.0F, 0.0F);
		this.cubeR1.texOffs(0, 53).addBox(-3.5F, -9.75F, -2.5F, 7.0F, 3.0F, 7.0F, 0.0F, false);
		
		this.head.addChild(this.kepi);
	}

	@Override
	public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		matrixStack.pushPose();
		if (this.young) {
			matrixStack.translate(0.0f, -0.1875f, 0.0f);
		}
		matrixStack.translate(this.head.x / 16.0f, this.head.y / 16.0f, this.head.z / 16.0f);
		matrixStack.scale(1.25f, 1.25f, 1.25f);
		matrixStack.translate(-this.head.x / 16.0f, -this.head.y / 16.0f, -this.head.z / 16.0f);
		super.renderToBuffer(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
		matrixStack.popPose();
	}
	
	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.xRot = x;
		modelRenderer.yRot = y;
		modelRenderer.zRot = z;
	}
	
}
