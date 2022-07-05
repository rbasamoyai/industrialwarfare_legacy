package rbasamoyai.industrialwarfare.common.blockentities;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

public interface ConfigurableBounds {

	void trySettingBounds(Player player, ItemStack stack, BlockPos pos1, BlockPos pos2);
	@Nullable AABB getBoxForRenderingCurrentBounds(ItemStack stack);
	BlockPos startingCorner();
	BlockPos endingCorner();
	
}
