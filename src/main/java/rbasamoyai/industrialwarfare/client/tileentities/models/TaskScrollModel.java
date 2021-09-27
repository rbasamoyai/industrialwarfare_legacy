package rbasamoyai.industrialwarfare.client.tileentities.models;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;

public class TaskScrollModel extends Model {

	public ModelRenderer scroll;
	
	public TaskScrollModel() {
		super(RenderType::entityCutout);
		
		this.texWidth = 32;
		this.texHeight = 32;
		
		this.scroll = new ModelRenderer(this);
		this.scroll.texOffs(0, 0);
		this.scroll.addBox(0, 0, 0, 8, 2, 2, 0);
	}

	@Override
	public void renderToBuffer(MatrixStack stack, IVertexBuilder buf, int combinedLightIn, int combinedOverlayIn, float r, float g, float b, float a) {
		this.scroll.render(stack, buf, combinedLightIn, combinedOverlayIn);
	}

}
