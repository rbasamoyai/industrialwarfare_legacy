package rbasamoyai.industrialwarfare.core.init;

import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.npccombatskill.NPCCombatSkill;
import rbasamoyai.industrialwarfare.common.npccombatskill.UntrainedCombatSkill;

public class NPCCombatSkillInit {

	public static final DeferredRegister<NPCCombatSkill> COMBAT_SKILLS = DeferredRegister.create(NPCCombatSkill.class, IndustrialWarfare.MOD_ID);
	
	public static final RegistryObject<NPCCombatSkill> UNTRAINED = COMBAT_SKILLS.register("untrained", UntrainedCombatSkill::new);
	
}
