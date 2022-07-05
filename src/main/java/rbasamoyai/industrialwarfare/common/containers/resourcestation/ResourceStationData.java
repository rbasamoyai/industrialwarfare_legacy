package rbasamoyai.industrialwarfare.common.containers.resourcestation;

import net.minecraft.world.inventory.ContainerData;
import rbasamoyai.industrialwarfare.common.blockentities.ResourceStationBlockEntity;

public class ResourceStationData implements ContainerData {

	private final ResourceStationBlockEntity te;
	
	public ResourceStationData(ResourceStationBlockEntity te) {
		this.te = te;
	}
	
	@Override
	public int get(int index) {
		return switch (index) {
			case 0 -> this.te.isRunning() ? 1 : 0;
			case 1 -> this.te.isFinished() ? 1 : 0;
			default -> 0;
		};
	}

	@Override
	public void set(int index, int value) {
		switch (index) {
			case 0 -> this.te.setRunning(value != 0);
		}
	}

	@Override
	public int getCount() {
		return 2;
	}

}
