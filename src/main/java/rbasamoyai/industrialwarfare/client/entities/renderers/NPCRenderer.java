package rbasamoyai.industrialwarfare.client.entities.renderers;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.entity.BipedRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.layers.BipedArmorLayer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.util.ResourceLocation;
import rbasamoyai.industrialwarfare.client.entities.models.NPCModel;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;

/*
 * Base NPC renderer class
 */

public class NPCRenderer<T extends NPCEntity> extends BipedRenderer<T, NPCModel<T>> {

	public static final ResourceLocation DEFAULT_TEXTURE = new ResourceLocation("minecraft", "textures/entity/steve.png");
	
	public NPCRenderer(EntityRendererManager manager) {
		super(manager, new NPCModel<>(), 0.5f);
		this.addLayer(new BipedArmorLayer<>(this, new BipedModel<>(0.5f), new BipedModel<>(1.0f)));
	}
	
	// Taken from PlayerRenderer so that the NPC can be at player proportions
	@Override
	protected void scale(T entity, MatrixStack stack, float scale) {
		float f = 0.9375F;
		stack.scale(f, f, f);
	}

	@Override
	public ResourceLocation getTextureLocation(T entity) {
		return DEFAULT_TEXTURE;
	}
	
}
