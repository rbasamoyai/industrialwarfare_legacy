package rbasamoyai.industrialwarfare.common.tags;

import net.minecraft.block.Block;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import rbasamoyai.industrialwarfare.IndustrialWarfare;

public class IWBlockTags {

	public static final ITag.INamedTag<Block> SHATTERABLE = bind("shatterable");
	public static final ITag.INamedTag<Block> IGNORE_WHEN_MINING = bind("ignore_when_mining");
	public static final ITag.INamedTag<Block> CAN_PLANT_SAPLING = bind("can_plant_sapling");
	public static final ITag.INamedTag<Block> CAN_PLANT_FUNGUS = bind("can_plant_fungus");
	public static final ITag.INamedTag<Block> FORESTRY_HARVESTABLE = bind("forestry_harvestable");
	public static final ITag.INamedTag<Block> FUNGUS = bind("fungus");
	
	protected static ITag.INamedTag<Block> bind(String id) {
		return BlockTags.bind(IndustrialWarfare.MOD_ID + ":" + id);
	}
	
}
