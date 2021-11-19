package rbasamoyai.industrialwarfare.common.npcprofessions;

import java.util.Objects;

import net.minecraftforge.registries.ForgeRegistryEntry;

public class NPCProfession extends ForgeRegistryEntry<NPCProfession> {

	private final IWorkUnit workUnit;
	private final ICombatUnit combatUnit;
	
	public NPCProfession(IWorkUnit workUnit, ICombatUnit combatUnit) {
		Objects.requireNonNull(workUnit);
		Objects.requireNonNull(combatUnit);
		this.workUnit = workUnit;
		this.combatUnit = combatUnit;
	}
	
	public static NPCProfession untrainedCombat(IWorkUnit workUnit) {
		return new NPCProfession(workUnit, new UntrainedCombatUnit());
	}
	
	public IWorkUnit getWorkUnit() { return this.workUnit; }
	public ICombatUnit getCombatUnit() { return this.combatUnit; }
	
}
