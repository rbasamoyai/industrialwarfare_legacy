package rbasamoyai.industrialwarfare.client.tileentities.renderers;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.ResourceLocation;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.tileentities.TaskScrollShelfTileEntity;

public class TaskScrollShelfTileEntityRenderer extends TileEntityRenderer<TaskScrollShelfTileEntity> {

	private static final ResourceLocation TASK_SCROLL_SHELF_MODEL = new ResourceLocation(IndustrialWarfare.MOD_ID, "block/task_scroll_shelf");
	private Minecraft mc = Minecraft.getInstance();
	
	public TaskScrollShelfTileEntityRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	public void render(TaskScrollShelfTileEntity te, float partialTicks, MatrixStack stack, IRenderTypeBuffer buf, int combinedLightIn, int combinedOverlayIn) {
		
	}

}
