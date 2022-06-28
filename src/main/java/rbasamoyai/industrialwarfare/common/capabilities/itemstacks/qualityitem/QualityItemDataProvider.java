package rbasamoyai.industrialwarfare.common.capabilities.itemstacks.qualityitem;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class QualityItemDataProvider implements ICapabilitySerializable<CompoundTag> {
	
	private final IQualityItemData dataHandler = new QualityItemDataHandler();
	private final LazyOptional<IQualityItemData> dataOptional = LazyOptional.of(() -> this.dataHandler);
	
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		return cap == QualityItemCapability.INSTANCE ? this.dataOptional.cast() : LazyOptional.empty();
	}
	
	@Override
	public CompoundTag serializeNBT() {
		return QualityItemCapability.INSTANCE.isRegistered() ? this.dataHandler.writeTag(new CompoundTag()) : new CompoundTag();
	}

	@Override
	public void deserializeNBT(CompoundTag tag) {
		if (QualityItemCapability.INSTANCE.isRegistered()) {
			this.dataHandler.readTag(tag);
		}
	}

}
