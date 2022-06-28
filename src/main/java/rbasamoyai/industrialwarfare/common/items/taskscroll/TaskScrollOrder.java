package rbasamoyai.industrialwarfare.common.items.taskscroll;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.TaskScrollCommand;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.common.EmptyArgHolder;
import rbasamoyai.industrialwarfare.core.IWModRegistries;
import rbasamoyai.industrialwarfare.core.init.TaskScrollCommandInit;

/**
 * A class for the TaskScrollItem. It can be expanded for multiple task scroll types.
 * 
 * @author rbasmaoyai
 *
 */

public class TaskScrollOrder implements INBTSerializable<CompoundTag> {
	
	private static final String TAG_COMMAND = "command";
	private static final String TAG_ARGS = "args";
	
	private TaskScrollCommand command;
	private List<IArgHolder> args;
	
	public TaskScrollOrder(TaskScrollCommand command, List<IArgHolder> args) {
		this.command = command;
		this.args = args;
	}
	
	public static TaskScrollOrder filledWith(TaskScrollCommand command, ArgWrapper wrapper) {
		return new TaskScrollOrder(command, command.getCommandTree().getArgumentsWith(wrapper));
	}
	
	public static TaskScrollOrder empty(TaskScrollCommand command) {
		return filledWith(command, ArgWrapper.EMPTY);
	}
	
	public static TaskScrollOrder fromTag(CompoundTag tag) {
		TaskScrollOrder order = empty(TaskScrollCommandInit.MOVE_TO.get());
		order.deserializeNBT(tag);
		return order;
	}
	
	public static TaskScrollOrder withPos(TaskScrollCommand command, BlockPos pos) {
		return filledWith(command, new ArgWrapper(pos));
	}
	
	public final void setCommand(TaskScrollCommand command, ArgWrapper fill) {
		TaskScrollCommand oldCommand = this.command;
		this.command = command;
		if (oldCommand != this.command) {
			this.args = this.command.getCommandTree().getArgumentsWith(fill);
		}
	}
	
	public final TaskScrollCommand getCommand() {
		return this.command;
	}
	
	public final boolean setArg(int i, ArgWrapper arg, ArgWrapper fill) {
		boolean changedBranch = false;
		if (this.isValidIndex(i)) {
			changedBranch = this.willBranchDifferently(arg, i);
			this.args.get(i).accept(arg);
			if (changedBranch) {
				this.args = this.command.getCommandTree().fillArgumentsAfterPoint(this.args, i, fill);
			}
		}
		return changedBranch;
	}
	
	public final ArgWrapper getWrappedArg(int i) {
		return this.isValidIndex(i) ? this.args.get(i).getWrapper() : ArgWrapper.EMPTY;
	}
	
	public final IArgHolder getArgHolder(int i) {
		return this.isValidIndex(i) ? this.args.get(i) : new EmptyArgHolder();
	}
	
	public final int currentArgLength() {
		return this.args.size();
	}
	
	private boolean isValidIndex(int i) {
		return 0 <= i && i < this.args.size();
	}
	
	public final boolean willBranchDifferently(ArgWrapper other, int index) {
		return this.command.getCommandTree().differentBranches(this.args.subList(0, index), this.args.get(index).getWrapper(), other, index);
	}

	@Override
	public CompoundTag serializeNBT() {
		CompoundTag tag = new CompoundTag();
		tag.putString(TAG_COMMAND, this.command.getRegistryName().toString());
		
		ListTag argTag = new ListTag();
		this.args.stream()
				.map(IArgHolder::getWrapper)
				.map(ArgWrapper::serializeNBT)
				.forEach(argTag::add);
		tag.put(TAG_ARGS, argTag);
		
		return tag;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) {
		this.setCommand(IWModRegistries.TASK_SCROLL_COMMANDS.get().getValue(new ResourceLocation(nbt.getString(TAG_COMMAND))), ArgWrapper.EMPTY);
		
		ListTag argTags = nbt.getList(TAG_ARGS, Tag.TAG_COMPOUND);
		List<ArgWrapper> wrappers = argTags.stream()
				.map(tag -> (CompoundTag) tag)
				.map(ArgWrapper::fromNBT)
				.collect(Collectors.toList());
		this.args = this.command.getCommandTree().getArguments(wrappers);
	}
	
	public void toNetwork(FriendlyByteBuf buf) {
		buf.writeResourceLocation(this.command.getRegistryName());
		buf.writeVarInt(this.args.size());
		this.args.stream()
				.map(IArgHolder::getWrapper)
				.forEach(wrapper -> wrapper.toNetwork(buf));
	}
	
	public static TaskScrollOrder fromNetwork(FriendlyByteBuf buf) {
		TaskScrollCommand command = IWModRegistries.TASK_SCROLL_COMMANDS.get().getValue(buf.readResourceLocation());
		int sz = buf.readVarInt();
		
		List<ArgWrapper> wrappers = new ArrayList<>();
		for (int i = 0; i < sz; i++) {
			wrappers.add(ArgWrapper.fromNetwork(buf));
		}
		
		return new TaskScrollOrder(command, command.getCommandTree().getArguments(wrappers));
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("TaskScrollOrder[cmd=" + this.command.toString() + ", args=[\n");
		ListIterator<IArgHolder> iter = this.args.listIterator();
		while (iter.hasNext()) {
			sb.append("\tArgHolder[" + iter.next().getWrapper().toString() + "]");
			if (iter.hasNext()) sb.append(", ");
			sb.append("\n");
		}
		sb.append("]");
		return sb.toString();
	}
	
}
