package rbasamoyai.industrialwarfare.common.tags;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import rbasamoyai.industrialwarfare.IndustrialWarfare;

public class IWItemTags {

	public static final TagKey<Item> CHEAT_AMMO = create("cheat_ammo");
	public static final TagKey<Item> ENTRENCHING_TOOLS = create("entrenching_tools");
	public static final TagKey<Item> FIREARM_MELEE_ATTACHMENTS = create("attachments/firearm/melee");
	public static final TagKey<Item> FIREARM_OPTIC_ATTACHMENTS = create("attachments/firearm/optic");
	public static final TagKey<Item> FUNGUS = create("fungus");
	
	protected static final TagKey<Item> create(String id) {
		return ItemTags.create(new ResourceLocation(IndustrialWarfare.MOD_ID, id));
	}
	
}
