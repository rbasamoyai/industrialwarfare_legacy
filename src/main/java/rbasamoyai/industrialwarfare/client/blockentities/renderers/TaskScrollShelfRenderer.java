package rbasamoyai.industrialwarfare.client.blockentities.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.client.ModModelLayers;
import rbasamoyai.industrialwarfare.common.blockentities.TaskScrollShelfBlockEntity;
import rbasamoyai.industrialwarfare.common.blocks.TaskScrollShelfBlock;
import rbasamoyai.industrialwarfare.common.containers.taskscrollshelf.TaskScrollShelfItemHandler;
import rbasamoyai.industrialwarfare.common.containers.taskscrollshelf.TaskScrollShelfMenu;

public class TaskScrollShelfRenderer implements BlockEntityRenderer<TaskScrollShelfBlockEntity> {

	private static final float SCROLL_X_START = 12.625f;
	private static final float SCROLL_DX = -3.75f;
	private static final float SCROLL_Y_START = 12.25f;
	private static final float SCROLL_DY = -3.75f;
	
	private static final Material SCROLL_RESOURCE_LOCATION = new Material(InventoryMenu.BLOCK_ATLAS, new ResourceLocation(IndustrialWarfare.MOD_ID, "entity/task_scroll"));
	
	private final ModelPart scroll;
	
	public TaskScrollShelfRenderer(BlockEntityRendererProvider.Context context) {
		this.scroll = context.bakeLayer(ModModelLayers.SCROLL);
	}

	@Override
	public void render(TaskScrollShelfBlockEntity te, float partialTicks, PoseStack stack, MultiBufferSource buf, int combinedLightIn, int combinedOverlayIn) {
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
				float x = SCROLL_X_START * 0.0625f + SCROLL_DX * Mth.floor(i % TaskScrollShelfMenu.SHELF_COLUMNS) * 0.0625f;
				float y = SCROLL_Y_START * 0.0625f + SCROLL_DY * Mth.floor(i / TaskScrollShelfMenu.SHELF_COLUMNS) * 0.0625f;
				
				stack.pushPose();
				stack.translate(x, y, 7.0f * 0.0625f);
				
				VertexConsumer builder = SCROLL_RESOURCE_LOCATION.buffer(buf, RenderType::entitySolid);
				this.scroll.render(stack, builder, combinedLightIn, combinedOverlayIn, 1.0f, 1.0f, 1.0f, 1.0f);
				
				stack.popPose();
			}
		}
		
		stack.popPose();
	}
	
	public static LayerDefinition createScrollLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition part = mesh.getRoot();
		part.addOrReplaceChild("scroll", CubeListBuilder.create().texOffs(0, 0).addBox(0.0f, 0.0f, 0.0f, 2.0f, 2.0f, 8.0f), PartPose.ZERO);
		return LayerDefinition.create(mesh, 32, 32);
	}
	
	public static class ScrollModel extends Model {
		public static final ResourceLocation TEXTURE = new ResourceLocation(IndustrialWarfare.MOD_ID, "textures/entity/task_scroll.png");
		public final ModelPart root;
		
		public ScrollModel(ModelPart root) {
			super(RenderType::entitySolid);
			this.root = root;
		}
		
		@Override
		public void renderToBuffer(PoseStack pPoseStack, VertexConsumer pBuffer, int pPackedLight, int pPackedOverlay, float pRed, float pGreen, float pBlue, float pAlpha) {
			this.root.render(pPoseStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);			
		}
	}
	
}