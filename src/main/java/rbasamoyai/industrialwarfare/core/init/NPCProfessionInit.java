package rbasamoyai.industrialwarfare.core.init;

import com.google.common.collect.ImmutableList;

import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.entityai.SupplyRequestPredicate;
import rbasamoyai.industrialwarfare.common.entityai.SupplyRequestPredicate.IntBound;
import rbasamoyai.industrialwarfare.common.npcprofessions.JoblessProfession;
import rbasamoyai.industrialwarfare.common.npcprofessions.NPCProfession;
import rbasamoyai.industrialwarfare.common.npcprofessions.ResourceGatheringProfession;
import rbasamoyai.industrialwarfare.common.npcprofessions.WorkstationProfession;
import rbasamoyai.industrialwarfare.core.init.items.ItemInit;

public class NPCProfessionInit {
	
	public static final DeferredRegister<NPCProfession> PROFESSIONS = DeferredRegister.create(NPCProfession.class, IndustrialWarfare.MOD_ID);
	
	public static final RegistryObject<NPCProfession> ASSEMBLER = PROFESSIONS.register("assembler",
			() -> new WorkstationProfession(BlockInit.ASSEMBLER_WORKSTATION.get()));
	
	public static final RegistryObject<NPCProfession> JOBLESS = PROFESSIONS.register("jobless", JoblessProfession::new);
	
	public static final RegistryObject<NPCProfession> QUARRIER = PROFESSIONS.register("quarrier",
			() -> new ResourceGatheringProfession(
					ImmutableList.of(
							SupplyRequestPredicate.forTool(ToolType.PICKAXE, IntBound.atLeast(1)),
							SupplyRequestPredicate.forTool(ToolType.SHOVEL, IntBound.ANY),
							SupplyRequestPredicate.forItem(ItemInit.WORKER_SUPPORT.get(), IntBound.atLeast(1))),
					BlockInit.QUARRY.get()));
	
}
