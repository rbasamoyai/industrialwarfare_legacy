package rbasamoyai.industrialwarfare.common.blockentities;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;

public interface ConfigurableBounds {

	void trySettingBounds(Player player, ItemStack stack, BlockPos pos1, BlockPos pos2);
	
}
