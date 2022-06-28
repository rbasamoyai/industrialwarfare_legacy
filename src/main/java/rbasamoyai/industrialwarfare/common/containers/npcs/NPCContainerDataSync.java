package rbasamoyai.industrialwarfare.common.containers.npcs;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;

public class NPCContainerDataSync implements ContainerData {

	public static final float VALID_RANGE = 4.0f;
	
	protected final NPCEntity npc;
	protected Player player;
	
	public NPCContainerDataSync(NPCEntity npc, Player player) {
		this.npc = npc;
		this.player = player;
	}
	
	@Override
	public int get(int index) {
		switch (index) {
			case 0:
				return this.npc.getInventoryItemHandler().getSlots();
			case 1:
				return	Mth.abs((float)(player.getX() - npc.getX())) <= VALID_RANGE
						&& Mth.abs((float)(player.getY() - npc.getY())) <= VALID_RANGE
						&& Mth.abs((float)(player.getZ() - npc.getZ())) <= VALID_RANGE
						&& !npc.isDeadOrDying() ? 1 : 0;
			case 2:
				return this.npc.getDataHandler()
						.map(h -> h.canWearEquipment() ? 1 : 0)
						.orElse(0);
			default:
				return 0;
		}
	}

	@Override
	public void set(int index, int value) {
		switch (index) {
		case 2:
			this.npc.getDataHandler().ifPresent(h -> {
				h.setCanWearEquipment(value > 0);
			});
			break;
		default:
			break;
		}
	}

	@Override
	public int getCount() {
		return 3;
	}

}
