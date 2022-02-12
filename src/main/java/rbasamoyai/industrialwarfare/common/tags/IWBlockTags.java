package rbasamoyai.industrialwarfare.common.tags;

import net.minecraft.block.Block;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import rbasamoyai.industrialwarfare.IndustrialWarfare;

public class IWBlockTags {

	public static final ITag.INamedTag<Block> SHATTERABLE = bind("shatterable");
	
	protected static ITag.INamedTag<Block> bind(String id) {
		return BlockTags.bind(IndustrialWarfare.MOD_ID + ":" + id);
	}
	
}
