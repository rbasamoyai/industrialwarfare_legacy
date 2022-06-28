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
		this.dropSelf(BlockInit.ASSEMBLER_WORKSTATION.get());
		this.dropSelf(BlockInit.TASK_SCROLL_SHELF.get());
		this.dropOther(BlockInit.MATCH_COIL.get(), Items.AIR);
		this.dropSelf(BlockInit.SPOOL.get());
		this.dropSelf(BlockInit.QUARRY.get());
		this.dropSelf(BlockInit.TREE_FARM.get());
		this.dropSelf(BlockInit.WORKER_SUPPORT.get());
	}
	
	@Override
	protected Iterable<Block> getKnownBlocks() {
		return StreamSupport
				.stream(ForgeRegistries.BLOCKS.spliterator(), false)
				.filter(entry -> entry.getRegistryName().getNamespace().equals(IndustrialWarfare.MOD_ID))
				.collect(Collectors.toSet());
	}
	
}
