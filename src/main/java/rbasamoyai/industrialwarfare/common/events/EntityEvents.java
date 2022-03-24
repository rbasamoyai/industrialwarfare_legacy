package rbasamoyai.industrialwarfare.common.events;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.monster.AbstractSkeletonEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;

@Mod.EventBusSubscriber(modid = IndustrialWarfare.MOD_ID, bus = Bus.FORGE)
public class EntityEvents {

	@SubscribeEvent
	public static void onEntityJoin(EntityJoinWorldEvent event) {
		Entity entity = event.getEntity();
		
		if (entity instanceof MobEntity) {
			MobEntity mob = (MobEntity) entity;
			if (mob instanceof ZombieEntity
				|| mob instanceof AbstractSkeletonEntity) {
				mob.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(mob, NPCEntity.class, mob instanceof ZombieEntity));
			}
		}
	}
	
}
