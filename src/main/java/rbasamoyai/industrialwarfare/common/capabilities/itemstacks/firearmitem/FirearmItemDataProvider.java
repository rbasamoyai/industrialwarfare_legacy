package rbasamoyai.industrialwarfare.common.capabilities.itemstacks.firearmitem;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class FirearmItemDataProvider implements ICapabilitySerializable<CompoundNBT> {

	private final IFirearmItemDataHandler dataHandler;
	private final LazyOptional<IFirearmItemDataHandler> dataOptional;
	
	public FirearmItemDataProvider(IFirearmItemDataHandler dataHandler) {
		this.dataHandler = dataHandler;
		this.dataOptional = LazyOptional.of(() -> this.dataHandler);
	}
	
	public void invalidate() {
		this.dataOptional.invalidate();
	}
	
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		return this.dataOptional.cast();
	}

	@Override
	public CompoundNBT serializeNBT() {
		return FirearmItemDataCapability.FIREARM_ITEM_DATA_CAPABILITY == null
				? new CompoundNBT()
				: (CompoundNBT) FirearmItemDataCapability.FIREARM_ITEM_DATA_CAPABILITY.writeNBT(this.dataHandler, null);
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		if (FirearmItemDataCapability.FIREARM_ITEM_DATA_CAPABILITY != null)
			FirearmItemDataCapability.FIREARM_ITEM_DATA_CAPABILITY.readNBT(this.dataHandler, null, nbt);
	}

}
