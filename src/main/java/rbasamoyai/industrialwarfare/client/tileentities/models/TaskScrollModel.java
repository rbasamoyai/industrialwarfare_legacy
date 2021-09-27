package rbasamoyai.industrialwarfare.client.tileentities.models;

import java.util.function.Function;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3i;

public class TaskScrollModel extends Model {

	private static final float SCROLL_Z = 7.0f;
	private static final Vector3i SCROLL_SIZE = new Vector3i(2, 2, 8);
	
	private final ModelRenderer scroll;
	
	public TaskScrollModel(Function<ResourceLocation, RenderType> renderTypeFunc, float x, float y) {
		super(renderTypeFunc);
		
		this.texWidth = 32;
		this.texHeight = 32;
		
		this.scroll = new ModelRenderer(this.texWidth, this.texHeight, 0, 0);
		this.scroll.addBox(0.0f, 0.0f, 0.0f, SCROLL_SIZE.getX(), SCROLL_SIZE.getY(), SCROLL_SIZE.getZ());
		this.scroll.setPos(x, y, SCROLL_Z);
	}

	@Override
	public void renderToBuffer(MatrixStack stack, IVertexBuilder builder, int combinedLightIn, int combinedOverlayIn, float r, float g, float b, float a) {
		this.scroll.render(stack, builder, combinedLightIn, combinedOverlayIn);
	}

}
