package rbasamoyai.industrialwarfare.common.npcprofessions;

import rbasamoyai.industrialwarfare.common.entities.NPCEntity;

public interface ICombatUnit {

	public FightMode getFightMode(NPCEntity npc);
	
	public float getEffectiveness(NPCEntity npc);
	
}
