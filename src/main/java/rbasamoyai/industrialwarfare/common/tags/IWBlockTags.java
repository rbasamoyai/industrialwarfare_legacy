package rbasamoyai.industrialwarfare.common.tags;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import rbasamoyai.industrialwarfare.IndustrialWarfare;

public class IWBlockTags {

	public static final TagKey<Block> SHATTERABLE = bind("shatterable");
	public static final TagKey<Block> IGNORE_WHEN_MINING = bind("ignore_when_mining");
	public static final TagKey<Block> CAN_PLANT_SAPLING = bind("can_plant_sapling");
	public static final TagKey<Block> CAN_PLANT_FUNGUS = bind("can_plant_fungus");
	public static final TagKey<Block> CAN_PLANT_FORESTRY = bind("can_plant_forestry");
	public static final TagKey<Block> FORESTRY_HARVESTABLE = bind("forestry_harvestable");
	public static final TagKey<Block> FUNGUS = bind("fungus");
	
	protected static TagKey<Block> bind(String id) {
		return BlockTags.create(new ResourceLocation(IndustrialWarfare.MOD_ID, id));
	}
	
}
