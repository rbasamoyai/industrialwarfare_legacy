package rbasamoyai.industrialwarfare.core.init;

import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.npcprofessions.JoblessProfession;
import rbasamoyai.industrialwarfare.common.npcprofessions.LoggerProfession;
import rbasamoyai.industrialwarfare.common.npcprofessions.NPCProfession;
import rbasamoyai.industrialwarfare.common.npcprofessions.QuarrierProfession;
import rbasamoyai.industrialwarfare.common.npcprofessions.WorkstationProfession;

public class NPCProfessionInit {
	
	public static final DeferredRegister<NPCProfession> PROFESSIONS = DeferredRegister.create(NPCProfession.class, IndustrialWarfare.MOD_ID);
	
	public static final RegistryObject<NPCProfession> ASSEMBLER = PROFESSIONS.register("assembler",
			() -> new WorkstationProfession(BlockInit.ASSEMBLER_WORKSTATION.get()));
	
	public static final RegistryObject<NPCProfession> JOBLESS = PROFESSIONS.register("jobless", JoblessProfession::new);
	
	public static final RegistryObject<NPCProfession> QUARRIER = PROFESSIONS.register("quarrier", QuarrierProfession::new);
	
	public static final RegistryObject<NPCProfession> LOGGER = PROFESSIONS.register("logger", LoggerProfession::new);
	
}
