package rbasamoyai.industrialwarfare.client.tileentities.renderers;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.client.tileentities.models.TaskScrollModel;
import rbasamoyai.industrialwarfare.common.blocks.TaskScrollShelfBlock;
import rbasamoyai.industrialwarfare.common.containers.taskscrollshelf.TaskScrollShelfContainer;
import rbasamoyai.industrialwarfare.common.containers.taskscrollshelf.TaskScrollShelfItemHandler;
import rbasamoyai.industrialwarfare.common.tileentities.TaskScrollShelfTileEntity;

public class TaskScrollShelfTileEntityRenderer extends TileEntityRenderer<TaskScrollShelfTileEntity> {

	private static final float SCROLL_X_START = 12.625f;
	private static final float SCROLL_DX = -3.75f;
	private static final float SCROLL_Y_START = 12.25f;
	private static final float SCROLL_DY = -3.75f;
	
	@SuppressWarnings("deprecation")
	private static final RenderMaterial SCROLL_RESOURCE_LOCATION =
			new RenderMaterial(AtlasTexture.LOCATION_BLOCKS, new ResourceLocation(IndustrialWarfare.MOD_ID, "entity/task_scroll"));
	
	private final TaskScrollModel[] scrolls = new TaskScrollModel[TaskScrollShelfContainer.SHELF_SLOT_COUNT];
	
	public TaskScrollShelfTileEntityRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
		
		for (int i = 0; i < this.scrolls.length; i++) {
			float x = SCROLL_X_START + SCROLL_DX * MathHelper.floor(i % TaskScrollShelfContainer.SHELF_COLUMNS);
			float y = SCROLL_Y_START + SCROLL_DY * MathHelper.floor(i / TaskScrollShelfContainer.SHELF_COLUMNS);
			TaskScrollModel model = new TaskScrollModel(RenderType::entitySolid, x, y);
			this.scrolls[i] = model;
		}
	}

	@Override
	public void render(TaskScrollShelfTileEntity te, float partialTicks, MatrixStack stack, IRenderTypeBuffer buf, int combinedLightIn, int combinedOverlayIn) {
		TaskScrollShelfItemHandler handler = te.getItemHandler();
		BlockState state = te.getBlockState();
		
		stack.pushPose();
		Direction d = state.getValue(TaskScrollShelfBlock.HORIZONTAL_FACING);
		float f = d.toYRot() + (d.getAxis() == Axis.X ? 0.0f : 180.0f);
		
		stack.translate(0.5d, 0.5d, 0.5d);
		stack.mulPose(Vector3f.YP.rotationDegrees(f));
		stack.translate(-0.5d, -0.5d, -0.5d);
		
		for (int i = 0; i < handler.getSlots(); i++) {
			ItemStack slotItem = handler.getStackInSlot(i);
			if (!slotItem.isEmpty()) {
				
				TaskScrollModel scroll = this.scrolls[i];
				
				IVertexBuilder builder = SCROLL_RESOURCE_LOCATION.buffer(buf, RenderType::entitySolid);
				scroll.renderToBuffer(stack, builder, combinedLightIn, combinedOverlayIn, 1.0f, 1.0f, 1.0f, 1.0f);
			}
		}
		
		stack.popPose();
	}
	
}