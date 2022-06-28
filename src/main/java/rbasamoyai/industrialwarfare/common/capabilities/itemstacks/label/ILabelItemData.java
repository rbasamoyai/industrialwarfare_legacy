package rbasamoyai.industrialwarfare.common.capabilities.itemstacks.label;

import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

public interface ILabelItemData {

	void setUUID(UUID uuid);
	UUID getUUID();
	
	void setNumber(byte b);
	byte getNumber();
	
	void setFlags(byte b);
	byte getFlags();
	
	void cacheName(Component name);
	Component getCachedName();
	
	CompoundTag writeTag(CompoundTag tag);
	void readTag(CompoundTag tag);
	
}
