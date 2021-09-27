package rbasamoyai.industrialwarfare.common.capabilities.itemstacks.partitem;

import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.qualityitem.QualityItemDataHandler;

public class PartItemDataHandler extends QualityItemDataHandler implements IPartItemDataHandler {

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

}
