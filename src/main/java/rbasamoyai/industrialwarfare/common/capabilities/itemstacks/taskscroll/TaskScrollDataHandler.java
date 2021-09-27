package rbasamoyai.industrialwarfare.common.capabilities.itemstacks.taskscroll;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollOrder;

public class TaskScrollDataHandler implements ITaskScrollDataHandler {

	private static final int MAX_SIZE = 16;
	
	private final List<TaskScrollOrder> orderList = new ArrayList<>(MAX_SIZE);
	private ItemStack label = ItemStack.EMPTY;

	@Override
	public TaskScrollOrder getOrder(byte index) {
		return this.orderList.get(index);
	}
	
	@Override
	public void setLabel(ItemStack label) {
		this.label = label.copy();
	}
	
	@Override
	public ItemStack getLabel() {
		return this.label;
	}
	
	@Override
	public void setList(List<TaskScrollOrder> list) {
		this.orderList.clear();
		list.forEach(this.orderList::add);
	}
	
	@Override
	public List<TaskScrollOrder> getList() {
		return this.orderList;
	}
	
	@Override
	public int getMaxListSize() {
		return MAX_SIZE;
	}
	
}
