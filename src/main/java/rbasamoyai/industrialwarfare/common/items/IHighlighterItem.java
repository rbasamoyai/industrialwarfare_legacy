package rbasamoyai.industrialwarfare.common.items;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;

public interface IHighlighterItem {

	default boolean shouldHighlightEntity(ItemStack stack, Entity entity) { return false; }
	void renderHighlight(Entity entity, ItemStack item, MatrixStack matrixstack, IRenderTypeBuffer buf);
	
}
