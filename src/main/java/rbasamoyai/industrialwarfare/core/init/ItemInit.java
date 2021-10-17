package rbasamoyai.industrialwarfare.core.init;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.ObjectHolder;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.items.LabelItem;
import rbasamoyai.industrialwarfare.common.items.PartItem;
import rbasamoyai.industrialwarfare.common.items.RecipeItem;
import rbasamoyai.industrialwarfare.common.items.ScheduleItem;
import rbasamoyai.industrialwarfare.common.items.debugitems.ComplaintRemoverItem;
import rbasamoyai.industrialwarfare.common.items.debugitems.DebugOwnerItem;
import rbasamoyai.industrialwarfare.common.items.debugitems.JobSitePointerItem;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollItem;
import rbasamoyai.industrialwarfare.core.itemgroup.IWItemGroups;

/*
 * Item initialization for rbasamoyai's Industrial Warfare.
 */

@Mod.EventBusSubscriber(modid = IndustrialWarfare.MOD_ID, bus = Bus.MOD)
@ObjectHolder(IndustrialWarfare.MOD_ID)
public class ItemInit {
	
	public static final Item HAMMER = null;
	public static final Item WAND = null;
	
	public static final Item JOB_SITE_POINTER = null;
	public static final Item COMPLAINT_REMOVER = null;
	public static final Item DEBUG_OWNER = null;
	
	public static final Item CURED_FLESH = null;
	public static final Item BODY_PART = null;
	public static final Item MAKESHIFT_BRAIN = null;
	public static final Item MAKESHIFT_HEAD = null;
	
	public static final Item ASSEMBLER_WORKSTATION = null;
	
	public static final Item TASK_SCROLL_SHELF = null;
	
	public static final Item PART_IRON_WIRE = null;
	public static final Item PART_SCREW = null;
	
	public static final Item RECIPE_MANUAL = null;
	
	public static final Item TASK_SCROLL = null;
	public static final Item LABEL = null;
	public static final Item SCHEDULE = null;
	
	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event) {
		event.getRegistry().registerAll(
				toolItem().setRegistryName(IndustrialWarfare.MOD_ID, "hammer"),
				toolItem().setRegistryName(IndustrialWarfare.MOD_ID, "wand"),
				new JobSitePointerItem(),
				new DebugOwnerItem(),
				new ComplaintRemoverItem(),
				new Item(new Item.Properties().tab(IWItemGroups.TAB_GENERAL)).setRegistryName(IndustrialWarfare.MOD_ID, "cured_flesh"),
				new Item(new Item.Properties().tab(IWItemGroups.TAB_GENERAL)).setRegistryName(IndustrialWarfare.MOD_ID, "body_part"),
				new Item(new Item.Properties().tab(IWItemGroups.TAB_GENERAL)).setRegistryName(IndustrialWarfare.MOD_ID, "makeshift_brain"),
				new Item(new Item.Properties().tab(IWItemGroups.TAB_GENERAL)).setRegistryName(IndustrialWarfare.MOD_ID, "makeshift_head"),
				blockItem(BlockInit.ASSEMBLER_WORKSTATION),
				blockItem(BlockInit.TASK_SCROLL_SHELF),
				new PartItem("iron_wire"),
				new PartItem("screw"),
				new RecipeItem(),
				new TaskScrollItem().setRegistryName(IndustrialWarfare.MOD_ID, "task_scroll"),
				new LabelItem(),
				new ScheduleItem()
		);
	}
	
	private static Item toolItem() {
		return new Item(new Item.Properties().stacksTo(1).tab(IWItemGroups.TAB_GENERAL));
	}
	
	private static Item blockItem(Block block) {
		return new BlockItem(block, new Item.Properties().tab(IWItemGroups.TAB_BLOCKS)).setRegistryName(block.getRegistryName());
	}
}
