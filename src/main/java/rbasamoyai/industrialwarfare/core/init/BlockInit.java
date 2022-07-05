package rbasamoyai.industrialwarfare.core.init;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.blocks.FarmingPlotBlock;
import rbasamoyai.industrialwarfare.common.blocks.LivestockPenBlock;
import rbasamoyai.industrialwarfare.common.blocks.ManufacturingBlock;
import rbasamoyai.industrialwarfare.common.blocks.MatchCoilBlock;
import rbasamoyai.industrialwarfare.common.blocks.OpaqueCutoutBlock;
import rbasamoyai.industrialwarfare.common.blocks.QuarryBlock;
import rbasamoyai.industrialwarfare.common.blocks.SpoolBlock;
import rbasamoyai.industrialwarfare.common.blocks.TaskScrollShelfBlock;
import rbasamoyai.industrialwarfare.common.blocks.TreeFarmBlock;

public class BlockInit {
	
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, IndustrialWarfare.MOD_ID);
	
	public static final RegistryObject<Block> ASSEMBLER_WORKSTATION = BLOCKS.register("assembler_workstation",
			() -> new ManufacturingBlock(ManufacturingBlock.WORKSTATION_WOOD, BlockEntityTypeInit.ASSEMBLER_WORKSTATION));
	
	public static final RegistryObject<Block> TASK_SCROLL_SHELF = BLOCKS.register("task_scroll_shelf", TaskScrollShelfBlock::new);
	public static final RegistryObject<Block> MATCH_COIL = BLOCKS.register("match_coil", MatchCoilBlock::new);
	public static final RegistryObject<Block> SPOOL = BLOCKS.register("spool", SpoolBlock::new);
	public static final RegistryObject<Block> QUARRY = BLOCKS.register("quarry", QuarryBlock::new);
	public static final RegistryObject<Block> WORKER_SUPPORT = BLOCKS.register("worker_support",
			() -> new OpaqueCutoutBlock(BlockBehaviour.Properties.of(Material.WOOD).sound(SoundType.WOOD).noOcclusion()));
	
	public static final RegistryObject<Block> TREE_FARM = BLOCKS.register("tree_farm", TreeFarmBlock::new);
	public static final RegistryObject<Block> FARMING_PLOT = BLOCKS.register("farming_plot", FarmingPlotBlock::new);
	public static final RegistryObject<Block> LIVESTOCK_PEN = BLOCKS.register("livestock_pen", LivestockPenBlock::new);
	
}
