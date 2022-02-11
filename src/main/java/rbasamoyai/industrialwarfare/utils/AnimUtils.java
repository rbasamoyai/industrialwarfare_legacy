package rbasamoyai.industrialwarfare.utils;

import java.lang.reflect.Field;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import rbasamoyai.industrialwarfare.client.rendering.NothingLayer;
import software.bernie.geckolib3.geo.render.built.GeoBone;

public class AnimUtils {

	public static void renderPartOverBone(ModelRenderer model, GeoBone bone, MatrixStack stack, IVertexBuilder buffer,
			int packedLightIn, float alpha, int packedOverlayIn) {
		model.setPos(bone.rotationPointX, bone.rotationPointY, bone.rotationPointZ);
		model.xRot = 0.0f;
		model.yRot = 0.0f;
		model.zRot = 0.0f;
		model.render(stack, buffer, packedLightIn, packedOverlayIn, 1.0f, 1.0f, 1.0f, alpha);
	}

	public static void hideLayers(Class<?> clazz, LivingRenderer<?, ?> renderer) {
		// I want to throw up after this
		Field layerField = ObfuscationReflectionHelper.findField(LivingRenderer.class, "field_177097_h");
		try {
			@SuppressWarnings("unchecked")
			List<LayerRenderer<?, ?>> layers = (List<LayerRenderer<?, ?>>) layerField.get(renderer);
			for (int i = 0; i < layers.size(); ++i) {
				LayerRenderer<?, ?> layer = layers.get(i);
				Class<?> layerClass = layer.getClass();
				if (layerClass.equals(clazz)) layers.set(i, new NothingLayer<>(renderer, layer));
			}
		} catch (Exception e) {
			throw new RuntimeException("Something went wrong");
		}		
	}
	
	public static void restoreLayers(LivingRenderer<?, ?> renderer) {
		// I also want to throw up after this
		Field layerField = ObfuscationReflectionHelper.findField(LivingRenderer.class, "field_177097_h");
		try {
			@SuppressWarnings("unchecked")
			List<LayerRenderer<?, ?>> layers = (List<LayerRenderer<?, ?>>) layerField.get(renderer);
			for (int i = 0; i < layers.size(); ++i) {
				LayerRenderer<?, ?> layer = layers.get(i);
				if (layer instanceof NothingLayer) {
					layers.set(i, ((NothingLayer<?, ?>) layer).getReplacedLayer());  
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Something went wrong");
		}
	}
	
}
