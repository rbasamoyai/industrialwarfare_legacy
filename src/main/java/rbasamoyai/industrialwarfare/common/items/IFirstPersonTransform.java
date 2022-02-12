package rbasamoyai.industrialwarfare.common.items;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public interface IFirstPersonTransform {

	boolean shouldTransform(ItemStack stack, PlayerEntity player);
	
	void transformMatrixStack(ItemStack itemStack, PlayerEntity player, MatrixStack matrixStack);
	
}
