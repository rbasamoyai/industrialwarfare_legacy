package rbasamoyai.industrialwarfare.core.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.ModTags;
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
			tag(BlockTags.MINEABLE_WITH_AXE)
					.add(BlockInit.ASSEMBLER_WORKSTATION.get())
					.add(BlockInit.QUARRY.get())
					.add(BlockInit.TREE_FARM.get())
					.add(BlockInit.MATCH_COIL.get())
					.add(BlockInit.SPOOL.get())
					.add(BlockInit.TASK_SCROLL_SHELF.get())
					.add(BlockInit.WORKER_SUPPORT.get());
			
			tag(ModTags.Blocks.SHATTERABLE)
					.addTag(BlockTags.FLOWER_POTS)
					.addTag(BlockTags.IMPERMEABLE)
					.addTag(BlockTags.ICE)
					.addTag(Tags.Blocks.GLASS)
					.addTag(Tags.Blocks.GLASS_PANES);
			
			tag(ModTags.Blocks.IGNORE_WHEN_MINING)
					.addTag(BlockTags.PLANKS)
					.addTag(BlockTags.STONE_BRICKS)
					.addTag(BlockTags.FENCES)
					.addTag(BlockTags.SLABS)
					.addTag(BlockTags.STAIRS)
					.add(BlockInit.WORKER_SUPPORT.get())
					.add(Blocks.BONE_BLOCK);
			
			tag(ModTags.Blocks.CAN_PLANT_SAPLING)
					.add(Blocks.DIRT)
					.add(Blocks.COARSE_DIRT)
					.add(Blocks.GRASS_BLOCK)
					.add(Blocks.PODZOL)
					.add(Blocks.FARMLAND);
			
			tag(ModTags.Blocks.CAN_PLANT_FUNGUS)
					.addTag(BlockTags.NYLIUM)
					.add(Blocks.MYCELIUM)
					.add(Blocks.SOUL_SOIL);
			
			tag(ModTags.Blocks.CAN_PLANT_FORESTRY)
					.addTag(ModTags.Blocks.CAN_PLANT_SAPLING)
					.addTag(ModTags.Blocks.CAN_PLANT_FUNGUS);
			
			tag(ModTags.Blocks.FORESTRY_HARVESTABLE)
					.addTag(BlockTags.LEAVES)
					.addTag(BlockTags.LOGS)
					.addTag(BlockTags.WART_BLOCKS)
					.add(Blocks.SHROOMLIGHT);
			
			tag(ModTags.Blocks.FUNGUS)
					.add(Blocks.CRIMSON_FUNGUS)
					.add(Blocks.WARPED_FUNGUS);
			
			tag(ModTags.Blocks.CLEARABLES)
					.addTag(BlockTags.FLOWERS)
					.addTag(BlockTags.REPLACEABLE_PLANTS);
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
			tag(ModTags.Items.CHEAT_AMMO)
					.add(ItemInit.INFINITE_AMMO_GENERIC.get(), ItemInit.INFINITE_PAPER_CARTRIDGE.get());
			
			tag(ModTags.Items.ENTRENCHING_TOOLS)
					.add(Items.WOODEN_SHOVEL, Items.STONE_SHOVEL, Items.IRON_SHOVEL, Items.GOLDEN_SHOVEL, Items.DIAMOND_SHOVEL, Items.NETHERITE_SHOVEL);
			
			tag(ModTags.Items.FIREARM_MELEE_ATTACHMENTS)
					.addTag(ModTags.Items.ENTRENCHING_TOOLS)
					.add(Items.WOODEN_SWORD, Items.STONE_SWORD, Items.IRON_SWORD, Items.GOLDEN_SWORD, Items.DIAMOND_SWORD, Items.NETHERITE_SWORD)
					.add(Items.WOODEN_AXE, Items.STONE_AXE, Items.IRON_AXE, Items.GOLDEN_AXE, Items.DIAMOND_AXE, Items.NETHERITE_AXE);
			
			tag(ModTags.Items.FIREARM_OPTIC_ATTACHMENTS);
			
			copy(ModTags.Blocks.FUNGUS, ModTags.Items.FUNGUS);	
		}
		
		@Override
		public String getName() {
			return "Industrial Warfare tags - Items";
		}
	}
	
}
