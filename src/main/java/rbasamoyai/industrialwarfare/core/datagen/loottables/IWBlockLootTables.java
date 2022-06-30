package rbasamoyai.industrialwarfare.core.datagen.loottables;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import net.minecraft.data.loot.BlockLoot;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.core.init.BlockInit;

public class IWBlockLootTables extends BlockLoot {

	@Override
	protected void addTables() {
		dropSelf(BlockInit.ASSEMBLER_WORKSTATION.get());
		dropSelf(BlockInit.TASK_SCROLL_SHELF.get());
		dropOther(BlockInit.MATCH_COIL.get(), Items.AIR);
		dropSelf(BlockInit.SPOOL.get());
		dropSelf(BlockInit.QUARRY.get());
		dropSelf(BlockInit.TREE_FARM.get());
		dropSelf(BlockInit.WORKER_SUPPORT.get());
		dropSelf(BlockInit.FARMING_PLOT.get());
	}
	
	@Override
	protected Iterable<Block> getKnownBlocks() {
		return StreamSupport
				.stream(ForgeRegistries.BLOCKS.spliterator(), false)
				.filter(entry -> entry.getRegistryName().getNamespace().equals(IndustrialWarfare.MOD_ID))
				.collect(Collectors.toSet());
	}
	
}
