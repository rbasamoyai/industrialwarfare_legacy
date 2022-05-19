package rbasamoyai.industrialwarfare.core.datagen;

import net.minecraft.block.Blocks;
import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.ItemTagsProvider;
import net.minecraft.item.Items;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.tags.IWBlockTags;
import rbasamoyai.industrialwarfare.common.tags.IWItemTags;
import rbasamoyai.industrialwarfare.core.init.items.ItemInit;

public class TagsGeneration {
	
	public static void addAll(DataGenerator datagen, ExistingFileHelper fileHelper) {
		BlocksGen blocksGen = new BlocksGen(datagen, fileHelper);
		datagen.addProvider(blocksGen);
		datagen.addProvider(new ItemsGen(datagen, blocksGen, fileHelper));
	}

	public static class BlocksGen extends BlockTagsProvider {		
		public BlocksGen(DataGenerator datagen, ExistingFileHelper fileHelper) {
			super(datagen, IndustrialWarfare.MOD_ID, fileHelper);
		}
		
		@Override
		protected void addTags() {
			tag(IWBlockTags.SHATTERABLE)
					.addTag(BlockTags.FLOWER_POTS)
					.addTag(BlockTags.IMPERMEABLE)
					.addTag(BlockTags.ICE)
					.addTag(Tags.Blocks.GLASS)
					.addTag(Tags.Blocks.GLASS_WHITE)
					.addTag(Tags.Blocks.GLASS_ORANGE)
					.addTag(Tags.Blocks.GLASS_MAGENTA)
					.addTag(Tags.Blocks.GLASS_LIGHT_BLUE)
					.addTag(Tags.Blocks.GLASS_YELLOW)
					.addTag(Tags.Blocks.GLASS_PINK)
					.addTag(Tags.Blocks.GLASS_GRAY)
					.addTag(Tags.Blocks.GLASS_LIGHT_GRAY)
					.addTag(Tags.Blocks.GLASS_COLORLESS)
					.addTag(Tags.Blocks.GLASS_CYAN)
					.addTag(Tags.Blocks.GLASS_PURPLE)
					.addTag(Tags.Blocks.GLASS_BLUE)
					.addTag(Tags.Blocks.GLASS_BROWN)
					.addTag(Tags.Blocks.GLASS_GREEN)
					.addTag(Tags.Blocks.GLASS_RED)
					.addTag(Tags.Blocks.GLASS_BLACK)
					.addTag(Tags.Blocks.GLASS_PANES)
					.addTag(Tags.Blocks.GLASS_PANES_WHITE)
					.addTag(Tags.Blocks.GLASS_PANES_ORANGE)
					.addTag(Tags.Blocks.GLASS_PANES_MAGENTA)
					.addTag(Tags.Blocks.GLASS_PANES_LIGHT_BLUE)
					.addTag(Tags.Blocks.GLASS_PANES_YELLOW)
					.addTag(Tags.Blocks.GLASS_PANES_PINK)
					.addTag(Tags.Blocks.GLASS_PANES_GRAY)
					.addTag(Tags.Blocks.GLASS_PANES_LIGHT_GRAY)
					.addTag(Tags.Blocks.GLASS_PANES_COLORLESS)
					.addTag(Tags.Blocks.GLASS_PANES_CYAN)
					.addTag(Tags.Blocks.GLASS_PANES_PURPLE)
					.addTag(Tags.Blocks.GLASS_PANES_BLUE)
					.addTag(Tags.Blocks.GLASS_PANES_BROWN)
					.addTag(Tags.Blocks.GLASS_PANES_GREEN)
					.addTag(Tags.Blocks.GLASS_PANES_RED)
					.addTag(Tags.Blocks.GLASS_PANES_BLACK)
					.add(
							Blocks.GLASS_PANE,
							Blocks.WHITE_STAINED_GLASS_PANE,
							Blocks.ORANGE_STAINED_GLASS_PANE,
							Blocks.MAGENTA_STAINED_GLASS_PANE,
							Blocks.LIGHT_BLUE_STAINED_GLASS_PANE,
							Blocks.YELLOW_STAINED_GLASS_PANE,
							Blocks.PINK_STAINED_GLASS_PANE,
							Blocks.GRAY_STAINED_GLASS_PANE,
							Blocks.LIGHT_GRAY_STAINED_GLASS_PANE,
							Blocks.LIME_STAINED_GLASS_PANE,
							Blocks.CYAN_STAINED_GLASS_PANE,
							Blocks.PURPLE_STAINED_GLASS_PANE,
							Blocks.BLUE_STAINED_GLASS_PANE,
							Blocks.BROWN_STAINED_GLASS_PANE,
							Blocks.GREEN_STAINED_GLASS_PANE,
							Blocks.RED_STAINED_GLASS_PANE,
							Blocks.BLACK_STAINED_GLASS_PANE);
		}
		
		@Override
		public String getName() {
			return "Industrial Warfare tags - Blocks";
		}
	}
	
	public static class ItemsGen extends ItemTagsProvider {
		public ItemsGen(DataGenerator datagen, BlockTagsProvider blockTagsProvider, ExistingFileHelper fileHelper) {
			super(datagen, blockTagsProvider, IndustrialWarfare.MOD_ID, fileHelper);
		}
		
		@Override
		protected void addTags() {
			tag(IWItemTags.CHEAT_AMMO)
					.add(ItemInit.INFINITE_AMMO_GENERIC.get(), ItemInit.INFINITE_PAPER_CARTRIDGE.get());
			
			tag(IWItemTags.ENTRENCHING_TOOLS)
					.add(Items.WOODEN_SHOVEL, Items.STONE_SHOVEL, Items.IRON_SHOVEL, Items.GOLDEN_SHOVEL, Items.DIAMOND_SHOVEL, Items.NETHERITE_SHOVEL);
			
			tag(IWItemTags.FIREARM_MELEE_ATTACHMENTS)
					.addTag(IWItemTags.ENTRENCHING_TOOLS)
					.add(Items.WOODEN_SWORD, Items.STONE_SWORD, Items.IRON_SWORD, Items.GOLDEN_SWORD, Items.DIAMOND_SWORD, Items.NETHERITE_SWORD)
					.add(Items.WOODEN_AXE, Items.STONE_AXE, Items.IRON_AXE, Items.GOLDEN_AXE, Items.DIAMOND_AXE, Items.NETHERITE_AXE);
			
			tag(IWItemTags.FIREARM_OPTIC_ATTACHMENTS);
		}
		
		@Override
		public String getName() {
			return "Industrial Warfare tags - Items";
		}
	}
	
}
