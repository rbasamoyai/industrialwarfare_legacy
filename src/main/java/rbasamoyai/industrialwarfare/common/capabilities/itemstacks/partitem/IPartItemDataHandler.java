package rbasamoyai.industrialwarfare.common.capabilities.itemstacks.partitem;

import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.qualityitem.IQualityItemDataHandler;

public interface IPartItemDataHandler extends IQualityItemDataHandler {

	public void setPartCount(int partCount);
	public int getPartCount();
	
	public void setWeight(float weight);
	public float getWeight();
	
}
