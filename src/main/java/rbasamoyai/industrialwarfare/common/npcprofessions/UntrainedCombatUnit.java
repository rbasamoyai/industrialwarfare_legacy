package rbasamoyai.industrialwarfare.common.npcprofessions;

import java.util.Random;

import rbasamoyai.industrialwarfare.common.entities.NPCEntity;

public class UntrainedCombatUnit implements ICombatUnit {
	
	private static final Random RNG = new Random();
	private static final float GAUSSIAN_MIDPOINT = 0.6f;
	private static final float MAX_DEVIATION = 0.2f;

	@Override
	public FightMode getFightMode(NPCEntity npc) {
		return FightMode.MELEE;
	}
	
	@Override
	public float getEffectiveness(NPCEntity npc) {
		// Random effectiveness, no skill taken from NPC
		return GAUSSIAN_MIDPOINT + (float) RNG.nextGaussian() * MAX_DEVIATION;
	}
	
}
