package rbasamoyai.industrialwarfare.core.datagen;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.data.DataGenerator;
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
				.predicate(modLoc("is_lit"), 1.0f)
				.model(new UncheckedModelFile(modLoc("item/match_cord_lit")))
				.end();
		
		simpleBuilder("match_cord_lit");
		
		simpleBuilder("infinite_match_cord", "match_cord")
		.override()
				.predicate(modLoc("is_lit"), 1.0f)
				.model(new UncheckedModelFile(modLoc("item/match_cord_lit")))
				.end();
		
		getBuilder("match_coil")
		.parent(new UncheckedModelFile(modLoc("block/match_coil0")))
		.override()
				.predicate(modLoc("coil_amount"), 1.0f)
				.model(new UncheckedModelFile(modLoc("block/match_coil1")))
				.end()
		.override()
				.predicate(modLoc("coil_amount"), 2.0f)
				.model(new UncheckedModelFile(modLoc("block/match_coil2")))
				.end()
		.override()
				.predicate(modLoc("coil_amount"), 3.0f)
				.model(new UncheckedModelFile(modLoc("block/match_coil3")))
				.end()
		.override()
				.predicate(modLoc("coil_amount"), 4.0f)
				.model(new UncheckedModelFile(modLoc("block/spool")))
				.end();
		
		simpleBuilder("pith_helmet");
		
		getBuilder("american_kepi")
		.parent(new UncheckedModelFile("item/generated"))
		.texture("layer0", modLoc("item/american_kepi"))
		.texture("layer1", modLoc("item/american_kepi_overlay"));
		
		getBuilder("pickelhaube_high")
		.parent(new UncheckedModelFile("item/generated"))
		.texture("layer0", modLoc("item/pickelhaube_high"))
		.texture("layer1", modLoc("item/pickelhaube_high_overlay"));
		
		getBuilder("dragoon_helmet_gold")
		.parent(new UncheckedModelFile("item/generated"))
		.texture("layer0", modLoc("item/dragoon_helmet"))
		.texture("layer1", modLoc("item/dragoon_helmet_overlay1"));
		
		getBuilder("dragoon_helmet_bronze")
		.parent(new UncheckedModelFile("item/generated"))
		.texture("layer0", modLoc("item/dragoon_helmet"))
		.texture("layer1", modLoc("item/dragoon_helmet_overlay1"));
		
		getBuilder("dragoon_helmet")
		.parent(new UncheckedModelFile(modLoc("item/dragoon_helmet_bronze")))
		.texture("layer0", modLoc("item/dragoon_helmet"))
		.texture("layer1", modLoc("item/dragoon_helmet_overlay"))
		.override()
				.predicate(mcLoc("custom_model_data"), 1.0f)
				.model(new UncheckedModelFile(modLoc("item/dragoon_helmet_gold")))
				.end();
		
		simpleBuilder("set_profession_jobless");
		simpleBuilder("set_profession_assembler");
		simpleBuilder("set_profession_quarrier");
		simpleBuilder("set_profession_logger");
		simpleBuilder("set_profession_farmer");
		
		simpleBuilder("surveyors_kit");
		
		LOGGER.debug("Finished generating item models for rbasamoyai's Industrial Warfare mod");
	}
	
	private ItemModelBuilder simpleBuilder(String id) {
		return simpleBuilder(id, id);
	}
	
	private ItemModelBuilder simpleBuilder(String itemId, String textureId) {
		return getBuilder(itemId)
				.parent(new UncheckedModelFile("item/generated"))
				.texture("layer0", modLoc("item/" + textureId));
	}
	
	@Override
	public String getName() {
		return "Industrial Warfare item models";
	}
}