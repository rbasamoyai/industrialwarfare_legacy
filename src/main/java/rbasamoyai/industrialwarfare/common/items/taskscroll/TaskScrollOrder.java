package rbasamoyai.industrialwarfare.common.items.taskscroll;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.INBTSerializable;
import rbasamoyai.industrialwarfare.client.screen.selectors.ArgSelector;
import rbasamoyai.industrialwarfare.common.taskscrollcmds.TaskScrollCommand;
import rbasamoyai.industrialwarfare.core.IWModRegistries;
import rbasamoyai.industrialwarfare.utils.ArgUtils;

/**
 * A class for the TaskScrollItem. It can be expanded for multiple task scroll types.
 * 
 * @author rbasmaoyai
 *
 */

public class TaskScrollOrder implements INBTSerializable<CompoundNBT> {
	
	private static final String TAG_COMMAND = "command";
	private static final String TAG_POS_X = "x";
	private static final String TAG_POS_Y = "y";
	private static final String TAG_POS_Z = "z";
	private static final String TAG_ARGS = "args";
	private static final String TAG_FILTER = "filter";
	
	private TaskScrollCommand cmd;
	private BlockPos pos;
	private ItemStack filter;
	private List<Byte> args;
	
	public TaskScrollOrder(final TaskScrollCommand cmd, final BlockPos pos, final ItemStack filter, final List<Byte> args) {
		this.cmd = cmd;
		this.pos = pos;
		this.filter = filter.copy();
		this.args = args;
	}
	
	public TaskScrollOrder(TaskScrollCommand cmd, BlockPos pos, ItemStack filter) {
		this(cmd, pos, filter, new ArrayList<Byte>(cmd.getArgCount()));
	}
	
	public TaskScrollOrder(TaskScrollCommand cmd, BlockPos pos) {
		this(cmd, pos, ItemStack.EMPTY);
	}
	
	public TaskScrollOrder(TaskScrollCommand cmd) {
		this(cmd, BlockPos.ZERO);
	}
	
	public void setCmdFromSelector(ArgSelector<TaskScrollCommand> selector) {
		TaskScrollCommand oldCmd = this.cmd;
		this.cmd = selector.getSelectedArg();
		if (oldCmd != this.cmd) {
			this.args = new ArrayList<Byte>();
			for (int i = 0; i < this.cmd.getArgCount(); i++) {
				this.args.add(Byte.valueOf((byte) 0));
			}
			if (!this.cmd.canUseFilter()) this.setFilter(ItemStack.EMPTY);
		}
	}
	
	public void setPosFromSelector(ArgSelector<BlockPos> selector) {
		this.pos = selector.getSelectedArg();
	}
	
	public void setFilter(ItemStack stack) {
		this.filter = stack.copy();
		this.filter.setCount(1);
	}
	
	public void setArgFromSelectorAndIndex(ArgSelector<Byte> selector, int pos) {
		this.args.set(pos, selector.getSelectedArg());
	}
	
	public TaskScrollCommand getCommand() {
		return this.cmd;
	}
	
	public boolean usesBlockPos() {
		return this.cmd.usesBlockPos();
	}
	
	public boolean canUseFilter() {
		return this.cmd.canUseFilter();
	}
	
	public BlockPos getPos() {
		return this.pos;
	}
	
	public ItemStack getFilter() {
		return this.filter;
	}
	
	public List<Byte> getArgs() {
		return this.args;
	}
	
	public int getArg(int index) {
		return this.args.get(index).intValue();
	}
	
	public boolean filterMatches(ItemStack stack) {
		// TODO: implement filters
		return this.filter.isEmpty() || this.filter.getItem().equals(stack.getItem());
	}

	@Override
	public CompoundNBT serializeNBT() {
		CompoundNBT tag = new CompoundNBT();
		tag.putString(TAG_COMMAND, this.cmd.getRegistryName().toString());
		tag.putInt(TAG_POS_X, this.pos.getX());
		tag.putInt(TAG_POS_Y, this.pos.getY());
		tag.putInt(TAG_POS_Z, this.pos.getZ());
		tag.putByteArray(TAG_ARGS, ArgUtils.unbox(this.args));
		tag.put(TAG_FILTER, this.filter.serializeNBT());
		return tag;
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		this.cmd = IWModRegistries.TASK_SCROLL_COMMANDS.getValue(new ResourceLocation(nbt.getString(TAG_COMMAND)));
		this.pos = new BlockPos(nbt.getInt(TAG_POS_X), nbt.getInt(TAG_POS_Y), nbt.getInt(TAG_POS_Z));
		this.args = ArgUtils.box(nbt.getByteArray(TAG_ARGS));
		this.filter = ItemStack.of(nbt.getCompound(TAG_FILTER));
	}
	
	@Override
	public String toString() {
		String cmdString = this.cmd.getRegistryName().toString();
		String posString = this.pos.getX() + " " + this.pos.getY() + " " + this.pos.getZ();
		
		String filterString = "";
		
		if (this.canUseFilter()) {
			StringBuilder fsBuilder = new StringBuilder();
			fsBuilder.append(" with ");
			fsBuilder.append(this.filter.getItem() == Items.AIR ? "no filter" : "filter of " + this.filter.toString());
			filterString = fsBuilder.toString();
		}
		
		String argString = this.cmd.getArgCount() == 0 ? "" : " with args " + String.join(" ", this.args.stream().map(b -> b.toString()).collect(Collectors.toList()));
		
		return cmdString + " at " + posString + filterString + argString;
	}
	
}
