package rbasamoyai.industrialwarfare.common.capabilities.entities.npc;

import javax.annotation.Nonnull;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class NPCDataProvider implements ICapabilitySerializable<CompoundTag> {
	
	private final NPCDataHandler dataHandler = new NPCDataHandler();
	private final LazyOptional<INPCData> dataOptional = LazyOptional.of(() -> this.dataHandler);
	
	public void invalidate() {
		this.dataOptional.invalidate();
	}

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		return cap == NPCDataCapability.INSTANCE? this.dataOptional.cast() : LazyOptional.empty();
	}

	@Override
	public CompoundTag serializeNBT() {
		return NPCDataCapability.INSTANCE.isRegistered() ? this.dataHandler.writeTag(new CompoundTag()) : new CompoundTag();
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) {
		if (NPCDataCapability.INSTANCE.isRegistered()) {
			this.dataHandler.readTag(nbt);
		}
	}
	
}
