package rbasamoyai.industrialwarfare.client.items.renderers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.RenderProperties;
import rbasamoyai.industrialwarfare.client.items.models.FirearmModel;
import rbasamoyai.industrialwarfare.common.items.firearms.FirearmItem;
import rbasamoyai.industrialwarfare.utils.AnimUtils;
import rbasamoyai.industrialwarfare.utils.TextureUtils;
import software.bernie.geckolib3.core.IAnimatableModel;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;
import software.bernie.geckolib3.util.RenderUtils;

@SuppressWarnings("unchecked")
public class FirearmRenderer extends GeoItemRenderer<FirearmItem> implements RendersPlayerArms {

	static {
		AnimationController.addModelFetcher(animatable -> {
			if (animatable instanceof Item) {
				Item item = (Item) animatable;
				BlockEntityWithoutLevelRenderer ister = RenderProperties.get(item).getItemStackRenderer();
				if (ister instanceof GeoItemRenderer) {
					return (IAnimatableModel<Object>) ((GeoItemRenderer<?>) ister).getGeoModelProvider();
				}
			}
			return null;
		});
	}
	
	private static final float SCALE_RECIPROCAL = 1.0f / 16.0f;
	
	protected boolean renderArms = false;
	
	protected MultiBufferSource currentBuffer;
	protected RenderType renderType;
	protected TransformType transformType;
	
	protected FirearmItem animatable;
	
	private float aimProgress = 0.0f;
	
	private final Set<String> hiddenBones = new HashSet<>();
	private final Set<String> suppressedBones = new HashSet<>();
	private final Map<String, Vector3f> queuedBoneSetMovements = new HashMap<>();
	private final Map<String, Vector3f> queuedBoneSetRotations = new HashMap<>();
	private final Map<String, Vector3f> queuedBoneAddRotations = new HashMap<>();
	
	public FirearmRenderer() {
		super(new FirearmModel());
	}
	
	@Override
	public void renderByItem(ItemStack itemStack, TransformType transformType, PoseStack matrixStack, 
			MultiBufferSource bufferIn, int combinedLightIn, int p_239207_6_) {
		this.transformType = transformType;
		super.renderByItem(itemStack, transformType, matrixStack, bufferIn, combinedLightIn, p_239207_6_);
	}
	
	@Override
	public void render(GeoModel model, FirearmItem animatable, float partialTicks, RenderType type,
			PoseStack matrixStackIn, MultiBufferSource renderTypeBuffer, VertexConsumer vertexBuilder,
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
	public void render(FirearmItem animatable, PoseStack stack, MultiBufferSource bufferIn, int packedLightIn, ItemStack itemStack) {
		Minecraft mc = Minecraft.getInstance();
		float sign = FirearmItem.isAiming(itemStack) ? 1.0f : -1.0f;
		this.aimProgress = Mth.clamp(this.aimProgress + mc.getFrameTime() * sign * 0.1f, 0.0f, 1.0f);
		
		stack.pushPose();
		animatable.setupAnimationState(this, itemStack, stack, this.aimProgress);
		super.render(animatable, stack, bufferIn, packedLightIn, itemStack);
		stack.popPose();
	}
	
	@Override
	public void renderRecursively(GeoBone bone, PoseStack stack, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
		Minecraft mc = Minecraft.getInstance();
		
		String name = bone.getName();
		
		boolean renderingArms = false;
		if (name.equals("arm_left") || name.equals("arm_right")) {
			bone.setHidden(true);		
			renderingArms = true;
		} else {
			bone.setHidden(this.hiddenBones.contains(name));
		}
		
		if (!this.suppressedBones.contains(name)) {
			if (this.queuedBoneSetMovements.containsKey(name)) {
				Vector3f pos = this.queuedBoneSetMovements.get(name);
				bone.setPositionX(pos.x());
				bone.setPositionY(pos.y());
				bone.setPositionZ(pos.z());
			}
			
			if (this.queuedBoneSetRotations.containsKey(name)) {
				Vector3f rot = this.queuedBoneSetRotations.get(name);
				bone.setRotationX(rot.x());
				bone.setRotationY(rot.y());
				bone.setRotationZ(rot.z());
			}
			
			if (this.queuedBoneAddRotations.containsKey(name)) {
				Vector3f rot = this.queuedBoneAddRotations.get(name);
				bone.setRotationX(bone.getRotationX() + rot.x());
				bone.setRotationY(bone.getRotationY() + rot.y());
				bone.setRotationZ(bone.getRotationZ() + rot.z());
			}
		}
		
		if (this.renderArms && renderingArms && !mc.isPaused()) {
			AbstractClientPlayer player = mc.player;
			
			float armsAlpha = player.isInvisible() ? 0.15f : 1.0f;
			PlayerRenderer playerRenderer = (PlayerRenderer) mc.getEntityRenderDispatcher().getRenderer(player);
			PlayerModel<AbstractClientPlayer> model = playerRenderer.getModel();
			
			stack.pushPose();
			
			RenderUtils.translate(bone, stack);
			RenderUtils.moveToPivot(bone, stack);
			RenderUtils.rotate(bone, stack);
			RenderUtils.scale(bone, stack);
			RenderUtils.moveBackFromPivot(bone, stack);

			ResourceLocation loc = player.getSkinTextureLocation();
			VertexConsumer armBuilder = this.currentBuffer.getBuffer(RenderType.entitySolid(loc));
			VertexConsumer sleeveBuilder = this.currentBuffer.getBuffer(RenderType.entityTranslucent(loc));
			
			if (name.equals("arm_left")) {
				stack.translate(-1.0f * SCALE_RECIPROCAL, 2.0f * SCALE_RECIPROCAL, 0.0f);
				AnimUtils.renderPartOverBone(model.leftArm, bone, stack, armBuilder, packedLightIn, OverlayTexture.NO_OVERLAY, armsAlpha);
				AnimUtils.renderPartOverBone(model.leftSleeve, bone, stack, sleeveBuilder, packedLightIn, OverlayTexture.NO_OVERLAY, armsAlpha);
			} else if (name.equals("arm_right")) {
				stack.translate(1.0f * SCALE_RECIPROCAL, 2.0f * SCALE_RECIPROCAL, 0.0f);
				AnimUtils.renderPartOverBone(model.rightArm, bone, stack, armBuilder, packedLightIn, OverlayTexture.NO_OVERLAY, armsAlpha);
				AnimUtils.renderPartOverBone(model.rightSleeve, bone, stack, sleeveBuilder, packedLightIn, OverlayTexture.NO_OVERLAY, armsAlpha);
			}
			
			stack.popPose();
		}
		super.renderRecursively(bone, stack, this.currentBuffer.getBuffer(this.renderType), packedLightIn, packedOverlayIn, red, green, blue, alpha);
	}
	
	@Override
	public ResourceLocation getTextureLocation(FirearmItem instance) {
		return this.currentItemStack == null ? super.getTextureLocation(instance) : TextureUtils.getWeaponSkinTexture(this.currentItemStack);
	}
	
	@Override
	public Integer getUniqueID(FirearmItem animatable) {
		if (this.currentItemStack == null || this.transformType != TransformType.FIRST_PERSON_LEFT_HAND && this.transformType != TransformType.FIRST_PERSON_RIGHT_HAND) {
			return -1;
		}
		return super.getUniqueID(animatable);
	}
	
	public void hideBone(String name, boolean hide) {
		if (hide) {
			this.hiddenBones.add(name);
		} else {
			this.hiddenBones.remove(name);
		}
	}
	
	@Override public void setRenderArms(boolean renderArms) { this.renderArms = renderArms; }
	
	public TransformType getCurrentTransform() { return this.transformType; }
	
	public void suppressModification(String name) { this.suppressedBones.add(name); }
	public void allowModification(String name) { this.suppressedBones.remove(name); }
	
	public void setBonePosition(String name, float x, float y, float z) {
		this.queuedBoneSetMovements.put(name, new Vector3f(x, y, z));
	}
	
	public void addToBoneRotation(String name, float x, float y, float z) {
		this.queuedBoneAddRotations.put(name, new Vector3f(x, y, z));
	}
	
	public void setBoneRotation(String name, float x, float y, float z) {
		this.queuedBoneSetRotations.put(name, new Vector3f(x, y, z));
	}
	
	public ItemStack getCurrentItem() { return this.currentItemStack; }
	
	@Override
	public boolean shouldAllowHandRender(ItemStack mainhand, ItemStack offhand, InteractionHand renderingHand) {
		return renderingHand == InteractionHand.MAIN_HAND;
	}
	
}
