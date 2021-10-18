package rbasamoyai.industrialwarfare.core.init;

import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.npcprofessions.JoblessWorkstationComponent;
import rbasamoyai.industrialwarfare.common.npcprofessions.NPCProfession;
import rbasamoyai.industrialwarfare.common.npcprofessions.NormalWorkstationProfessionComponent;

public class NPCProfessionInit {
	
	public static final DeferredRegister<NPCProfession> PROFESSIONS = DeferredRegister.create(NPCProfession.class, IndustrialWarfare.MOD_ID);
	
	public static final RegistryObject<NPCProfession> ASSEMBLER = PROFESSIONS.register("assembler",
			() -> new NPCProfession(new NormalWorkstationProfessionComponent(BlockInit.ASSEMBLER_WORKSTATION.get())));
	
	public static final RegistryObject<NPCProfession> JOBLESS = PROFESSIONS.register("jobless",
			() -> new NPCProfession(new JoblessWorkstationComponent()));
	
}
