package rbasamoyai.industrialwarfare.common.events;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.monster.AbstractSkeletonEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.stats.Stats;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.diplomacy.PlayerIDTag;
import rbasamoyai.industrialwarfare.common.entities.IHasDiplomaticOwner;
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
	
	@SubscribeEvent
	public static void onEntityDeath(LivingDeathEvent event) {
		DamageSource source = event.getSource();
		Entity target = event.getEntity();
		if (target.level.isClientSide) return;
		
		Entity owner = source.getEntity();
		
		if (owner instanceof IHasDiplomaticOwner) {
			PlayerIDTag tag = ((IHasDiplomaticOwner) owner).getDiplomaticOwner();
			if (tag.isPlayer()) {
				PlayerEntity player = owner.level.getServer().getPlayerList().getPlayer(tag.getUUID());
				if (player != null) {
					player.awardStat(Stats.ENTITY_KILLED.get(target.getType()));
				}
			}
		}
	}	
	
}
