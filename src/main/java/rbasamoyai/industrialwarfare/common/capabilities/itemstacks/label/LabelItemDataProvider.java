package rbasamoyai.industrialwarfare.common.capabilities.itemstacks.label;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class LabelItemDataProvider implements ICapabilitySerializable<CompoundTag> {

	private final ILabelItemData dataHandler = new LabelItemDataHandler();
	private final LazyOptional<ILabelItemData> dataOptional = LazyOptional.of(() -> this.dataHandler);
	
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		return cap == LabelItemCapability.INSTANCE ? this.dataOptional.cast() : LazyOptional.empty();
	}

	@Override
	public CompoundTag serializeNBT() {
		return LabelItemCapability.INSTANCE.isRegistered() ? this.dataHandler.writeTag(new CompoundTag()) : new CompoundTag();
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) {
		if (LabelItemCapability.INSTANCE.isRegistered()) {
			this.dataHandler.readTag(nbt);
		}
	}

}
