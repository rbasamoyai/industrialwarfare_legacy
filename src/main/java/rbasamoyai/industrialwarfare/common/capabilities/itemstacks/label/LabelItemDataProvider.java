package rbasamoyai.industrialwarfare.common.capabilities.itemstacks.label;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class LabelItemDataProvider implements ICapabilitySerializable<CompoundNBT> {

	private final LabelItemDataHandler dataHandler = new LabelItemDataHandler();
	private final LazyOptional<ILabelItemDataHandler> dataOptional = LazyOptional.of(() -> this.dataHandler);
	
	public void invalidate() {
		this.dataOptional.invalidate();
	}
	
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		return cap == LabelItemDataCapability.LABEL_ITEM_DATA_CAPABILITY ? this.dataOptional.cast() : LazyOptional.empty();
	}

	@Override
	public CompoundNBT serializeNBT() {
		return LabelItemDataCapability.LABEL_ITEM_DATA_CAPABILITY == null
				? new CompoundNBT()
				: (CompoundNBT) LabelItemDataCapability.LABEL_ITEM_DATA_CAPABILITY.writeNBT(this.dataHandler, null);
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		if (LabelItemDataCapability.LABEL_ITEM_DATA_CAPABILITY != null)
			LabelItemDataCapability.LABEL_ITEM_DATA_CAPABILITY.readNBT(this.dataHandler, null, nbt);
	}

}
