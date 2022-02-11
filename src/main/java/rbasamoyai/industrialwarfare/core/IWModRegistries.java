package rbasamoyai.industrialwarfare.core;

import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.entityai.NPCComplaint;
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

	public static IForgeRegistry<NPCCombatSkill> NPC_COMBAT_SKILLS = null;
	public static IForgeRegistry<NPCComplaint> NPC_COMPLAINTS = null;
	public static IForgeRegistry<NPCProfession> NPC_PROFESSIONS = null;
	public static IForgeRegistry<TaskScrollCommand> TASK_SCROLL_COMMANDS = null;
	public static IForgeRegistry<UnitFormationType<?>> UNIT_FORMATION_TYPES = null;
	
	@SubscribeEvent
	public static void buildModRegistries(RegistryEvent.NewRegistry event) {
		IndustrialWarfare.LOGGER.info("Starting registry building for IndustrialWarfare by rbasamoyai");
		
		NPC_COMBAT_SKILLS = new RegistryBuilder<NPCCombatSkill>()
				.setName(KEY_NPC_COMBAT_SKILLS.location())
				.setMaxID(MAX_ID)
				.setType(NPCCombatSkill.class)
				.setDefaultKey(new ResourceLocation(IndustrialWarfare.MOD_ID, "untrained"))
				.allowModification()
				.create();
		
		NPC_COMPLAINTS = new RegistryBuilder<NPCComplaint>()
				.setName(KEY_NPC_COMPLAINTS.location())
				.setMaxID(MAX_ID)
				.setType(NPCComplaint.class)
				.setDefaultKey(new ResourceLocation(IndustrialWarfare.MOD_ID, "cant_open"))
				.allowModification()
				.create();
		
		NPC_PROFESSIONS = new RegistryBuilder<NPCProfession>()
				.setName(KEY_NPC_PROFESSIONS.location())
				.setMaxID(MAX_ID)
				.setType(NPCProfession.class)
				.setDefaultKey(new ResourceLocation(IndustrialWarfare.MOD_ID, "jobless"))
				.allowModification()
				.create();
		
		TASK_SCROLL_COMMANDS = new RegistryBuilder<TaskScrollCommand>() 
				.setName(KEY_TASK_COMMANDS.location())
				.setMaxID(MAX_ID)
				.setType(TaskScrollCommand.class)
				.setDefaultKey(new ResourceLocation(IndustrialWarfare.MOD_ID, "move_to"))
				.allowModification()
				.create();
		
		UNIT_FORMATION_TYPES = new RegistryBuilder<UnitFormationType<?>>()
				.setName(KEY_UNIT_FORMATION_TYPES.location())
				.setMaxID(MAX_ID)
				.setType(UnitFormationType.CLASS_GENERIC) // This is unholy, probably
				.setDefaultKey(new ResourceLocation(IndustrialWarfare.MOD_ID, "line"))
				.allowModification()
				.create();
		
		IndustrialWarfare.LOGGER.info("Finished registry building for Industrial Warfare by rbasamoyai");
	}
	
	private static final RegistryKey<Registry<NPCCombatSkill>> KEY_NPC_COMBAT_SKILLS = key("npc_combat_skills");
	private static final RegistryKey<Registry<NPCComplaint>> KEY_NPC_COMPLAINTS = key("npc_complaints");
	private static final RegistryKey<Registry<NPCProfession>> KEY_NPC_PROFESSIONS = key("npc_professions");
	private static final RegistryKey<Registry<TaskScrollCommand>> KEY_TASK_COMMANDS = key("task_commands");
	private static final RegistryKey<Registry<UnitFormationType<?>>> KEY_UNIT_FORMATION_TYPES = key("unit_formation_types");
	
	private static final int MAX_ID = Integer.MAX_VALUE - 1;
	
	/**
	 * Pretty much copied from {@link net.minecraftforge.registries.ForgeRegistries.Keys#key}
	 */
	private static <T> RegistryKey<Registry<T>> key(String id) {
		return RegistryKey.createRegistryKey(new ResourceLocation(IndustrialWarfare.MOD_ID, id));
	}
}
