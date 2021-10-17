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

public class ItemModels extends ItemModelProvider {

	private static final Logger LOGGER = LogManager.getLogger();
	
	public ItemModels(DataGenerator generator, ExistingFileHelper existingFileHelper) {
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
	
		simpleBuilder("essence_armorer");
		simpleBuilder("essence_assembler");
		simpleBuilder("essence_bladesmith");
		simpleBuilder("essence_chemist");
		simpleBuilder("essence_commander");
		simpleBuilder("essence_founder");
		simpleBuilder("essence_gunsmith");
		simpleBuilder("essence_machinist");
		simpleBuilder("essence_medic");
		simpleBuilder("essence_metalsmith");
		simpleBuilder("essence_reanimator");
		simpleBuilder("essence_researcher");
		simpleBuilder("essence_transporter");
		simpleBuilder("essence_warrior");
		simpleBuilder("essence_weaponsmith");
		simpleBuilder("essence_woodworker");
		
		simpleBuilder("task_scroll");
		simpleBuilder("schedule");
		simpleBuilder("label");
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