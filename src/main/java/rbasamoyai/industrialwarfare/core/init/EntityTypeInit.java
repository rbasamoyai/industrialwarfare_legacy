package rbasamoyai.industrialwarfare.core.init;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.entities.BulletEntity;
import rbasamoyai.industrialwarfare.common.entities.FormationLeaderEntity;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;

@Mod.EventBusSubscriber(modid = IndustrialWarfare.MOD_ID, bus = Bus.MOD)
public class EntityTypeInit {
	
	public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITIES, IndustrialWarfare.MOD_ID);
	
	public static final RegistryObject<EntityType<NPCEntity>> NPC = ENTITY_TYPES.register("npc",
			() -> EntityType.Builder.<NPCEntity>of(NPCEntity::new, MobCategory.CREATURE)
					.sized(0.6f, 1.8f)
					.clientTrackingRange(10)
					.setTrackingRange(8)
					.build(makeId("npc").toString()));
	
	public static final RegistryObject<EntityType<BulletEntity>> BULLET = ENTITY_TYPES.register("bullet",
			() -> EntityType.Builder.<BulletEntity>of(BulletEntity::new, MobCategory.MISC)
					.sized(0.125f, 0.125f)
					.clientTrackingRange(4)
					.updateInterval(1)
					.build(makeId("bullet").toString()));
	
	public static final RegistryObject<EntityType<FormationLeaderEntity>> FORMATION_LEADER = ENTITY_TYPES.register("formation_leader",
			() -> EntityType.Builder.<FormationLeaderEntity>of(FormationLeaderEntity::new, MobCategory.MISC)
					.sized(0.6f, 1.8f)
					.setTrackingRange(8)
					.noSummon()
					.build(makeId("formation_leader").toString()));
	
	@SubscribeEvent
	public static void addEntityAttributes(EntityAttributeCreationEvent event) {
		event.put(EntityTypeInit.NPC.get(), NPCEntity.setAttributes().build());
		event.put(EntityTypeInit.FORMATION_LEADER.get(), FormationLeaderEntity.setAttributes().build());
	}
	
	private static ResourceLocation makeId(String id) {
		return new ResourceLocation(IndustrialWarfare.MOD_ID, id);
	}
	
}
