package rbasamoyai.industrialwarfare.common.capabilities.entities.npc;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import rbasamoyai.industrialwarfare.common.diplomacy.PlayerIDTag;
import rbasamoyai.industrialwarfare.core.IWModRegistries;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class NPCDataCapability {
	
	private static final String TAG_FIRST_OWNER = "firstOwner";
	private static final String TAG_CURRENT_OWNER = "currentOwner";
	private static final String TAG_PROFESSION = "profession";
	private static final String TAG_COMBAT_SKILL = "combatSkill";
	private static final String TAG_CAN_WEAR_EQUIPMENT = "canWearEquipment";
	private static final String TAG_KNOWN_RECIPE = "knownRecipe";
	private static final String TAG_SKILL = "currentRecipeSkill";
	
	@CapabilityInject(INPCDataHandler.class)
	public static Capability<INPCDataHandler> NPC_DATA_CAPABILITY = null;
	
	public static void register() {
		CapabilityManager.INSTANCE.register(INPCDataHandler.class, new Storage(), NPCDataHandler::new);
	}
	
	public static class Storage implements IStorage<INPCDataHandler> {

		@Nullable
		@Override
		public INBT writeNBT(Capability<INPCDataHandler> capability, INPCDataHandler instance, Direction side) {
			CompoundNBT tag = new CompoundNBT();
			tag.putBoolean(TAG_CAN_WEAR_EQUIPMENT, instance.canWearEquipment());
			tag.putString(TAG_COMBAT_SKILL, instance.getCombatSkill().getRegistryName().toString());
			tag.putString(TAG_PROFESSION, instance.getProfession().getRegistryName().toString());
			tag.put(TAG_FIRST_OWNER, instance.getFirstOwner().serializeNBT());
			tag.put(TAG_CURRENT_OWNER, instance.getOwner().serializeNBT());
			ItemStack recipeItem = instance.getRecipeItem();
			tag.put(TAG_KNOWN_RECIPE, recipeItem != null ? recipeItem.serializeNBT() : new CompoundNBT());
			tag.putFloat(TAG_SKILL, instance.getSkill());
			return tag;
		}

		@Override
		public void readNBT(Capability<INPCDataHandler> capability, INPCDataHandler instance, Direction side,
				INBT nbt) {
			CompoundNBT tag = (CompoundNBT) nbt;
			instance.setCanWearEquipment(tag.getBoolean(TAG_CAN_WEAR_EQUIPMENT));
			instance.setCombatSkill(IWModRegistries.NPC_COMBAT_SKILLS.getValue(new ResourceLocation(tag.getString(TAG_COMBAT_SKILL))));
			instance.setProfession(IWModRegistries.NPC_PROFESSIONS.getValue(new ResourceLocation(tag.getString(TAG_PROFESSION))));
			instance.setFirstOwner(PlayerIDTag.fromNBT(tag.getCompound(TAG_FIRST_OWNER)));
			instance.setOwner(PlayerIDTag.fromNBT(tag.getCompound(TAG_CURRENT_OWNER)));
			instance.setRecipeItem(ItemStack.of(tag.getCompound(TAG_KNOWN_RECIPE)));
			instance.setSkill(tag.getFloat(TAG_SKILL));
		}
		
	}
	
}
