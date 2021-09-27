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
		simpleBuilder("ammo_111");
		simpleBuilder("ammo_112");
		simpleBuilder("ammo_113");
		simpleBuilder("ammo_121");
		simpleBuilder("ammo_122");
		simpleBuilder("ammo_123");
		simpleBuilder("ammo_131");
		simpleBuilder("ammo_132");
		simpleBuilder("ammo_133");
		
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
		simpleBuilder("label");
		
		// Smokeless ammo uses same texture as metal casing ammo, so simpleBuilder() is not applicable here
		getBuilder("ammo_231")
			.parent(new UncheckedModelFile("item/generated"))
			.texture("layer0", new ResourceLocation(IndustrialWarfare.MOD_ID, "item/ammo_131"));
		
		getBuilder("ammo_232")
			.parent(new UncheckedModelFile("item/generated"))
			.texture("layer0", new ResourceLocation(IndustrialWarfare.MOD_ID, "item/ammo_132"));
		
		getBuilder("ammo_233")
			.parent(new UncheckedModelFile("item/generated"))
			.texture("layer0", new ResourceLocation(IndustrialWarfare.MOD_ID, "item/ammo_133"));
	}
	
	// """Macro""" time
	private ItemModelBuilder simpleBuilder(String id) {
		return getBuilder(id)
				.parent(new UncheckedModelFile("item/generated"))
				.texture("layer0", new ResourceLocation(IndustrialWarfare.MOD_ID, "item/" + id));
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