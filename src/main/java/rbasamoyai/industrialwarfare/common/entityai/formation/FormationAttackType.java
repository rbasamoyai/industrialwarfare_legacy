package rbasamoyai.industrialwarfare.common.entityai.formation;

import net.minecraftforge.registries.ForgeRegistryEntry;

public class FormationAttackType extends ForgeRegistryEntry<FormationAttackType> {

	private final String name;
	private final int hashCode;
	
	public FormationAttackType(String name) {
		this.name = name;
		this.hashCode = name.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj != null && obj instanceof FormationAttackType) {
			return this.name.equals(((FormationAttackType) obj).name);
		} else {
			return false;
		}
	}
	
	@Override
	public String toString() {
		return this.name;
	}
	
	@Override public int hashCode() { return this.hashCode; }
	
}
