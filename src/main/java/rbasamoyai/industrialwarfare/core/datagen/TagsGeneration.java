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
import rbasamoyai.industrialwarfare.core.init.BlockInit;
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
					.addTag(Tags.Blocks.GLASS_PANES);
			
			tag(IWBlockTags.IGNORE_WHEN_MINING)
					.addTag(BlockTags.PLANKS)
					.addTag(BlockTags.STONE_BRICKS)
					.addTag(BlockTags.FENCES)
					.addTag(BlockTags.SLABS)
					.addTag(BlockTags.STAIRS)
					.add(BlockInit.WORKER_SUPPORT.get())
					.add(Blocks.BONE_BLOCK);
			
			tag(IWBlockTags.CAN_PLANT_SAPLING)
					.add(Blocks.DIRT)
					.add(Blocks.COARSE_DIRT)
					.add(Blocks.GRASS_BLOCK)
					.add(Blocks.PODZOL)
					.add(Blocks.FARMLAND);
			
			tag(IWBlockTags.CAN_PLANT_FUNGUS)
					.addTag(BlockTags.NYLIUM)
					.add(Blocks.MYCELIUM)
					.add(Blocks.SOUL_SOIL);
			
			tag(IWBlockTags.CAN_PLANT_FORESTRY)
					.addTag(IWBlockTags.CAN_PLANT_SAPLING)
					.addTag(IWBlockTags.CAN_PLANT_FUNGUS);
			
			tag(IWBlockTags.FORESTRY_HARVESTABLE)
					.addTag(BlockTags.LEAVES)
					.addTag(BlockTags.LOGS)
					.addTag(BlockTags.WART_BLOCKS)
					.add(Blocks.SHROOMLIGHT);
			
			tag(IWBlockTags.FUNGUS)
					.add(Blocks.CRIMSON_FUNGUS)
					.add(Blocks.WARPED_FUNGUS);
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
			
			copy(IWBlockTags.FUNGUS, IWItemTags.FUNGUS);	
		}
		
		@Override
		public String getName() {
			return "Industrial Warfare tags - Items";
		}
	}
	
}
