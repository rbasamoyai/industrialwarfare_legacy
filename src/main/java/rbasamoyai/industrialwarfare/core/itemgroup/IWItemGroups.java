package rbasamoyai.industrialwarfare.core.itemgroup;

import java.util.Arrays;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.NonNullList;
import rbasamoyai.industrialwarfare.common.items.PartItem;
import rbasamoyai.industrialwarfare.common.items.QualityItem;
import rbasamoyai.industrialwarfare.common.items.RecipeItem;
import rbasamoyai.industrialwarfare.core.init.ItemInit;

/**
 * Item groups for rbasamoyai's Industrial Warfare mod.
 * <p></p>
 * The item groups and their contents are described below.
 * <p></p>
 * {@code TAB_GENERAL} - General items
 * <ul>
 * 		General items, mostly consists of regular crafting items, some tools such as the Hammer and the Wand of Reanimation,
 * 		NPC Essence items, etc.
 * </ul>
 * {@code TAB_BLOCKS} - Block items
 * <ul>
 * 		Contains block items such as workstations.
 * </ul>
 * {@code TAB_PARTS} - Part items
 * <ul>
 * 		Contains part items, which are used in the making of weapons, armor, and other things that this mod centers around.
 * </ul>
 * {@code TAB_RECIPES} - Recipe manual items
 * <ul>
 * 		Contains recipe manuals of various items. There is only one recipe manual item (industrialwarfare:recipe_manual),
 * 		but it can store the ID of an item. This makes it easier to manage the recipes.
 * </ul>
 * {@code TAB_WEAPONS} - Weapon items
 * <ul>
 * 		Contains weapon items, melee, ranged, etc.
 * </ul>
 * {@code TAB_ARMOR} - Armor items - <b>TODO: NOT IMPLEMENTED</b>
 * <ul>
 * 		Contains armor items.
 * </ul>
 * {@code TAB_EQUIPMENT} - Equipment items - <b>TODO: NOT IMPLEMENTED</b>
 * <ul>
 * 		Contains equipment items, such as shields, quivers, pouches, etc.
 * </ul>
 * <hr></hr>
 * Some groups' {@code ItemGroup#fillItemList} method does not call the superclass method and instead clears the "items" argument,
 * then adds some new items to said argument. This is for adding items with NBT tags/capabilities attached. In effect, this renders the
 * {@code Item.Properties#tab} method used in initialization unused, but the code will be kept for documentation and readability
 * purposes.
 */

public class IWItemGroups {
	
	private static int groupId = ItemGroup.getGroupCountSafe();
	
	public static final ItemGroup TAB_GENERAL = new ItemGroup(groupId++, "industrialwarfare.general") {
		@Override
		public void fillItemList(NonNullList<ItemStack> items) {
			items.addAll(Arrays.asList(
					getItemStack(ItemInit.HAMMER),
					getItemStack(ItemInit.WAND),
					getItemStack(ItemInit.CURED_FLESH),
					getItemStack(ItemInit.BODY_PART),
					getItemStack(ItemInit.MAKESHIFT_BRAIN),
					getItemStack(ItemInit.MAKESHIFT_HEAD)
					));
		}
		
		@Override
		public ItemStack makeIcon() {
			return getItemStack(ItemInit.HAMMER);
		}	
	};
	
	public static final ItemGroup TAB_DEBUG = new ItemGroup(groupId++, "industrialwarfare.debug") {
		public void fillItemList(NonNullList<ItemStack> items) {
			items.addAll(Arrays.asList(
					getItemStack(ItemInit.JOB_SITE_POINTER),
					getItemStack(ItemInit.COMPLAINT_REMOVER),
					getItemStack(ItemInit.DEBUG_OWNER)
					));
		}	
		
		@Override
		public ItemStack makeIcon() {
			return getItemStack(ItemInit.JOB_SITE_POINTER);
		}
	};

	public static final ItemGroup TAB_BLOCKS = new ItemGroup(groupId++, "industrialwarfare.blocks") {
		@Override
		public void fillItemList(NonNullList<ItemStack> items) {
			items.addAll(Arrays.asList(
					getItemStack(ItemInit.ASSEMBLER_WORKSTATION),
					getItemStack(ItemInit.TASK_SCROLL_SHELF)
					));
		}
		
		@Override
		public ItemStack makeIcon() {
			return getItemStack(ItemInit.ASSEMBLER_WORKSTATION);
		}
	};
	
	public static final ItemGroup TAB_PARTS = new ItemGroup(groupId++, "industrialwarfare.parts") {		
		@Override
		public void fillItemList(NonNullList<ItemStack> items) {
			items.addAll(Arrays.asList(
					PartItem.setQualityValues(getItemStack(ItemInit.PART_IRON_WIRE), 1.0f, 1, 1),
					PartItem.setQualityValues(getItemStack(ItemInit.PART_SCREW), 1.0f, 1, 1)
					));
		}
			
		@Override
		public ItemStack makeIcon() {
			return getItemStack(ItemInit.PART_SCREW);
		}		
	};
	
	public static final ItemGroup TAB_RECIPES = new ItemGroup(groupId++, "industrialwarfare.recipes") {	
		@Override
		public void fillItemList(NonNullList<ItemStack> items) {
			items.addAll(Arrays.asList(
					QualityItem.setQualityValues(RecipeItem.getRecipeManualOf(ItemInit.PART_IRON_WIRE), 1.0f),
					QualityItem.setQualityValues(RecipeItem.getRecipeManualOf(ItemInit.PART_SCREW), 1.0f)
					));
		}
		
		@Override
		public ItemStack makeIcon() {
			return getItemStack(ItemInit.RECIPE_MANUAL);
		}	
	};
	
	public static final ItemGroup TAB_WEAPONS = new ItemGroup(groupId++, "industrialwarfare.weapons") {	
		@Override
		public void fillItemList(NonNullList<ItemStack> items) {
			items.addAll(Arrays.asList(
					
					));
		}
		
		@Override
		public ItemStack makeIcon() {
			return getItemStack(Items.IRON_SWORD);
		}	
	};
	
	private static ItemStack getItemStack(Item item) {
		return new ItemStack(item);
	}
	
}
