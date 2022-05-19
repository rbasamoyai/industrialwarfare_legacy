package rbasamoyai.industrialwarfare.core.datagen;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile.UncheckedModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import rbasamoyai.industrialwarfare.IndustrialWarfare;

public class ItemModelGeneration extends ItemModelProvider {

	private static final Logger LOGGER = LogManager.getLogger();
	
	public ItemModelGeneration(DataGenerator generator, ExistingFileHelper existingFileHelper) {
		super(generator, IndustrialWarfare.MOD_ID, existingFileHelper);
	}
	
	@Override
	protected void registerModels() {
		LOGGER.debug("Generating item models for rbasamoyai's Industrial Warfare mod");
		simpleBuilder("job_site_pointer", "debug_tool");
		simpleBuilder("complaint_remover", "debug_tool");
		simpleBuilder("debug_owner", "debug_tool");
		
		simpleBuilder("body_part");
		simpleBuilder("cured_flesh");
		simpleBuilder("makeshift_brain");
		getBuilder("npc_spawn_egg").parent(new UncheckedModelFile("item/template_spawn_egg"));
		
		simpleBuilder("task_scroll");
		simpleBuilder("schedule");
		simpleBuilder("label");
		
		simpleBuilder("recipe_manual");
		
		simpleBuilder("cartridge_case", "ammo_large_metal_casing");
		simpleBuilder("ammo_generic", "ammo_large_metal");
		simpleBuilder("infinite_ammo_generic", "ammo_large_metal");
		simpleBuilder("paper_cartridge", "paper_cartridge_musket");
		simpleBuilder("infinite_paper_cartridge", "paper_cartridge_musket");
		
		simpleBuilder("part_bullet");
		
		simpleBuilder("whistle");
		
		simpleBuilder("match_cord")
		.override()
				.predicate(new ResourceLocation(IndustrialWarfare.MOD_ID, "is_lit"), 1.0f)
				.model(new UncheckedModelFile(new ResourceLocation(IndustrialWarfare.MOD_ID, "item/match_cord_lit")))
				.end();
		
		simpleBuilder("match_cord_lit");
		
		simpleBuilder("infinite_match_cord", "match_cord")
		.override()
				.predicate(new ResourceLocation(IndustrialWarfare.MOD_ID, "is_lit"), 1.0f)
				.model(new UncheckedModelFile(new ResourceLocation(IndustrialWarfare.MOD_ID, "item/match_cord_lit")))
				.end();
		
		getBuilder("match_coil")
		.parent(new UncheckedModelFile(new ResourceLocation(IndustrialWarfare.MOD_ID, "block/match_coil0")))
		.override()
				.predicate(new ResourceLocation(IndustrialWarfare.MOD_ID, "coil_amount"), 1.0f)
				.model(new UncheckedModelFile(new ResourceLocation(IndustrialWarfare.MOD_ID, "block/match_coil1")))
				.end()
		.override()
				.predicate(new ResourceLocation(IndustrialWarfare.MOD_ID, "coil_amount"), 2.0f)
				.model(new UncheckedModelFile(new ResourceLocation(IndustrialWarfare.MOD_ID, "block/match_coil2")))
				.end()
		.override()
				.predicate(new ResourceLocation(IndustrialWarfare.MOD_ID, "coil_amount"), 3.0f)
				.model(new UncheckedModelFile(new ResourceLocation(IndustrialWarfare.MOD_ID, "block/match_coil3")))
				.end()
		.override()
				.predicate(new ResourceLocation(IndustrialWarfare.MOD_ID, "coil_amount"), 4.0f)
				.model(new UncheckedModelFile(new ResourceLocation(IndustrialWarfare.MOD_ID, "block/spool")))
				.end();
		
		LOGGER.debug("Finished generating item models for rbasamoyai's Industrial Warfare mod");
	}
	
	// """Macro""" time
	private ItemModelBuilder simpleBuilder(String id) {
		return simpleBuilder(id, id);
	}
	
	private ItemModelBuilder simpleBuilder(String itemId, String textureId) {
		return getBuilder(itemId)
				.parent(new UncheckedModelFile("item/generated"))
				.texture("layer0", new ResourceLocation(IndustrialWarfare.MOD_ID, "item/" + textureId));
	}
	
	@Override
	public void run(DirectoryCache cache) throws IOException {
		super.run(cache);	
	}
	
	@Override
	public String getName() {
		return "Industrial Warfare item models";
	}
}