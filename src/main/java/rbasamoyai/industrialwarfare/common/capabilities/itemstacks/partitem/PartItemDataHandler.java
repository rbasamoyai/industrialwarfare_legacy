package rbasamoyai.industrialwarfare.common.capabilities.itemstacks.partitem;

import net.minecraft.nbt.CompoundTag;

public class PartItemDataHandler implements IPartItemData {

	private int partCount = 1;
	private float weight = 1.0f;
	
	@Override
	public void setPartCount(int partCount) {
		this.partCount = partCount;
	}

	@Override
	public int getPartCount() {
		return this.partCount;
	}

	@Override
	public void setWeight(float weight) {
		this.weight = weight;
	}

	@Override
	public float getWeight() {
		return this.weight;
	}
	
	@Override
	public CompoundTag writeTag(CompoundTag tag) {
		tag.putFloat("weight", this.weight);
		tag.putInt("partCount", this.partCount);
		return tag;
	}
	
	@Override
	public void readTag(CompoundTag tag) {
		this.weight = tag.getFloat("weight");
		this.partCount = tag.getInt("partCount");
	}

}
