package rbasamoyai.industrialwarfare.core.network.handlers;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import rbasamoyai.industrialwarfare.client.events.RenderEvents;
import rbasamoyai.industrialwarfare.common.entities.ThirdPersonItemAnimEntity;
import rbasamoyai.industrialwarfare.core.network.messages.CQueueEntityAnimMessage;

public class CQueueEntityAnimHandler {

	public static void handle(CQueueEntityAnimMessage msg) {
		Minecraft mc = Minecraft.getInstance();
		World world = mc.player.level;
		Entity entity = world.getEntity(msg.id);
		if (entity == null) return;
		ThirdPersonItemAnimEntity animEntity = RenderEvents.ANIM_ENTITY_CACHE.get(entity.getUUID());
		if (animEntity == null) return;
		animEntity.queueAnim(msg.controller, msg.makeAnim());
		animEntity.setSpeed(msg.speed);
	}
	
}
