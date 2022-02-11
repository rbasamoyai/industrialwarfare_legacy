package rbasamoyai.industrialwarfare.common.entityai.formation;

import java.util.function.Function;

import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistryEntry;
import rbasamoyai.industrialwarfare.common.entityai.formation.formations.UnitFormation;

public class UnitFormationType<F extends UnitFormation> extends ForgeRegistryEntry<UnitFormationType<?>> {
	
	@SuppressWarnings("unchecked")
	public static final Class<UnitFormationType<?>> CLASS_GENERIC = (Class<UnitFormationType<?>>)((Class<?>) UnitFormationType.class); 
	
	private final Function<World, F> formationProvider;
	
	public UnitFormationType(Function<World, F> formationProvider) {
		this.formationProvider = formationProvider;
	}
	
	public F getFormation(World level) { return this.formationProvider.apply(level); }
	
}
