package rbasamoyai.industrialwarfare.common.items;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface FirstPersonTransform {

	boolean shouldTransform(ItemStack stack, Player player);
	
	void transformPoseStack(ItemStack itemStack, Player player, PoseStack matrixStack);
	
}
