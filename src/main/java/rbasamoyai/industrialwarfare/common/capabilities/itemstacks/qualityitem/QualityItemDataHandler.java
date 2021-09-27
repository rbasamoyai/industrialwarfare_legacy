package rbasamoyai.industrialwarfare.common.capabilities.itemstacks.qualityitem;

public class QualityItemDataHandler implements IQualityItemDataHandler {

	protected float quality = 1.0f;
	
	@Override
	public void setQuality(float f) {
		this.quality = f;
	}

	@Override
	public float getQuality() {
		return this.quality;
	}

}
