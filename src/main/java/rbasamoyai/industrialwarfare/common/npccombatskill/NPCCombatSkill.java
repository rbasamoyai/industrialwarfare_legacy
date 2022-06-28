package rbasamoyai.industrialwarfare.common.npccombatskill;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistryEntry;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;

public abstract class NPCCombatSkill extends ForgeRegistryEntry<NPCCombatSkill> {
	
	public abstract float getEffectiveness(NPCEntity npc);
	public abstract float getTimeModifier(NPCEntity npc);
	
	public abstract boolean canUseRangedWeapon(NPCEntity npc, ItemStack weapon);
	
}
