package rbasamoyai.industrialwarfare.core.datagen.loottables;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.datafixers.util.Pair;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.data.LootTableProvider;
import net.minecraft.loot.LootParameterSet;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTable.Builder;
import net.minecraft.loot.LootTableManager;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.ValidationTracker;
import net.minecraft.util.ResourceLocation;
import rbasamoyai.industrialwarfare.IndustrialWarfare;

/*
 * Loot table provider for data generation.
 * 
 * Can't be bothered to really handwrite the wheel again - I just want to generate loot tables damnit!
 * Credit is given where credit is due.
 */

public class IWLootTableProvider extends LootTableProvider {

	private static final Logger LOGGER = LogManager.getLogger();
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	
	protected final List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootParameterSet>> lootTables = ImmutableList.of(
			Pair.of(IWBlockLootTables::new, LootParameterSets.BLOCK)
			);
	
	private final DataGenerator generator;
	
	public IWLootTableProvider(DataGenerator datagen) {
		super(datagen);
		this.generator = datagen;
	}
	
	@Override
	// "Readable" copy of LootTableProvider#run, as well as some stuff put in other methods, like writeTables.
	public void run(DirectoryCache cache) {
		Map<ResourceLocation, LootTable> map = new HashMap<>();
		this.lootTables.forEach(pair -> {
			pair.getFirst().get().accept((id, builder) -> {
				if (map.put(id, builder.setParamSet(pair.getSecond()).build()) != null)
					throw new IllegalStateException("Duplicate loot table " + id.toString());
			});
		});
		ValidationTracker validationTracker = new ValidationTracker(LootParameterSets.ALL_PARAMS, (a) -> { return null; }, map::get);
		validate(map, validationTracker);
		
		Multimap<String, String> multimap = validationTracker.getProblems();
		if (!multimap.isEmpty()) {
			multimap.forEach((a, b) -> {
				LOGGER.warn("Found validation problem in " + a + ":" + b);
			});
			throw new IllegalStateException("Failed to validate loot tables, see logs");
		} else writeTables(cache, map);
	}
	
	// Taken from https://forums.minecraftforge.net/topic/96569-1165-loot-tables/?do=findComment&comment=439795
	@Override
	protected void validate(Map<ResourceLocation, LootTable> map, ValidationTracker validationtracker) {
		final Set<ResourceLocation> modLootTableIds =
				LootTables
					.all()
					.stream()
					.filter(table -> table.getNamespace().equals(IndustrialWarfare.MOD_ID))
					.collect(Collectors.toSet());
		
		for (ResourceLocation id : Sets.difference(modLootTableIds, map.keySet()))
			validationtracker.reportProblem("Missing mod loot table: " + id.toString());
		
		map.forEach((id, lootTable) -> LootTableManager.validate(validationtracker, id, lootTable));
	}
	
	// Pretty much taken verbatim from https://forge.gemwire.uk/wiki/Datageneration/Loot_Tables/1.16
	private void writeTables(DirectoryCache cache, Map<ResourceLocation, LootTable> tables) {
		Path outputFolder = this.generator.getOutputFolder();
		tables.forEach((key, lootTable) -> {
			Path path = outputFolder.resolve("data/" + key.getNamespace() + "/loot_tables/" + key.getPath() + ".json");
			try {
				IDataProvider.save(GSON, cache, LootTableManager.serialize(lootTable), path);
			} catch (IOException e) {
				LOGGER.error("Couldn't write loot table {}", path, (Object) e);
			}
		});
	}
	
	@Override
	protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, Builder>>>, LootParameterSet>> getTables() {
		return this.lootTables;
	}
	
	@Override
	public String getName() {
		return IndustrialWarfare.MOD_ID + "_lootTables";
	}

}
