package rbasamoyai.industrialwarfare.common.npcprofessions;

import net.minecraft.item.ItemStack;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;

public interface ICombatUnit {
	
	float getEffectiveness(NPCEntity npc);
	float getTimeModifier(NPCEntity npc);
	
	boolean canUseRangedWeapon(NPCEntity npc, ItemStack weapon);
	
}
