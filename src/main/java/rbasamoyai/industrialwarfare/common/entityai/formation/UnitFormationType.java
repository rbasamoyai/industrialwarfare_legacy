package rbasamoyai.industrialwarfare.common.entityai.formation;

import net.minecraftforge.registries.ForgeRegistryEntry;
import rbasamoyai.industrialwarfare.common.entityai.formation.formations.UnitFormation;

public class UnitFormationType<F extends UnitFormation> extends ForgeRegistryEntry<UnitFormationType<?>> {
	
	@SuppressWarnings("unchecked")
	public static final Class<UnitFormationType<?>> CLASS_GENERIC = (Class<UnitFormationType<?>>)((Class<?>) UnitFormationType.class); 
	
	private final IFactory<F> factory;
	private final int formationRank;
	
	public UnitFormationType(IFactory<F> factory, int formationRank) {
		this.factory = factory;
		this.formationRank = formationRank;
	}
	
	public F getFormation(int formationRank) { return this.factory.create(this, formationRank); }
	
	public int getFormationRank() { return this.formationRank; }
	
	public static interface IFactory<T extends UnitFormation> {
		T create(UnitFormationType<T> type, int formationRank);
	}
	
}
