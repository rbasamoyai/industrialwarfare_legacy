package rbasamoyai.industrialwarfare.common.entityai.formation;

import net.minecraft.entity.CreatureEntity;

public class FormationEntityWrapper<E extends CreatureEntity & IMovesInFormation> {

	private final E entity;
	
	public FormationEntityWrapper(E entity) {
		this.entity = entity;
	}
	
	public E getEntity() { return this.entity; }
	
}
