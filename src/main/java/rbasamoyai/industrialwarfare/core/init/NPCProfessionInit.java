package rbasamoyai.industrialwarfare.core.init;

import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.npcprofessions.JoblessWorkUnit;
import rbasamoyai.industrialwarfare.common.npcprofessions.NPCProfession;
import rbasamoyai.industrialwarfare.common.npcprofessions.WorkstationWorkUnit;

public class NPCProfessionInit {
	
	public static final DeferredRegister<NPCProfession> PROFESSIONS = DeferredRegister.create(NPCProfession.class, IndustrialWarfare.MOD_ID);
	
	public static final RegistryObject<NPCProfession> ASSEMBLER = PROFESSIONS.register("assembler",
			() -> NPCProfession.untrainedCombat(new WorkstationWorkUnit(BlockInit.ASSEMBLER_WORKSTATION.get())));
	
	public static final RegistryObject<NPCProfession> JOBLESS = PROFESSIONS.register("jobless",
			() -> NPCProfession.untrainedCombat(new JoblessWorkUnit()));
	
}
