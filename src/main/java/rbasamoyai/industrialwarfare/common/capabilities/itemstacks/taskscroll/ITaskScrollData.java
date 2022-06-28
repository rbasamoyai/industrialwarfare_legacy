package rbasamoyai.industrialwarfare.common.capabilities.itemstacks.taskscroll;

import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollOrder;

public interface ITaskScrollData {

	TaskScrollOrder getOrder(int index);
	
	void setLabel(ItemStack label);
	ItemStack getLabel();
	
	void setList(List<TaskScrollOrder> list);
	List<TaskScrollOrder> getList();
	
	int getMaxListSize();
	
	CompoundTag writeTag(CompoundTag tag);
	void readTag(CompoundTag tag);
	
}
