package rbasamoyai.industrialwarfare.core;

import java.util.function.Supplier;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegistryBuilder;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.entityai.NPCComplaint;
import rbasamoyai.industrialwarfare.common.entityai.formation.FormationAttackType;
import rbasamoyai.industrialwarfare.common.entityai.formation.UnitFormationType;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.TaskScrollCommand;
import rbasamoyai.industrialwarfare.common.npccombatskill.NPCCombatSkill;
import rbasamoyai.industrialwarfare.common.npcprofessions.NPCProfession;

/**
 * Time to take notes from {@link net.minecraftforge.registries.ForgeRegistries} and {@link net.minecraftforge.registries.GameData}
 * 
 * @author rbasamoyai
 */

@EventBusSubscriber(modid = IndustrialWarfare.MOD_ID, bus = Bus.MOD)
public class IWModRegistries {

	public static Supplier<IForgeRegistry<FormationAttackType>> FORMATION_ATTACK_TYPES = null;
	public static Supplier<IForgeRegistry<NPCCombatSkill>> NPC_COMBAT_SKILLS = null;
	public static Supplier<IForgeRegistry<NPCComplaint>> NPC_COMPLAINTS = null;
	public static Supplier<IForgeRegistry<NPCProfession>> NPC_PROFESSIONS = null;
	public static Supplier<IForgeRegistry<TaskScrollCommand>> TASK_SCROLL_COMMANDS = null;
	public static Supplier<IForgeRegistry<UnitFormationType<?>>> UNIT_FORMATION_TYPES = null;
	
	@SubscribeEvent
	public static void buildModRegistries(NewRegistryEvent event) {
		IndustrialWarfare.LOGGER.info("Starting registry building for IndustrialWarfare by rbasamoyai");
		
		FORMATION_ATTACK_TYPES = event.create(new RegistryBuilder<FormationAttackType>()
				.setName(KEY_FORMATION_ATTACK_TYPES.location())
				.setMaxID(MAX_ID)
				.setType(FormationAttackType.class)
				.setDefaultKey(new ResourceLocation(IndustrialWarfare.MOD_ID, "fire_at_will"))
				.allowModification());
		
		NPC_COMBAT_SKILLS = event.create(new RegistryBuilder<NPCCombatSkill>()
				.setName(KEY_NPC_COMBAT_SKILLS.location())
				.setMaxID(MAX_ID)
				.setType(NPCCombatSkill.class)
				.setDefaultKey(new ResourceLocation(IndustrialWarfare.MOD_ID, "untrained"))
				.allowModification());
		
		NPC_COMPLAINTS = event.create(new RegistryBuilder<NPCComplaint>()
				.setName(KEY_NPC_COMPLAINTS.location())
				.setMaxID(MAX_ID)
				.setType(NPCComplaint.class)
				.setDefaultKey(new ResourceLocation(IndustrialWarfare.MOD_ID, "cant_open"))
				.allowModification());
		
		NPC_PROFESSIONS = event.create(new RegistryBuilder<NPCProfession>()
				.setName(KEY_NPC_PROFESSIONS.location())
				.setMaxID(MAX_ID)
				.setType(NPCProfession.class)
				.setDefaultKey(new ResourceLocation(IndustrialWarfare.MOD_ID, "jobless"))
				.allowModification());
		
		TASK_SCROLL_COMMANDS = event.create(new RegistryBuilder<TaskScrollCommand>() 
				.setName(KEY_TASK_COMMANDS.location())
				.setMaxID(MAX_ID)
				.setType(TaskScrollCommand.class)
				.setDefaultKey(new ResourceLocation(IndustrialWarfare.MOD_ID, "move_to"))
				.allowModification());
		
		UNIT_FORMATION_TYPES = event.create(new RegistryBuilder<UnitFormationType<?>>()
				.setName(KEY_UNIT_FORMATION_TYPES.location())
				.setMaxID(MAX_ID)
				.setType(UnitFormationType.CLASS_GENERIC) // This is unholy, probably
				.setDefaultKey(new ResourceLocation(IndustrialWarfare.MOD_ID, "line"))
				.allowModification());
		
		IndustrialWarfare.LOGGER.info("Finished registry building for Industrial Warfare by rbasamoyai");
	}
	
	public static final ResourceKey<Registry<FormationAttackType>> KEY_FORMATION_ATTACK_TYPES = key("formation_attack_types");
	public static final ResourceKey<Registry<NPCCombatSkill>> KEY_NPC_COMBAT_SKILLS = key("npc_combat_skills");
	public static final ResourceKey<Registry<NPCComplaint>> KEY_NPC_COMPLAINTS = key("npc_complaints");
	public static final ResourceKey<Registry<NPCProfession>> KEY_NPC_PROFESSIONS = key("npc_professions");
	public static final ResourceKey<Registry<TaskScrollCommand>> KEY_TASK_COMMANDS = key("task_commands");
	public static final ResourceKey<Registry<UnitFormationType<?>>> KEY_UNIT_FORMATION_TYPES = key("unit_formation_types");
	
	private static final int MAX_ID = Integer.MAX_VALUE - 1;
	
	/**
	 * Pretty much copied from {@link net.minecraftforge.registries.ForgeRegistries.Keys#key}
	 */
	private static <T> ResourceKey<Registry<T>> key(String id) {
		return ResourceKey.createRegistryKey(new ResourceLocation(IndustrialWarfare.MOD_ID, id));
	}
}
