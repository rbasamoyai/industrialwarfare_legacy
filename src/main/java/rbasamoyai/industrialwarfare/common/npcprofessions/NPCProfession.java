package rbasamoyai.industrialwarfare.common.npcprofessions;

import net.minecraftforge.registries.ForgeRegistryEntry;

public class NPCProfession extends ForgeRegistryEntry<NPCProfession> {

	private final IWorkstationProfessionComponent workComponent;
	
	public NPCProfession(IWorkstationProfessionComponent workComponent) {
		this.workComponent = workComponent;
	}
	
	public IWorkstationProfessionComponent getWorkComponent() {
		return this.workComponent;
	}
	
}
