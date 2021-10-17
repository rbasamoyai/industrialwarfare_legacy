package rbasamoyai.industrialwarfare.core.init;

import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.ObjectHolder;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.npcprofessions.JoblessWorkstationComponent;
import rbasamoyai.industrialwarfare.common.npcprofessions.NPCProfession;
import rbasamoyai.industrialwarfare.common.npcprofessions.NormalWorkstationProfessionComponent;

@Mod.EventBusSubscriber(modid = IndustrialWarfare.MOD_ID, bus = Bus.MOD)
@ObjectHolder(IndustrialWarfare.MOD_ID)
public class NPCProfessionInit {
	
	public static final NPCProfession ASSEMBLER = null;
	public static final NPCProfession JOBLESS = null;

	@SubscribeEvent
	public static void registerProfessions(RegistryEvent.Register<NPCProfession> event) {
		event.getRegistry().registerAll(new NPCProfession[] {
				new NPCProfession(new NormalWorkstationProfessionComponent(BlockInit.ASSEMBLER_WORKSTATION)).setRegistryName(IndustrialWarfare.MOD_ID, "assembler"),
				new NPCProfession(new JoblessWorkstationComponent()).setRegistryName("jobless")
		});
	}
	
}
