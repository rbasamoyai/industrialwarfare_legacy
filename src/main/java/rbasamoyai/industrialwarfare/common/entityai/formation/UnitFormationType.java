package rbasamoyai.industrialwarfare.common.entityai.formation;

import com.google.common.collect.ImmutableSet;

import net.minecraftforge.registries.ForgeRegistryEntry;
import rbasamoyai.industrialwarfare.common.entityai.formation.formations.UnitFormation;
import rbasamoyai.industrialwarfare.common.items.WhistleItem.FormationCategory;

public class UnitFormationType<F extends UnitFormation> extends ForgeRegistryEntry<UnitFormationType<?>> {
	
	@SuppressWarnings("unchecked")
	public static final Class<UnitFormationType<?>> CLASS_GENERIC = (Class<UnitFormationType<?>>)((Class<?>) UnitFormationType.class); 
	
	private final IFactory<F> factory;
	private final int formationRank;
	private final FormationCategory category;
	private final ImmutableSet<FormationAttackType> validAttackTypes;
	
	public UnitFormationType(IFactory<F> factory, int formationRank, FormationCategory category, ImmutableSet<FormationAttackType> validAttackTypes) {
		this.factory = factory;
		this.formationRank = formationRank;
		this.category = category;
		this.validAttackTypes = validAttackTypes;
	}
	
	public F getFormation(int formationRank) {
		return this.factory.create(this, formationRank);
	}
	
	public int getFormationRank() { return this.formationRank; }
	public FormationCategory getCategory() { return this.category;}
	public boolean checkAttackType(FormationAttackType type) { return this.validAttackTypes.contains(type); }	
	
	public static interface IFactory<T extends UnitFormation> {
		T create(UnitFormationType<T> type, int formationRank);
	}
	
	@Override
	public String toString() {
		return this.getRegistryName().toString();
	}
	
}
