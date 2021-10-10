package rbasamoyai.industrialwarfare.common.items.taskscroll;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.TaskScrollCommand;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.common.EmptyArgHolder;
import rbasamoyai.industrialwarfare.core.IWModRegistries;

/**
 * A class for the TaskScrollItem. It can be expanded for multiple task scroll types.
 * 
 * @author rbasmaoyai
 *
 */

public class TaskScrollOrder implements INBTSerializable<CompoundNBT> {
	
	private static final String TAG_COMMAND = "command";
	private static final String TAG_ARGS = "args";
	
	private TaskScrollCommand command;
	private List<IArgHolder> args;
	
	public TaskScrollOrder(TaskScrollCommand command, List<IArgHolder> args) {
		this.command = command;
		this.args = args;
	}
	
	public static TaskScrollOrder filledWith(TaskScrollCommand command, ArgWrapper wrapper) {
		List<IArgHolder> args = new ArrayList<>(command.getArgCount());
		for (int i = 0; i < command.getArgCount(); i++) {
			IArgHolder holder = command.getArgHolderSupplier(i).get();
			holder.accept(wrapper);
			args.add(holder);
		}
		return new TaskScrollOrder(command, args);
	}
	
	public static TaskScrollOrder empty(TaskScrollCommand command) {
		return filledWith(command, ArgWrapper.EMPTY);
	}
	
	public static TaskScrollOrder withPos(TaskScrollCommand command, BlockPos pos) {
		return filledWith(command, new ArgWrapper(pos));
	}
	
	public final void setCommand(TaskScrollCommand command) {
		TaskScrollCommand oldCommand = this.command;
		this.command = command;
		if (oldCommand != this.command) {
			this.args.clear();
			for (int i = 0; i < this.command.getArgCount(); i++) {
				IArgHolder holder = this.command.getArgHolderSupplier(i).get();
				holder.accept(ArgWrapper.EMPTY);
				this.args.add(holder);
			}
		}
	}
	
	public final TaskScrollCommand getCommand() {
		return this.command;
	}
	
	public final void setArg(int i, ArgWrapper wrapper) {
		if (this.isValidIndex(i)) {
			this.args.get(i).accept(wrapper);
		}
	}
	
	public final ArgWrapper getWrappedArg(int i) {
		return this.isValidIndex(i) ? this.args.get(i).getWrapper() : new ArgWrapper(0);
	}
	
	public final IArgHolder getArgHolder(int i) {
		return this.isValidIndex(i) ? this.args.get(i) : new EmptyArgHolder();
	}
	
	private boolean isValidIndex(int i) {
		return 0 <= i && i < this.args.size();
	}

	@Override
	public CompoundNBT serializeNBT() {
		CompoundNBT tag = new CompoundNBT();
		tag.putString(TAG_COMMAND, this.command.getRegistryName().toString());
		
		ListNBT argTag = new ListNBT();
		this.args.forEach(arg -> argTag.add(arg.serializeNBT()));
		tag.put(TAG_ARGS, argTag);
		
		return tag;
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		this.setCommand(IWModRegistries.TASK_SCROLL_COMMANDS.getValue(new ResourceLocation(nbt.getString(TAG_COMMAND))));
		ListNBT argTags = nbt.getList(TAG_ARGS, Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < this.command.getArgCount(); i++) {
			IArgHolder holder = this.args.get(i);
			holder.deserializeNBT((CompoundNBT) argTags.get(i));
		}
	}
	
	public void toNetwork(PacketBuffer buf) {
		buf.writeResourceLocation(this.command.getRegistryName());
		// Expecting to write appropriate amount of args, so i depends on this.command#getArgCount rather than this.args#size
		for (int i = 0; i < this.command.getArgCount(); i++) {
			this.args.get(i).toNetwork(buf);
		}
	}
	
	public static TaskScrollOrder fromNetwork(PacketBuffer buf) {
		TaskScrollCommand cmd = IWModRegistries.TASK_SCROLL_COMMANDS.getValue(buf.readResourceLocation());
		List<IArgHolder> holders = new ArrayList<>(cmd.getArgCount());
		for (int i = 0; i < cmd.getArgCount(); i++) {
			IArgHolder holder = cmd.getArgHolderSupplier(i).get();
			holder.fromNetwork(buf);
			holders.add(holder);
		}
		return new TaskScrollOrder(cmd, holders);
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
