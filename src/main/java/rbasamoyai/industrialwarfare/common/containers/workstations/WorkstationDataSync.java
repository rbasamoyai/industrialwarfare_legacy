package rbasamoyai.industrialwarfare.common.containers.workstations;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.IIntArray;
import net.minecraftforge.common.util.LazyOptional;
import rbasamoyai.industrialwarfare.common.capabilities.tileentities.workstation.IWorkstationDataHandler;
import rbasamoyai.industrialwarfare.common.tileentities.WorkstationTileEntity;
import rbasamoyai.industrialwarfare.utils.IWUUIDUtils;

public class WorkstationDataSync implements IIntArray {
	
	protected final WorkstationTileEntity te;
	protected PlayerEntity player;
	
	public WorkstationDataSync(WorkstationTileEntity te, PlayerEntity player) {
		this.te = te;
		this.player = player;
	}
	
	@Override
	public int get(int index) {
		LazyOptional<IWorkstationDataHandler> optional = this.te.getDataHandler();
		switch (index) {
		case 0:	return optional.map(IWorkstationDataHandler::hasWorker).orElse(false) ? 1 : 0;
		case 1:	return optional.map(IWorkstationDataHandler::getWorkingTicks).orElse(0);
		case 2:	return this.te.getBaseWorkTicks();
		case 3:	return this.te.getBlockPos().getX();
		case 4:	return this.te.getBlockPos().getY();
		case 5:	return this.te.getBlockPos().getZ();
		case 6: return IWUUIDUtils.equalsFromWorkstationOptional(optional, this.player.getUUID()) ? 0 : 1;
		default: return 0;
		}
	}

	@Override
	public void set(int index, int value) {
		LazyOptional<IWorkstationDataHandler> optional = this.te.getDataHandler();	
		switch (index) {
		case 0:
			optional.ifPresent(h -> h.setHasWorker(value > 0));
			break;
		case 1:
			optional.ifPresent(h -> h.setWorkingTicks(value));
			break;
		default:
			break;
		}
	}

	@Override
	public int getCount() {
		return 7;
	}

}
