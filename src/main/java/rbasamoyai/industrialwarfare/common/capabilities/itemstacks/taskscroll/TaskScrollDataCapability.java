package rbasamoyai.industrialwarfare.common.capabilities.itemstacks.taskscroll;

import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.util.Constants;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollOrder;
import rbasamoyai.industrialwarfare.core.init.TaskScrollCommandInit;

public class TaskScrollDataCapability {

	public static final String TAG_ORDER_LIST = "orderList";
	public static final String TAG_LABEL_ITEM = "labelItem";
	
	@CapabilityInject(ITaskScrollDataHandler.class)
	public static Capability<ITaskScrollDataHandler> TASK_SCROLL_DATA_CAPABILITY = null;
	
	public static void register() {
		CapabilityManager.INSTANCE.register(ITaskScrollDataHandler.class, new Storage(), TaskScrollDataHandler::new);
	}
	
	public static class Storage implements IStorage<ITaskScrollDataHandler> {

		@Override
		public INBT writeNBT(Capability<ITaskScrollDataHandler> capability, ITaskScrollDataHandler instance, Direction side) {
			CompoundNBT tag = new CompoundNBT();
			
			ListNBT orderList = new ListNBT();
			instance.getList().forEach(order -> orderList.add(order.serializeNBT()));
			
			tag.put(TAG_ORDER_LIST, orderList);
			tag.put(TAG_LABEL_ITEM, instance.getLabel().serializeNBT());
			
			return tag;
		}

		@Override
		public void readNBT(Capability<ITaskScrollDataHandler> capability, ITaskScrollDataHandler instance, Direction side, INBT nbt) {
			CompoundNBT tag = (CompoundNBT) nbt;
			
			ListNBT orderTags = tag.getList(TAG_ORDER_LIST, Constants.NBT.TAG_COMPOUND);
			List<TaskScrollOrder> orderList = orderTags.stream()
					.map(ot -> {
						TaskScrollOrder order = TaskScrollOrder.empty(TaskScrollCommandInit.MOVE_TO.get());
						order.deserializeNBT((CompoundNBT) ot);
						return order;
					}).collect(Collectors.toList());
			instance.setList(orderList);
			instance.setLabel(ItemStack.of(tag.getCompound(TAG_LABEL_ITEM)));
		}
		
	}
	
}
