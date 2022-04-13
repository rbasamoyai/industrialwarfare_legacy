package rbasamoyai.industrialwarfare.common.tags;

import net.minecraft.item.Item;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import rbasamoyai.industrialwarfare.IndustrialWarfare;

public class IWItemTags {

	public static final ITag.INamedTag<Item> CHEAT_AMMO = bind("cheat_ammo");
	public static final ITag.INamedTag<Item> ENTRENCHING_TOOLS = bind("entrenching_tools");
	public static final ITag.INamedTag<Item> FIREARM_MELEE_ATTACHMENTS = bind("attachments/firearm/melee");
	public static final ITag.INamedTag<Item> FIREARM_OPTIC_ATTACHMENTS = bind("attachments/firearm/optic");
	
	protected static final ITag.INamedTag<Item> bind(String id) {
		return ItemTags.bind(IndustrialWarfare.MOD_ID + ":" + id);
	}
	
}
