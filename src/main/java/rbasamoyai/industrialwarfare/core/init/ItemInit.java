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
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollItem;
import rbasamoyai.industrialwarfare.core.itemgroup.IWItemGroups;

/*
 * Item initialization for rbasamoyai's Industrial Warfare.
 */

@Mod.EventBusSubscriber(modid = IndustrialWarfare.MOD_ID, bus = Bus.MOD)
@ObjectHolder(IndustrialWarfare.MOD_ID)
public class ItemInit {

	public static final Item AMMO_111 = null;
	public static final Item AMMO_112 = null;
	public static final Item AMMO_113 = null;
	public static final Item AMMO_121 = null;
	public static final Item AMMO_122 = null;
	public static final Item AMMO_123 = null;
	public static final Item AMMO_131 = null;
	public static final Item AMMO_132 = null;
	public static final Item AMMO_133 = null;
	public static final Item AMMO_231 = null;
	public static final Item AMMO_232 = null;
	public static final Item AMMO_233 = null;
	public static final Item HAMMER = null;
	public static final Item WAND = null;
	public static final Item CURED_FLESH = null;
	public static final Item BODY_PART = null;
	public static final Item MAKESHIFT_BRAIN = null;
	public static final Item MAKESHIFT_HEAD = null;
	public static final Item ESSENCE_ARMORER = null;
	public static final Item ESSENCE_ASSEMBLER = null;
	public static final Item ESSENCE_BLADESMITH = null;
	public static final Item ESSENCE_CHEMIST = null;
	public static final Item ESSENCE_COMMANDER = null;
	public static final Item ESSENCE_FOUNDER = null;
	public static final Item ESSENCE_GUNSMITH = null;
	public static final Item ESSENCE_MACHINIST = null;
	public static final Item ESSENCE_MEDIC = null;
	public static final Item ESSENCE_METALSMITH = null;
	public static final Item ESSENCE_REANIMATOR = null;
	public static final Item ESSENCE_RESEARCHER = null;
	public static final Item ESSENCE_TRANSPORTER = null;
	public static final Item ESSENCE_WARRIOR = null;
	public static final Item ESSENCE_WEAPONSMITH = null;
	public static final Item ESSENCE_WOODWORKER = null;
	
	public static final Item ASSEMBLER_WORKSTATION = null;
	
	public static final Item TASK_SCROLL_SHELF = null;
	
	public static final Item PART_IRON_WIRE = null;
	public static final Item PART_SCREW = null;
	
	public static final Item RECIPE_MANUAL = null;
	
	public static final Item TASK_SCROLL = null;
	
	public static final Item LABEL = null;
	
	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event) {
		event.getRegistry().registerAll(new Item[] {
				ammoItem("111"),
				ammoItem("112"),
				ammoItem("113"), 
				ammoItem("121"), 
				ammoItem("122"), 
				ammoItem("123"), 
				ammoItem("131"), 
				ammoItem("132"), 
				ammoItem("133"), 
				ammoItem("231"), 
				ammoItem("232"), 
				ammoItem("233"),
				new Item(new Item.Properties().stacksTo(1).tab(IWItemGroups.TAB_GENERAL)).setRegistryName(IndustrialWarfare.MOD_ID, "hammer"),
				new Item(new Item.Properties().stacksTo(1).tab(IWItemGroups.TAB_GENERAL)).setRegistryName(IndustrialWarfare.MOD_ID, "wand"),
				new Item(new Item.Properties().tab(IWItemGroups.TAB_GENERAL)).setRegistryName(IndustrialWarfare.MOD_ID, "cured_flesh"),
				new Item(new Item.Properties().tab(IWItemGroups.TAB_GENERAL)).setRegistryName(IndustrialWarfare.MOD_ID, "body_part"),
				new Item(new Item.Properties().tab(IWItemGroups.TAB_GENERAL)).setRegistryName(IndustrialWarfare.MOD_ID, "makeshift_brain"),
				new Item(new Item.Properties().tab(IWItemGroups.TAB_GENERAL)).setRegistryName(IndustrialWarfare.MOD_ID, "makeshift_head"),
				essenceItem("armorer"),
				essenceItem("assembler"),
				essenceItem("bladesmith"),
				essenceItem("chemist"),
				essenceItem("commander"),
				essenceItem("founder"),
				essenceItem("gunsmith"),
				essenceItem("machinist"),
				essenceItem("medic"),
				essenceItem("metalsmith"),
				essenceItem("reanimator"),
				essenceItem("researcher"),
				essenceItem("transporter"),
				essenceItem("warrior"),
				essenceItem("weaponsmith"),
				essenceItem("woodworker"),
				blockItem(BlockInit.ASSEMBLER_WORKSTATION),
				blockItem(BlockInit.TASK_SCROLL_SHELF),
				new PartItem("iron_wire"),
				new PartItem("screw"),
				new RecipeItem(),
				new TaskScrollItem().setRegistryName(IndustrialWarfare.MOD_ID, "task_scroll"),
				new LabelItem(),
				new ScheduleItem()
		});
	}
	
	// Facilitators for simple/"repetitive" items
	private static Item ammoItem(String id) {
		return new Item(new Item.Properties().tab(IWItemGroups.TAB_GENERAL)).setRegistryName(IndustrialWarfare.MOD_ID, "ammo_" + id);
	}
	
	private static Item essenceItem(String id) {
		return new Item(new Item.Properties().tab(IWItemGroups.TAB_GENERAL)).setRegistryName(IndustrialWarfare.MOD_ID, "essence_" + id);
	}
	
	private static Item blockItem(Block block) {
		return new BlockItem(block, new Item.Properties().tab(IWItemGroups.TAB_BLOCKS)).setRegistryName(block.getRegistryName());
	}
}
