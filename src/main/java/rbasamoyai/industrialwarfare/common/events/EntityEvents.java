package rbasamoyai.industrialwarfare.common.events;

import net.minecraft.stats.Stats;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.diplomacy.PlayerIDTag;
import rbasamoyai.industrialwarfare.common.entities.HasDiplomaticOwner;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.common.items.firearms.FirearmItem;

@Mod.EventBusSubscriber(modid = IndustrialWarfare.MOD_ID, bus = Bus.FORGE)
public class EntityEvents {

	@SubscribeEvent
	public static void onEntityJoin(EntityJoinWorldEvent event) {
		Entity entity = event.getEntity();
		
		if (entity instanceof Mob) {
			Mob mob = (Mob) entity;
			if (mob instanceof Zombie
				|| mob instanceof AbstractSkeleton) {
				mob.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(mob, NPCEntity.class, mob instanceof Zombie));
			}
		}
		
		if (entity instanceof LivingEntity) {
			LivingEntity living = (LivingEntity) entity;
			FirearmItem.resetSelected(living.getMainHandItem(), living);
			FirearmItem.resetSelected(living.getOffhandItem(), living);
		}
	}
	
	@SubscribeEvent
	public static void onEntityDeath(LivingDeathEvent event) {
		DamageSource source = event.getSource();
		Entity target = event.getEntity();
		if (target.level.isClientSide) return;
		
		Entity owner = source.getEntity();
		
		if (owner instanceof HasDiplomaticOwner) {
			PlayerIDTag tag = ((HasDiplomaticOwner) owner).getDiplomaticOwner();
			if (tag.isPlayer()) {
				Player player = owner.level.getServer().getPlayerList().getPlayer(tag.getUUID());
				if (player != null) {
					player.awardStat(Stats.ENTITY_KILLED.get(target.getType()));
				}
			}
		}
	}
	
}
