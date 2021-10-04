package rbasamoyai.industrialwarfare.common.containers.npcs;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.IIntArray;
import net.minecraft.util.math.MathHelper;
import rbasamoyai.industrialwarfare.common.capabilities.entities.npc.NPCDataCapability;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;

public class NPCContainerDataSync implements IIntArray {

	public static final float VALID_RANGE = 4.0f;
	
	protected final NPCEntity npc;
	protected PlayerEntity player;
	
	public NPCContainerDataSync(NPCEntity npc, PlayerEntity player) {
		this.npc = npc;
		this.player = player;
	}
	
	@Override
	public int get(int index) {
		switch (index) {
			case 0:
				return this.npc.getInventoryItemHandler().getSlots();
			case 1:
				return	MathHelper.abs((float)(player.getX() - npc.getX())) <= VALID_RANGE
						&& MathHelper.abs((float)(player.getY() - npc.getY())) <= VALID_RANGE
						&& MathHelper.abs((float)(player.getZ() - npc.getZ())) <= VALID_RANGE
						&& !npc.isDeadOrDying() ? 1 : 0;
			case 2:
				return this.npc.getCapability(NPCDataCapability.NPC_DATA_CAPABILITY)
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
