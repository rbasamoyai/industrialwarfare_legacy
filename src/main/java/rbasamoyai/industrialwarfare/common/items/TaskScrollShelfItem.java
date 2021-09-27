package rbasamoyai.industrialwarfare.common.items;

import net.minecraft.client.renderer.tileentity.PistonTileEntityRenderer;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.core.init.BlockInit;
import rbasamoyai.industrialwarfare.core.itemgroup.IWItemGroups;

public class TaskScrollShelfItem extends BlockItem {

	public TaskScrollShelfItem() {
		super(BlockInit.TASK_SCROLL_SHELF, new Item.Properties().tab(IWItemGroups.TAB_BLOCKS).setISTER(null));
		
		this.setRegistryName(IndustrialWarfare.MOD_ID, "task_scroll_shelf");
	}
	
}
