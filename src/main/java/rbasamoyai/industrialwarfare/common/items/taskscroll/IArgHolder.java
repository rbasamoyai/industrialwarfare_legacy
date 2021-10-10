package rbasamoyai.industrialwarfare.common.items.taskscroll;

import java.util.Optional;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.util.INBTSerializable;
import rbasamoyai.industrialwarfare.client.screen.widgets.ArgSelector;
import rbasamoyai.industrialwarfare.common.containers.TaskScrollContainer;

public interface IArgHolder extends INBTSerializable<CompoundNBT> {
	
	public void accept(ArgWrapper wrapper);
	public ArgWrapper getWrapper();
	
	public boolean isItemStackArg();
	
	public void toNetwork(PacketBuffer buf);
	public void fromNetwork(PacketBuffer buf);
	
	public Optional<ArgSelector<?>> getSelector(TaskScrollContainer container);
	
}
