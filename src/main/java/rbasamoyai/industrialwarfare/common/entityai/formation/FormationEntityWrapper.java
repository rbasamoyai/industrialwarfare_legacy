package rbasamoyai.industrialwarfare.common.entityai.formation;

import net.minecraft.world.entity.PathfinderMob;

public class FormationEntityWrapper<E extends PathfinderMob & MovesInFormation> {

	private final E entity;
	
	public FormationEntityWrapper(E entity) {
		this.entity = entity;
	}
	
	public E getEntity() { return this.entity; }
	
	public static final FormationEntityWrapper<?> EMPTY = new FormationEntityWrapper<>(null);
	
}
