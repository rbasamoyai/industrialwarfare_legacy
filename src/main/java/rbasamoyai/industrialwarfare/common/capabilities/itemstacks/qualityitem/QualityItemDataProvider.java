package rbasamoyai.industrialwarfare.common.capabilities.itemstacks.qualityitem;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class QualityItemDataProvider implements ICapabilitySerializable<CompoundNBT> {

	private final QualityItemDataHandler dataHandler = new QualityItemDataHandler();
	private final LazyOptional<IQualityItemDataHandler> dataOptional = LazyOptional.of(() -> this.dataHandler);
	
	public void invalidate() {
		this.dataOptional.invalidate();
	}
	
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		return this.dataOptional.cast();
	}

	@Override
	public CompoundNBT serializeNBT() {
		return QualityItemDataCapability.QUALITY_ITEM_DATA_CAPABILITY == null
				? new CompoundNBT()
				: (CompoundNBT) QualityItemDataCapability.QUALITY_ITEM_DATA_CAPABILITY.writeNBT(this.dataHandler, null);
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		if (QualityItemDataCapability.QUALITY_ITEM_DATA_CAPABILITY != null)
			QualityItemDataCapability.QUALITY_ITEM_DATA_CAPABILITY.readNBT(this.dataHandler, null, nbt);
	}

}
