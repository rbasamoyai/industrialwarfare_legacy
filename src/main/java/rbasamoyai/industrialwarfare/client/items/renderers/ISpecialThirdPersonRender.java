package rbasamoyai.industrialwarfare.client.items.renderers;

import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import rbasamoyai.industrialwarfare.client.entities.renderers.ThirdPersonItemAnimRenderer;
import rbasamoyai.industrialwarfare.common.entities.ThirdPersonItemAnimEntity;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.geo.render.built.GeoBone;

public interface ISpecialThirdPersonRender {

	boolean shouldSpecialRender(ItemStack stack, LivingEntity entity);
	
	default void onPreRender(LivingEntity entity, IAnimatable animatable, float entityYaw, float partialTicks, MatrixStack stack,
			IRenderTypeBuffer bufferIn, int packedLightIn, ThirdPersonItemAnimRenderer renderer) {}
	
	default void onJustAfterRender(LivingEntity entity, IAnimatable animatable, float entityYaw, float partialTicks, MatrixStack stack,
			IRenderTypeBuffer bufferIn, int packedLightIn, ThirdPersonItemAnimRenderer renderer) {}
	
	default void onPostRender(LivingEntity entity, IAnimatable animatable, float entityYaw, float partialTicks, MatrixStack stack,
			IRenderTypeBuffer bufferIn, int packedLightIn, ThirdPersonItemAnimRenderer renderer) {}
	
	ResourceLocation getAnimationFileLocation(ItemStack stack, LivingEntity entity);
	ResourceLocation getModelLocation(ItemStack stack, LivingEntity entity);
	ResourceLocation getTextureLocation(ItemStack stack, LivingEntity entity);
	
	AnimationBuilder getDefaultAnimation(ItemStack stack, LivingEntity entity, AnimationController<?> controller);
	
	List<AnimationController<ThirdPersonItemAnimEntity>> getAnimationControlllers(ItemStack stack, LivingEntity entity);
	
	void onRenderRecursively(ItemStack item, LivingEntity entity, float partialTicks, GeoBone bone, MatrixStack stack, IRenderTypeBuffer bufferIn,
			int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha, ThirdPersonItemAnimRenderer renderer);
	
	float getBoneAlpha(ItemStack item, LivingEntity entity, GeoBone bone, float argAlpha);
	
	default void interpretThirdPersonInstructions(List<String> tokens, ItemStack stack, ThirdPersonItemAnimRenderer renderer) {
		String firstTok = tokens.get(0);
		
		if (tokens.size() < 2) return;
		
		String boneName = tokens.get(1);
		
		if (firstTok.equals("set_hidden")) {
			boolean hidden = Boolean.valueOf(tokens.get(2));
			renderer.hideBone(boneName, hidden);
		} else if (firstTok.equals("lock_limbs")) {
			boolean lock = Boolean.valueOf(tokens.get(1));
			renderer.lockLimbs(lock);
		} else if (firstTok.equals("move")) {
			float x = Float.valueOf(tokens.get(2));
			float y = Float.valueOf(tokens.get(3));
			float z = Float.valueOf(tokens.get(4));
			renderer.setBonePosition(boneName, x, y, z);
		} else if (firstTok.equals("rotate")) {
			float x = Float.valueOf(tokens.get(2));
			float y = Float.valueOf(tokens.get(3));
			float z = Float.valueOf(tokens.get(4));
			renderer.setBoneRotation(boneName, x, y, z);
		}  else if (firstTok.equals("suppress_mod")) {
			renderer.suppressModification(boneName);
		} else if (firstTok.equals("allow_mod")) {
			renderer.allowModification(boneName);
		}
	}
	
}
