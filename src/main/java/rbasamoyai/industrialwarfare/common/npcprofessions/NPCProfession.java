package rbasamoyai.industrialwarfare.common.npcprofessions;

import net.minecraftforge.registries.ForgeRegistryEntry;

public class NPCProfession extends ForgeRegistryEntry<NPCProfession> {

	private final IWorkUnit workUnit;
	
	public NPCProfession(IWorkUnit workUnit) {
		this.workUnit = workUnit;
	}
	
	public IWorkUnit getWorkUnit() {
		return this.workUnit;
	}
	
}
