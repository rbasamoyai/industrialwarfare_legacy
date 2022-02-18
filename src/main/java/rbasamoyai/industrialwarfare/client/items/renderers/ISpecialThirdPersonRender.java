package rbasamoyai.industrialwarfare.client.items.renderers;

import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import rbasamoyai.industrialwarfare.common.entities.ThirdPersonItemAnimEntity;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.geo.render.built.GeoBone;

public interface ISpecialThirdPersonRender {

	boolean shouldSpecialRender(ItemStack stack, LivingEntity entity);
	
	default void onPreRender(LivingEntity entity, IAnimatable animatable, float entityYaw, float partialTicks, MatrixStack stack,
			IRenderTypeBuffer bufferIn, int packedLightIn) {}
	
	default void onJustAfterRender(LivingEntity entity, IAnimatable animatable, float entityYaw, float partialTicks, MatrixStack stack,
			IRenderTypeBuffer bufferIn, int packedLightIn) {}
	
	default void onPostRender(LivingEntity entity, IAnimatable animatable, float entityYaw, float partialTicks, MatrixStack stack,
			IRenderTypeBuffer bufferIn, int packedLightIn) {}
	
	ResourceLocation getAnimationFileLocation(ItemStack stack, LivingEntity entity);
	ResourceLocation getModelLocation(ItemStack stack, LivingEntity entity);
	ResourceLocation getTextureLocation(ItemStack stack, LivingEntity entity);
	
	AnimationBuilder getDefaultAnimation(ItemStack stack, LivingEntity entity, AnimationController<?> controller);
	
	List<AnimationController<ThirdPersonItemAnimEntity>> getAnimationControlllers(ItemStack stack, LivingEntity entity);
	
	void onRenderRecursively(ItemStack item, LivingEntity entity, float partialTicks, GeoBone bone, MatrixStack stack, IRenderTypeBuffer bufferIn,
			int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha);
	
	float getBoneAlpha(ItemStack item, LivingEntity entity, GeoBone bone, float argAlpha);
	
}
