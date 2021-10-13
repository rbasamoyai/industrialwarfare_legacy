package rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.common;

import java.util.Optional;

import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import rbasamoyai.industrialwarfare.common.containers.TaskScrollContainer;
import rbasamoyai.industrialwarfare.common.items.taskscroll.ArgSelector;
import rbasamoyai.industrialwarfare.common.items.taskscroll.ArgWrapper;
import rbasamoyai.industrialwarfare.common.items.taskscroll.IArgHolder;

public class ItemArgHolder implements IArgHolder {
	
	private final ITextComponent title;
	private ItemStack arg;
	
	public ItemArgHolder(ITextComponent title) {
		this.title = title;
	}
	
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
	public Optional<ArgSelector<?>> getSelector(TaskScrollContainer container) {
		return Optional.of(new ItemArgTitleArgSelector(this.title));
	}

}
