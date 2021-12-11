package rbasamoyai.industrialwarfare.client.items.renderers;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import rbasamoyai.industrialwarfare.client.items.models.FirearmModel;
import rbasamoyai.industrialwarfare.common.items.firearms.FirearmItem;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;
import software.bernie.geckolib3.util.RenderUtils;

public class FirearmRenderer extends GeoItemRenderer<FirearmItem> implements IRendersPlayerArms {

	private static final float SCALE_RECIPROCAL = 1.0f / 16.0f;
	
	protected boolean hideBullet = false;
	protected boolean hideSpeedloader = true;
	protected boolean renderArms = false;
	
	protected final Minecraft mc = Minecraft.getInstance();
	
	protected IRenderTypeBuffer currentBuffer;
	protected RenderType renderType;
	protected TransformType transformType;
	
	protected FirearmItem animatable;
	
	public FirearmRenderer() {
		super(new FirearmModel());
	}
	
	@Override
	public void renderByItem(ItemStack itemStack, TransformType transformType, MatrixStack matrixStack, 
			IRenderTypeBuffer bufferIn, int combinedLightIn, int p_239207_6_) {
		this.transformType = transformType;
		super.renderByItem(itemStack, transformType, matrixStack, bufferIn, combinedLightIn, p_239207_6_);
	}
	
	@Override
	public void render(GeoModel model, FirearmItem animatable, float partialTicks, RenderType type,
			MatrixStack matrixStackIn, IRenderTypeBuffer renderTypeBuffer, IVertexBuilder vertexBuilder,
			int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
		
		this.currentBuffer = renderTypeBuffer;
		this.renderType = type;
		this.animatable = animatable;
		
		super.render(model, animatable, partialTicks, type, matrixStackIn, renderTypeBuffer, vertexBuilder, packedLightIn,
				packedOverlayIn, red, green, blue, alpha);
		
		if (this.renderArms) {
			this.renderArms = false;
		}
	}
	
	@Override
	public void render(FirearmItem animatable, MatrixStack stack, IRenderTypeBuffer bufferIn, int packedLightIn, ItemStack itemStack) {
		super.render(animatable, stack, bufferIn, packedLightIn, itemStack);
		
		FirearmItem.getDataHandler(itemStack).ifPresent(h -> {
			IItemHandler attachments = h.getAttachmentsHandler();
			
			int slotSize = attachments.getSlots();
			if (slotSize >= 1) {
				ItemStack melee = attachments.getStackInSlot(0);
				ItemStackTileEntityRenderer ister = melee.getItem().getItemStackTileEntityRenderer();
			}
		});
	}
	
	@Override
	public void renderRecursively(GeoBone bone, MatrixStack stack, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
		if (bone.name.equals("bullet")) {
			bone.isHidden = this.hideBullet;
		}
		
		if (bone.name.equals("arm_left") || bone.name.equals("arm_right")) {
			bone.isHidden = true;			
		}
		
		if (this.renderArms && !this.mc.isPaused()) {
			AbstractClientPlayerEntity player = this.mc.player;
			
			if (!player.isInvisible()) {
				this.mc.textureManager.bind(player.getSkinTextureLocation());
				PlayerRenderer playerRenderer = (PlayerRenderer) this.mc.getEntityRenderDispatcher().<AbstractClientPlayerEntity>getRenderer(player);
				PlayerModel<AbstractClientPlayerEntity> model = playerRenderer.getModel();
				
				stack.pushPose();
				
				RenderUtils.translate(bone, stack);
				RenderUtils.moveToPivot(bone, stack);
				RenderUtils.rotate(bone, stack);
				RenderUtils.scale(bone, stack);
				RenderUtils.moveBackFromPivot(bone, stack);

				if (bone.name.equals("arm_left")) {
					stack.translate(-1.0f * SCALE_RECIPROCAL, 2.0f * SCALE_RECIPROCAL, 6.0f * SCALE_RECIPROCAL);
					this.renderPartOverBone(model.leftArm, bone, stack, this.currentBuffer.getBuffer(RenderType.entitySolid(player.getSkinTextureLocation())), packedLightIn);
					this.renderPartOverBone(model.leftSleeve, bone, stack, this.currentBuffer.getBuffer(RenderType.entityTranslucent(player.getSkinTextureLocation())), packedLightIn);
				} else if (bone.name.equals("arm_right")) {
					stack.translate(1.0f * SCALE_RECIPROCAL, 2.0f * SCALE_RECIPROCAL, -0.0f);
					this.renderPartOverBone(model.rightArm, bone, stack, this.currentBuffer.getBuffer(RenderType.entitySolid(player.getSkinTextureLocation())), packedLightIn);
					this.renderPartOverBone(model.rightSleeve, bone, stack, this.currentBuffer.getBuffer(RenderType.entityTranslucent(player.getSkinTextureLocation())), packedLightIn);
				}
				
				stack.popPose();
				
				this.mc.textureManager.bind(this.getTextureLocation(this.animatable));
			}
			super.renderRecursively(bone, stack, this.currentBuffer.getBuffer(this.renderType), packedLightIn, packedOverlayIn, red, green, blue, alpha);
		} else {
			super.renderRecursively(bone, stack, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
		}
	}
	
	@Override
	public Integer getUniqueID(FirearmItem animatable) {
		if (this.transformType == TransformType.GUI) {
			return -1;
		}
		return super.getUniqueID(animatable);
	}
	
	private void renderPartOverBone(ModelRenderer model, GeoBone bone, MatrixStack stack, IVertexBuilder buffer, int packedLightIn) {
		model.setPos(bone.rotationPointX, bone.rotationPointY, bone.rotationPointZ);
		model.xRot = 0.0f;
		model.yRot = 0.0f;
		model.zRot = 0.0f;
		model.render(stack, buffer, packedLightIn, OverlayTexture.NO_OVERLAY);
	}
	
	public void hideBullet(boolean hidebullet) { this.hideBullet = hidebullet; }
	public void hideSpeedloader(boolean hideSpeedloader) { this.hideSpeedloader = hideSpeedloader; }
	
	@Override public void setRenderArms(boolean renderArms) { this.renderArms = renderArms; }
	
	public TransformType getCurrentTransform() { return this.transformType; }
	
}
