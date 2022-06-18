package rbasamoyai.industrialwarfare.common.capabilities.itemstacks.partitem;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class PartItemDataProvider implements ICapabilitySerializable<CompoundNBT> {

	private final PartItemDataHandler dataHandler = new PartItemDataHandler();
	private final LazyOptional<IPartItemDataHandler> dataOptional = LazyOptional.of(() -> this.dataHandler);
	
	public void invalidate() {
		this.dataOptional.invalidate();
	}
	
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		return cap == PartItemDataCapability.PART_ITEM_DATA_CAPABILITY ? this.dataOptional.cast() : LazyOptional.empty();
	}

	@Override
	public CompoundNBT serializeNBT() {
		return PartItemDataCapability.PART_ITEM_DATA_CAPABILITY == null
				? new CompoundNBT()
				: (CompoundNBT) PartItemDataCapability.PART_ITEM_DATA_CAPABILITY.writeNBT(this.dataHandler, null);
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		if (PartItemDataCapability.PART_ITEM_DATA_CAPABILITY != null)
			PartItemDataCapability.PART_ITEM_DATA_CAPABILITY.readNBT(this.dataHandler, null, nbt);
	}

}
