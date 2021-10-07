package rbasamoyai.industrialwarfare.common.capabilities.itemstacks.taskscroll;

import java.util.List;

import net.minecraft.item.ItemStack;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollOrder;

public interface ITaskScrollDataHandler {

	public TaskScrollOrder getOrder(int index);
	
	public void setLabel(ItemStack label);
	public ItemStack getLabel();
	
	public void setList(List<TaskScrollOrder> list);
	public List<TaskScrollOrder> getList();
	
	public int getMaxListSize();
	
}
