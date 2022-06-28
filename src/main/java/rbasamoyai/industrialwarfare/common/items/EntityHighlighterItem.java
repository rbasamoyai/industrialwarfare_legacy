package rbasamoyai.industrialwarfare.common.items;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

public interface EntityHighlighterItem {

	default boolean shouldHighlightEntity(ItemStack stack, Entity entity) { return false; }
	void renderHighlight(Entity entity, ItemStack item, PoseStack matrixstack, MultiBufferSource buf);
	
}
