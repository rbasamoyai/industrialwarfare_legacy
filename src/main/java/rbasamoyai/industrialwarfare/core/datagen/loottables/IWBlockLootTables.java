package rbasamoyai.industrialwarfare.core.datagen.loottables;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import net.minecraft.block.Block;
import net.minecraft.data.loot.BlockLootTables;
import net.minecraft.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.core.init.BlockInit;

public class IWBlockLootTables extends BlockLootTables {

	@Override
	protected void addTables() {
		this.dropSelf(BlockInit.ASSEMBLER_WORKSTATION.get());
		this.dropSelf(BlockInit.TASK_SCROLL_SHELF.get());
		this.dropOther(BlockInit.MATCH_COIL.get(), Items.AIR);
		this.dropSelf(BlockInit.SPOOL.get());
	}
	
	@Override
	protected Iterable<Block> getKnownBlocks() {
		return StreamSupport
				.stream(ForgeRegistries.BLOCKS.spliterator(), false)
				.filter(entry -> entry.getRegistryName().getNamespace().equals(IndustrialWarfare.MOD_ID))
				.collect(Collectors.toSet());
	}
	
}
