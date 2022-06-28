package rbasamoyai.industrialwarfare.client.entities.renderers;

import java.util.Optional;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.common.entityai.NPCComplaint;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;

/*
 * Base NPC renderer class
 */

public class NPCRenderer<T extends NPCEntity> extends HumanoidMobRenderer<T, PlayerModel<T>> {

	public static final ResourceLocation DEFAULT_TEXTURE = new ResourceLocation("minecraft", "textures/entity/steve.png");
	
	public NPCRenderer(EntityRendererProvider.Context context) {
		this(context, false);
	}
	
	public NPCRenderer(EntityRendererProvider.Context context, boolean useSlim) {
		super(context, new PlayerModel<>(context.bakeLayer(useSlim ? ModelLayers.PLAYER_SLIM : ModelLayers.PLAYER), useSlim), 0.5f);
		this.addLayer(new HumanoidArmorLayer<>(this, new HumanoidModel<>(context.bakeLayer(useSlim ? ModelLayers.PLAYER_SLIM : ModelLayers.PLAYER)), new HumanoidModel<>(context.bakeLayer(useSlim ? ModelLayers.PLAYER_SLIM : ModelLayers.PLAYER))));
	}
	
	// Taken from PlayerRenderer so that the NPC can be at player proportions
	@Override
	protected void scale(T entity, PoseStack stack, float scale) {
		float f = 0.9375F;
		stack.scale(f, f, f);
	}
	
	@Override
	public void render(T npc, float p_225623_2_, float partialTicks, PoseStack stack, MultiBufferSource buf, int packedLight) {
		super.render(npc, p_225623_2_, partialTicks, stack, buf, packedLight);
		Optional<NPCComplaint> complaint = npc.getBrain().getMemory(MemoryModuleTypeInit.COMPLAINT.get());
		if (complaint.isPresent()) {
			stack.pushPose();
			stack.translate(0.0d, 0.25d, 0.0d);
			this.renderNameTag(npc, complaint.get().getMessage(), stack, buf, packedLight);
			stack.popPose();
		}
	}

	@Override
	public ResourceLocation getTextureLocation(T entity) {
		return DEFAULT_TEXTURE;
	}
	
}
