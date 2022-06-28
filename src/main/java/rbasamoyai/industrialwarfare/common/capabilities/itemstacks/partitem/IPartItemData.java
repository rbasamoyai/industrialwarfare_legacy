package rbasamoyai.industrialwarfare.common.capabilities.itemstacks.partitem;

import net.minecraft.nbt.CompoundTag;

public interface IPartItemData {

	void setPartCount(int partCount);
	int getPartCount();
	
	void setWeight(float weight);
	float getWeight();
	
	CompoundTag writeTag(CompoundTag tag);
	void readTag(CompoundTag tag);
	
}
