package rbasamoyai.industrialwarfare.core.init;

import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.npccombatskill.NPCCombatSkill;
import rbasamoyai.industrialwarfare.common.npccombatskill.UntrainedCombatSkill;
import rbasamoyai.industrialwarfare.core.IWModRegistries;

public class NPCCombatSkillInit {

	public static final DeferredRegister<NPCCombatSkill> COMBAT_SKILLS = DeferredRegister.create(IWModRegistries.KEY_NPC_COMBAT_SKILLS, IndustrialWarfare.MOD_ID);
	
	public static final RegistryObject<NPCCombatSkill> UNTRAINED = COMBAT_SKILLS.register("untrained", UntrainedCombatSkill::new);
	
}
