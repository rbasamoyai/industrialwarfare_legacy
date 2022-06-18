package rbasamoyai.industrialwarfare.common.capabilities.tileentities.workstation;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class WorkstationDataProvider implements ICapabilitySerializable<CompoundNBT> {

	private final WorkstationDataHandler dataHandler = new WorkstationDataHandler();
	private final LazyOptional<IWorkstationDataHandler> dataOptional = LazyOptional.of(() -> this.dataHandler);
	
	public void invalidate() {
		this.dataOptional.invalidate();
	}
	
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		return cap == WorkstationDataCapability.WORKSTATION_DATA_CAPABILITY ? this.dataOptional.cast() : LazyOptional.empty();
	}

	@Override
	public CompoundNBT serializeNBT() {
		return WorkstationDataCapability.WORKSTATION_DATA_CAPABILITY == null
				? new CompoundNBT()
				: (CompoundNBT) WorkstationDataCapability.WORKSTATION_DATA_CAPABILITY.writeNBT(this.dataHandler, null);
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		if (WorkstationDataCapability.WORKSTATION_DATA_CAPABILITY != null)
			WorkstationDataCapability.WORKSTATION_DATA_CAPABILITY.readNBT(this.dataHandler, null, nbt);
	}

}
