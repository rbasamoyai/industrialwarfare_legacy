package rbasamoyai.industrialwarfare.core.init;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.blocks.MatchCoilBlock;
import rbasamoyai.industrialwarfare.common.blocks.NormalWorkstationBlock;
import rbasamoyai.industrialwarfare.common.blocks.OpaqueCutoutBlock;
import rbasamoyai.industrialwarfare.common.blocks.QuarryBlock;
import rbasamoyai.industrialwarfare.common.blocks.SpoolBlock;
import rbasamoyai.industrialwarfare.common.blocks.TaskScrollShelfBlock;
import rbasamoyai.industrialwarfare.common.blocks.TreeFarmBlock;

public class BlockInit {
	
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Block.class, IndustrialWarfare.MOD_ID);
	
	public static final RegistryObject<Block> ASSEMBLER_WORKSTATION = BLOCKS.register("assembler_workstation", NormalWorkstationBlock::assemblerWorkstation);
	public static final RegistryObject<Block> TASK_SCROLL_SHELF = BLOCKS.register("task_scroll_shelf", TaskScrollShelfBlock::new);
	public static final RegistryObject<Block> MATCH_COIL = BLOCKS.register("match_coil", MatchCoilBlock::new);
	public static final RegistryObject<Block> SPOOL = BLOCKS.register("spool", SpoolBlock::new);
	public static final RegistryObject<Block> QUARRY = BLOCKS.register("quarry", QuarryBlock::new);
	public static final RegistryObject<Block> WORKER_SUPPORT = BLOCKS.register("worker_support", () -> new OpaqueCutoutBlock(AbstractBlock.Properties.of(Material.WOOD).sound(SoundType.WOOD).noOcclusion()));
	public static final RegistryObject<Block> TREE_FARM = BLOCKS.register("tree_farm", TreeFarmBlock::new);
	
}
