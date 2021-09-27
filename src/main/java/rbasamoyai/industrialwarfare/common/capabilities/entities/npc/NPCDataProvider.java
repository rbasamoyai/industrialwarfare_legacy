package rbasamoyai.industrialwarfare.common.capabilities.entities.npc;

import javax.annotation.Nonnull;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class NPCDataProvider implements ICapabilitySerializable<CompoundNBT> {
	
	private final NPCDataHandler dataHandler = new NPCDataHandler();
	private final LazyOptional<INPCDataHandler> dataOptional = LazyOptional.of(() -> this.dataHandler);
	
	public void invalidate() {
		this.dataOptional.invalidate();
	}

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		return this.dataOptional.cast();
	}

	@Override
	public CompoundNBT serializeNBT() {
		return NPCDataCapability.NPC_DATA_CAPABILITY == null
				? new CompoundNBT()
				: (CompoundNBT) NPCDataCapability.NPC_DATA_CAPABILITY.writeNBT(this.dataHandler, null);
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		if (NPCDataCapability.NPC_DATA_CAPABILITY != null)
			NPCDataCapability.NPC_DATA_CAPABILITY.readNBT(this.dataHandler, null, nbt);
	}
	
}
