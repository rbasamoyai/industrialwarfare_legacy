package rbasamoyai.industrialwarfare.client.rendering;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.Entity;

public class NothingLayer<T extends Entity, M extends EntityModel<T>> extends LayerRenderer<T , M> {

	private final LayerRenderer<?, ?> replacedLayer;
	
	public NothingLayer(IEntityRenderer<T, M> renderer, LayerRenderer<?, ?> replacedLayer) {
		super(renderer);
		this.replacedLayer = replacedLayer;
	}

	@Override
	public void render(MatrixStack p_225628_1_, IRenderTypeBuffer p_225628_2_, int p_225628_3_, T p_225628_4_,
			float p_225628_5_, float p_225628_6_, float p_225628_7_, float p_225628_8_, float p_225628_9_,
			float p_225628_10_) {
		
	}
	
	public LayerRenderer<?, ?> getReplacedLayer() { return this.replacedLayer; }
	
}
