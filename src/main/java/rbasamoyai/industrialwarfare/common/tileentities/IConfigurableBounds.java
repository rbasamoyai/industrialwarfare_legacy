package rbasamoyai.industrialwarfare.common.tileentities;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public interface IConfigurableBounds {

	void trySettingBounds(PlayerEntity player, ItemStack stack, BlockPos pos1, BlockPos pos2);
	
}
