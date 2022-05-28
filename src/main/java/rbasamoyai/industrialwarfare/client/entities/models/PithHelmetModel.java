package rbasamoyai.industrialwarfare.client.entities.models;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;

public class PithHelmetModel extends BipedModel<LivingEntity> {
	public ModelRenderer helmet;

	public PithHelmetModel(float inflate) {
		super(inflate);
		this.texWidth = 64;
		this.texHeight = 64;

		this.helmet = new ModelRenderer(this);
		this.helmet.setPos(0.0f, -0.25f, -0.25f);
		this.helmet.texOffs(32, 44).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 5.0F, 8.0F, 0.0F, false);
		this.helmet.texOffs(0, 32).addBox(-4.5F, -4.0F, -5.0F, 9.0F, 2.0F, 10.0F, 0.0F, false);
		this.helmet.texOffs(0, 44).addBox(-4.0F, -7.0F, -4.0F, 8.0F, 3.0F, 8.0F, 0.0F, false);
		this.helmet.texOffs(0, 55).addBox(-3.0F, -9.0F, -3.0F, 6.0F, 2.0F, 6.0F, 0.0F, false);
		this.helmet.texOffs(0, 32).addBox(-1.0F, -9.5F, -1.0F, 2.0F, 1.0F, 2.0F, 0.0F, false);
		this.setRotationAngle(this.helmet, -0.0873F, 0.0F, 0.0F);
		
		this.head.addChild(this.helmet);
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
	
	@Override
	public void setupAnim(LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		this.setRotationAngle(this.helmet, -0.0873F, 0.0F, 0.0F);
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.xRot = x;
		modelRenderer.yRot = y;
		modelRenderer.zRot = z;
	}
	
}
