package rbasamoyai.industrialwarfare.common.npcprofessions;

import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;

public class UntrainedCombatUnit implements ICombatUnit {
	
	private static final float GAUSSIAN_MIDPOINT = 0.6f;
	private static final float MAX_DEVIATION = 0.2f;
	
	@Override
	public float getEffectiveness(NPCEntity npc) {
		// Random effectiveness, no skill taken from NPC
		return GAUSSIAN_MIDPOINT + (float) npc.getRandom().nextGaussian() * MAX_DEVIATION;
	}
	
	private static final float BASE_TIME = 1.0f;
	private static final float MAX_MODIFIER = 0.5f;
	
	@Override
	public float getTimeModifier(NPCEntity npc) {
		// Extends the time, no shortening of it
		return BASE_TIME + npc.getRandom().nextFloat() * MAX_MODIFIER;
	}
	
	@Override
	public boolean canUseRangedWeapon(NPCEntity npc, ItemStack weapon) {
		Item weaponItem = weapon.getItem();
		return weaponItem instanceof CrossbowItem /* TODO: gun item */;
	}
	
}
