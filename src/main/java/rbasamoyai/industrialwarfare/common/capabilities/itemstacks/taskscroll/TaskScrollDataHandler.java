package rbasamoyai.industrialwarfare.common.capabilities.itemstacks.taskscroll;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollOrder;

public class TaskScrollDataHandler implements ITaskScrollData {

	private static final int MAX_SIZE = 16;
	
	private final List<TaskScrollOrder> orderList = new ArrayList<>(MAX_SIZE);
	private ItemStack label = ItemStack.EMPTY;

	@Override
	public TaskScrollOrder getOrder(int index) {
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

	@Override
	public CompoundTag writeTag(CompoundTag tag) {
		tag.put("orderList", this.orderList.stream().map(TaskScrollOrder::serializeNBT).collect(Collectors.toCollection(ListTag::new)));
		tag.put("labelItem", this.getLabel().serializeNBT());
		return tag;
	}
	
	@Override
	public void readTag(CompoundTag tag) {
		List<TaskScrollOrder> newList =
				tag.getList("orderList", Tag.TAG_COMPOUND)
				.stream()
				.map(CompoundTag.class::cast)
				.map(TaskScrollOrder::fromTag)
				.collect(Collectors.toCollection(ArrayList::new));
		this.setList(newList);
		this.setLabel(ItemStack.of(tag.getCompound("labelItem")));
	}
	
}
