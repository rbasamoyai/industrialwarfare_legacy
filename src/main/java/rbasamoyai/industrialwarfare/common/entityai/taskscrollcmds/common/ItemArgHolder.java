package rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.common;

import java.util.Optional;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.StringTextComponent;
import rbasamoyai.industrialwarfare.client.screen.widgets.ArgSelector;
import rbasamoyai.industrialwarfare.common.containers.TaskScrollContainer;
import rbasamoyai.industrialwarfare.common.items.taskscroll.ArgWrapper;
import rbasamoyai.industrialwarfare.common.items.taskscroll.IArgHolder;

public class ItemArgHolder implements IArgHolder {

	private static final String TAG_ITEM = "item";
	
	private ItemStack arg;
	
	@Override
	public void accept(ArgWrapper wrapper) {
		this.arg = wrapper.getItem().orElse(ItemStack.EMPTY);
	}

	@Override
	public ArgWrapper getWrapper() {
		return new ArgWrapper(this.arg.copy());
	}

	@Override
	public boolean isItemStackArg() {
		return true;
	}

	@Override
	public void toNetwork(PacketBuffer buf) {
		buf.writeItem(this.arg);
	}

	@Override
	public void fromNetwork(PacketBuffer buf) {
		this.arg = buf.readItem();
	}

	@Override
	public CompoundNBT serializeNBT() {
		CompoundNBT tag = new CompoundNBT();
		tag.put(TAG_ITEM, this.arg.serializeNBT());
		return tag;
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		this.arg = ItemStack.of(nbt.getCompound(TAG_ITEM));
	}

	@Override
	public Optional<ArgSelector<?>> getSelector(TaskScrollContainer container) {
		return Optional.of(new ItemArgTitleArgSelector(StringTextComponent.EMPTY));
	}

}
