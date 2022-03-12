package rbasamoyai.industrialwarfare.common.entityai.formation;

import java.util.function.Supplier;

import net.minecraftforge.registries.ForgeRegistryEntry;
import rbasamoyai.industrialwarfare.common.entityai.formation.formations.UnitFormation;

public class UnitFormationType<F extends UnitFormation> extends ForgeRegistryEntry<UnitFormationType<?>> {
	
	@SuppressWarnings("unchecked")
	public static final Class<UnitFormationType<?>> CLASS_GENERIC = (Class<UnitFormationType<?>>)((Class<?>) UnitFormationType.class); 
	
	private final Supplier<F> formationProvider;
	
	public UnitFormationType(Supplier<F> formationProvider) {
		this.formationProvider = formationProvider;
	}
	
	public F getFormation() { return this.formationProvider.get(); }
	
}
