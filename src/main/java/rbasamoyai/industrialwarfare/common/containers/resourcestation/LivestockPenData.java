package rbasamoyai.industrialwarfare.common.containers.resourcestation;

import rbasamoyai.industrialwarfare.common.blockentities.LivestockPenBlockEntity;

public class LivestockPenData extends ResourceStationData {

	private final LivestockPenBlockEntity pen;
	
	public LivestockPenData(LivestockPenBlockEntity be) {
		super(be);
		this.pen = be;
	}
	
	@Override
	public int get(int index) {
		return switch (index) {
			case 2 -> this.pen.getMinimumLivestock();
			default -> super.get(index);
		};
	}
	
	@Override
	public void set(int index, int value) {
		switch (index) {
			case 2 -> this.pen.setMinimumLivestock(value);
			default -> super.set(index, value);
		}
	}
	
	@Override
	public int getCount() {
		return 3;
	}
	
}
