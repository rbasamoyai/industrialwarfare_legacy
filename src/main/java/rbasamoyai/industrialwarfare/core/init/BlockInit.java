package rbasamoyai.industrialwarfare.core.init;

import net.minecraft.block.Block;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.blocks.MatchCoilBlock;
import rbasamoyai.industrialwarfare.common.blocks.NormalWorkstationBlock;
import rbasamoyai.industrialwarfare.common.blocks.SpoolBlock;
import rbasamoyai.industrialwarfare.common.blocks.TaskScrollShelfBlock;

public class BlockInit {
	
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Block.class, IndustrialWarfare.MOD_ID);
	
	public static final RegistryObject<Block> ASSEMBLER_WORKSTATION = BLOCKS.register("assembler_workstation", NormalWorkstationBlock::assemblerWorkstation);
	public static final RegistryObject<Block> TASK_SCROLL_SHELF = BLOCKS.register("task_scroll_shelf", TaskScrollShelfBlock::new);
	public static final RegistryObject<Block> MATCH_COIL = BLOCKS.register("match_coil", MatchCoilBlock::new);
	public static final RegistryObject<Block> SPOOL = BLOCKS.register("spool", SpoolBlock::new);
	
}
