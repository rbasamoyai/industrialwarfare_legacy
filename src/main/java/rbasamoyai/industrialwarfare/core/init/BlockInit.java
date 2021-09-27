package rbasamoyai.industrialwarfare.core.init;

import net.minecraft.block.Block;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.ObjectHolder;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.blocks.NormalWorkstationBlock;
import rbasamoyai.industrialwarfare.common.blocks.TaskScrollShelfBlock;

@Mod.EventBusSubscriber(modid = IndustrialWarfare.MOD_ID, bus = Bus.MOD)
@ObjectHolder(IndustrialWarfare.MOD_ID)
public class BlockInit {
	
	public static final Block ASSEMBLER_WORKSTATION = null;
	public static final Block TASK_SCROLL_SHELF = null;
	
	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event) {
		event.getRegistry().registerAll(new Block[] {
				NormalWorkstationBlock.assemblerWorkstation(),
				new TaskScrollShelfBlock()
		});
	}
}
