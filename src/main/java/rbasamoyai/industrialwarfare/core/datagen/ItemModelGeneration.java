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
		
		simpleBuilder("ammo_generic", "ammo_large_metal");
		
		simpleBuilder("part_bullet");
		
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