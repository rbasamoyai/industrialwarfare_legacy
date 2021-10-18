package rbasamoyai.industrialwarfare.core.network.handlers;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.world.World;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;
import rbasamoyai.industrialwarfare.core.init.NPCComplaintInit;
import rbasamoyai.industrialwarfare.core.network.messages.CNPCBrainDataSyncMessage;

public class CNPCBrainDataSyncHandler {

	public static void handle(CNPCBrainDataSyncMessage msg) {
		Minecraft mc = Minecraft.getInstance();
		World world = mc.player.level;
		Entity e = world.getEntity(msg.id);
		if (e != null && e instanceof NPCEntity) {
			NPCEntity npc = (NPCEntity) e;
			Brain<?> brain = npc.getBrain();
			if (msg.complaint == NPCComplaintInit.CLEAR.get()) {
				brain.eraseMemory(MemoryModuleTypeInit.COMPLAINT.get());
			} else {
				brain.setMemory(MemoryModuleTypeInit.COMPLAINT.get(), msg.complaint);
			}
		}
	}
	
}
