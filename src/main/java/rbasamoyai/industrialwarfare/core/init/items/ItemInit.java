package rbasamoyai.industrialwarfare.core.init.items;

import net.minecraft.block.Block;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.items.IWArmorMaterial;
import rbasamoyai.industrialwarfare.common.items.InfiniteMatchCordItem;
import rbasamoyai.industrialwarfare.common.items.LabelItem;
import rbasamoyai.industrialwarfare.common.items.MatchCoilItem;
import rbasamoyai.industrialwarfare.common.items.MatchCordItem;
import rbasamoyai.industrialwarfare.common.items.PithHelmetItem;
import rbasamoyai.industrialwarfare.common.items.RecipeItem;
import rbasamoyai.industrialwarfare.common.items.ScheduleItem;
import rbasamoyai.industrialwarfare.common.items.WhistleItem;
import rbasamoyai.industrialwarfare.common.items.debugitems.ComplaintRemoverItem;
import rbasamoyai.industrialwarfare.common.items.debugitems.DebugOwnerItem;
import rbasamoyai.industrialwarfare.common.items.debugitems.JobSitePointerItem;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollItem;
import rbasamoyai.industrialwarfare.core.init.BlockInit;
import rbasamoyai.industrialwarfare.core.init.EntityTypeInit;
import rbasamoyai.industrialwarfare.core.itemgroup.IWItemGroups;

/*
 * Item initialization for rbasamoyai's Industrial Warfare.
 */

public class ItemInit {
	
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, IndustrialWarfare.MOD_ID);
	
	public static final RegistryObject<Item> HAMMER = ITEMS.register("hammer", ItemInit::toolItem);
	public static final RegistryObject<Item> WAND = ITEMS.register("wand", ItemInit::toolItem);
	public static final RegistryObject<Item> WHISTLE = ITEMS.register("whistle", WhistleItem::new);
	
	public static final RegistryObject<Item> JOB_SITE_POINTER = ITEMS.register("job_site_pointer", JobSitePointerItem::new);
	public static final RegistryObject<Item> COMPLAINT_REMOVER = ITEMS.register("complaint_remover", ComplaintRemoverItem::new);
	public static final RegistryObject<Item> DEBUG_OWNER = ITEMS.register("debug_owner", DebugOwnerItem::new);
	public static final RegistryObject<Item> NPC_SPAWN_EGG = ITEMS.register("npc_spawn_egg", () ->
			new ForgeSpawnEggItem(EntityTypeInit.NPC, 0x0000afaf, 0x00c69680, new Item.Properties().tab(IWItemGroups.TAB_DEBUG)));
	
	public static final RegistryObject<Item> CURED_FLESH = ITEMS.register("cured_flesh", ItemInit::generalGenericItem);
	public static final RegistryObject<Item> BODY_PART = ITEMS.register("body_part", ItemInit::generalGenericItem);
	public static final RegistryObject<Item> MAKESHIFT_BRAIN = ITEMS.register("makeshift_brain", ItemInit::generalGenericItem);
	public static final RegistryObject<Item> MAKESHIFT_HEAD = ITEMS.register("makeshift_head", ItemInit::generalGenericItem);
	
	public static final RegistryObject<Item> ASSEMBLER_WORKSTATION = registerBlockItem(BlockInit.ASSEMBLER_WORKSTATION);
	public static final RegistryObject<Item> TASK_SCROLL_SHELF = registerBlockItem(BlockInit.TASK_SCROLL_SHELF);
	public static final RegistryObject<Item> SPOOL = registerBlockItem(BlockInit.SPOOL);
	public static final RegistryObject<Item> QUARRY = registerBlockItem(BlockInit.QUARRY);
	public static final RegistryObject<Item> WORKER_SUPPORT = registerBlockItem(BlockInit.WORKER_SUPPORT);
	
	public static final RegistryObject<Item> RECIPE_MANUAL = ITEMS.register("recipe_manual", RecipeItem::new);
	
	public static final RegistryObject<Item> TASK_SCROLL = ITEMS.register("task_scroll", TaskScrollItem::new);
	public static final RegistryObject<Item> LABEL = ITEMS.register("label", LabelItem::new);
	public static final RegistryObject<Item> SCHEDULE = ITEMS.register("schedule", ScheduleItem::new);
	
	public static final RegistryObject<Item> AMMO_GENERIC = ITEMS.register("ammo_generic",
			() -> new Item(new Item.Properties().tab(IWItemGroups.TAB_GENERAL)));
	
	public static final RegistryObject<Item> INFINITE_AMMO_GENERIC = ITEMS.register("infinite_ammo_generic",
			() -> new Item(new Item.Properties().tab(IWItemGroups.TAB_GENERAL).rarity(Rarity.EPIC)) {
				@Override public boolean isFoil(ItemStack stack) { return true; }
			});
	
	public static final RegistryObject<Item> CARTRIDGE_CASE = ITEMS.register("cartridge_case",
			() -> new Item(new Item.Properties().tab(IWItemGroups.TAB_GENERAL)));
	
	public static final RegistryObject<Item> PAPER_CARTRIDGE = ITEMS.register("paper_cartridge",
			() -> new Item(new Item.Properties().tab(IWItemGroups.TAB_GENERAL)));
	
	public static final RegistryObject<Item> INFINITE_PAPER_CARTRIDGE = ITEMS.register("infinite_paper_cartridge",
			() -> new Item(new Item.Properties().tab(IWItemGroups.TAB_GENERAL).rarity(Rarity.EPIC)) {
				@Override public boolean isFoil(ItemStack stack) { return true; }
			});
	
	public static final RegistryObject<Item> MATCH_CORD = ITEMS.register("match_cord", MatchCordItem::new);
	public static final RegistryObject<Item> INFINITE_MATCH_CORD = ITEMS.register("infinite_match_cord", InfiniteMatchCordItem::new);
	public static final RegistryObject<Item> MATCH_COIL = ITEMS.register("match_coil", MatchCoilItem::new);
	
	public static final RegistryObject<Item> PITH_HELMET = ITEMS.register("pith_helmet",
			() -> new PithHelmetItem(IWArmorMaterial.WOOD, EquipmentSlotType.HEAD, new Item.Properties().tab(IWItemGroups.TAB_BLOCKS)));
	
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
