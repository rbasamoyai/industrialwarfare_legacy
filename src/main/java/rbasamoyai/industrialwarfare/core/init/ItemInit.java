package rbasamoyai.industrialwarfare.core.init;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.items.LabelItem;
import rbasamoyai.industrialwarfare.common.items.PartItem;
import rbasamoyai.industrialwarfare.common.items.RecipeItem;
import rbasamoyai.industrialwarfare.common.items.ScheduleItem;
import rbasamoyai.industrialwarfare.common.items.debugitems.ComplaintRemoverItem;
import rbasamoyai.industrialwarfare.common.items.debugitems.DebugOwnerItem;
import rbasamoyai.industrialwarfare.common.items.debugitems.JobSitePointerItem;
import rbasamoyai.industrialwarfare.common.items.debugitems.ModSpawnEggItem;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollItem;
import rbasamoyai.industrialwarfare.core.itemgroup.IWItemGroups;

/*
 * Item initialization for rbasamoyai's Industrial Warfare.
 */

public class ItemInit {
	
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, IndustrialWarfare.MOD_ID);
	
	public static final RegistryObject<Item> HAMMER = ITEMS.register("hammer", ItemInit::toolItem);
	public static final RegistryObject<Item> WAND = ITEMS.register("wand", ItemInit::toolItem);
	
	public static final RegistryObject<Item> JOB_SITE_POINTER = ITEMS.register("job_site_pointer", JobSitePointerItem::new);
	public static final RegistryObject<Item> COMPLAINT_REMOVER = ITEMS.register("complaint_remover", ComplaintRemoverItem::new);
	public static final RegistryObject<Item> DEBUG_OWNER = ITEMS.register("debug_owner", DebugOwnerItem::new);
	public static final RegistryObject<Item> NPC_SPAWN_EGG = ITEMS.register("npc_spawn_egg", () ->
			new ModSpawnEggItem(EntityTypeInit.NPC, 0x0000afaf, 0x00c69680, new Item.Properties().tab(IWItemGroups.TAB_DEBUG)));
	
	public static final RegistryObject<Item> CURED_FLESH = ITEMS.register("cured_flesh", ItemInit::generalGenericItem);
	public static final RegistryObject<Item> BODY_PART = ITEMS.register("body_part", ItemInit::generalGenericItem);
	public static final RegistryObject<Item> MAKESHIFT_BRAIN = ITEMS.register("makeshift_brain", ItemInit::generalGenericItem);
	public static final RegistryObject<Item> MAKESHIFT_HEAD = ITEMS.register("makeshift_head", ItemInit::generalGenericItem);
	
	public static final RegistryObject<Item> ASSEMBLER_WORKSTATION = registerBlockItem(BlockInit.ASSEMBLER_WORKSTATION);
	public static final RegistryObject<Item> TASK_SCROLL_SHELF = registerBlockItem(BlockInit.TASK_SCROLL_SHELF);
	
	public static final RegistryObject<Item> PART_IRON_WIRE = ITEMS.register("part_iron_wire", PartItem::new);
	public static final RegistryObject<Item> PART_SCREW = ITEMS.register("part_screw", PartItem::new);
	
	public static final RegistryObject<Item> RECIPE_MANUAL = ITEMS.register("recipe_manual", RecipeItem::new);
	
	public static final RegistryObject<Item> TASK_SCROLL = ITEMS.register("task_scroll", TaskScrollItem::new);
	public static final RegistryObject<Item> LABEL = ITEMS.register("label", LabelItem::new);
	public static final RegistryObject<Item> SCHEDULE = ITEMS.register("schedule", ScheduleItem::new);
	
	private static Item toolItem() {
		return new Item(new Item.Properties().stacksTo(1).tab(IWItemGroups.TAB_GENERAL));
	}
	
	private static Item generalGenericItem() {
		return new Item(new Item.Properties().tab(IWItemGroups.TAB_GENERAL));
	}
	
	private static RegistryObject<Item> registerBlockItem(RegistryObject<Block> blockObject) {
		return ITEMS.register(blockObject.getId().getPath(), () -> new BlockItem(blockObject.get(), new Item.Properties().tab(IWItemGroups.TAB_BLOCKS)));
	}
}
