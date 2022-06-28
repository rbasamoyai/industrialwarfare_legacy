package rbasamoyai.industrialwarfare.core.datagen.loottables;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import rbasamoyai.industrialwarfare.IndustrialWarfare;

public class IWLootTableProvider extends LootTableProvider {
	
	protected final List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> lootTables = ImmutableList.of(
			Pair.of(IWBlockLootTables::new, LootContextParamSets.BLOCK)
			);
	
	public IWLootTableProvider(DataGenerator datagen) {
		super(datagen);
	}
	
	@Override
	protected void validate(Map<ResourceLocation, LootTable> map, ValidationContext validationtracker) {
		map.forEach((id, lootTable) -> LootTables.validate(validationtracker, id, lootTable));
	}
	
	@Override
	protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> getTables() {
		return this.lootTables;
	}
	
	@Override
	public String getName() {
		return IndustrialWarfare.MOD_ID + "_lootTables";
	}

}
