package rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.common;

import java.util.Optional;

import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import rbasamoyai.industrialwarfare.common.containers.TaskScrollMenu;
import rbasamoyai.industrialwarfare.common.items.taskscroll.ArgSelector;
import rbasamoyai.industrialwarfare.common.items.taskscroll.ArgWrapper;
import rbasamoyai.industrialwarfare.common.items.taskscroll.IArgHolder;

public class ItemArgHolder implements IArgHolder {
	
	private final Component title;
	private ItemStack arg;
	
	public ItemArgHolder(Component title) {
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
	public Optional<ArgSelector<?>> getSelector(TaskScrollMenu container) {
		return Optional.of(new ItemArgTitleArgSelector(this.title));
	}

}
