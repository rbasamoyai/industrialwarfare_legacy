package rbasamoyai.industrialwarfare.common.containers.workstations;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import rbasamoyai.industrialwarfare.common.blockentities.ManufacturingBlockEntity;

public class ManufacturingMenuDataSync implements ContainerData {
	
	protected final ManufacturingBlockEntity te;
	protected Player player;
	
	public ManufacturingMenuDataSync(ManufacturingBlockEntity te, Player player) {
		this.te = te;
		this.player = player;
	}
	
	@Override
	public int get(int index) {
		switch (index) {
		case 0:	return this.te.hasWorker() ? 1 : 0;
		case 1:	return this.te.getWorkingTicks();
		case 2:	return this.te.getBaseWorkTicks();
		case 3:	return this.te.getBlockPos().getX();
		case 4:	return this.te.getBlockPos().getY();
		case 5:	return this.te.getBlockPos().getZ();
		case 6: return this.te.isSameWorker(player) ? 0 : 1;
		default: return 0;
		}
	}

	@Override public void set(int index, int value) {}

	@Override
	public int getCount() {
		return 7;
	}

}
