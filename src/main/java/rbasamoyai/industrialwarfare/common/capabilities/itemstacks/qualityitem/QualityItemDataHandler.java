package rbasamoyai.industrialwarfare.common.capabilities.itemstacks.qualityitem;

import net.minecraft.nbt.CompoundTag;

public class QualityItemDataHandler implements IQualityItemData {

	protected float quality = 1.0f;
	
	@Override
	public void setQuality(float f) {
		this.quality = f;
	}

	@Override
	public float getQuality() {
		return this.quality;
	}
	
	@Override
	public CompoundTag writeTag(CompoundTag tag) {
		tag.putFloat("quality", this.quality);
		return tag;
	}
	
	@Override
	public void readTag(CompoundTag tag) {
		this.quality = tag.getFloat("quality");
	}

}
