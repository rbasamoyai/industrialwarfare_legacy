package rbasamoyai.industrialwarfare.common.capabilities.itemstacks.qualityitem;

import net.minecraft.nbt.CompoundTag;

public interface IQualityItemData {

	void setQuality(float f);
	float getQuality();
	
	CompoundTag writeTag(CompoundTag tag);
	void readTag(CompoundTag tag);
	
}
