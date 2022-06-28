package rbasamoyai.industrialwarfare.core.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.core.datagen.loottables.IWLootTableProvider;

@Mod.EventBusSubscriber(modid = IndustrialWarfare.MOD_ID, bus = Bus.MOD)
public class DataGeneration {

	@SubscribeEvent
	public static void registerDataProviders(GatherDataEvent event) {
		DataGenerator datagen = event.getGenerator();
		ExistingFileHelper helper = event.getExistingFileHelper();
		
		if (event.includeClient()) {
			datagen.addProvider(new ItemModelGeneration(datagen, helper));
			datagen.addProvider(new BlockStateModelGeneration(datagen, helper));
			datagen.addProvider(new SoundsGeneration(datagen, helper));
		}
		
		if (event.includeServer()) {
			datagen.addProvider(new RecipeGeneration(datagen));
			datagen.addProvider(new IWLootTableProvider(datagen));
			TagsGeneration.addAll(datagen, helper);
		}
	}
		
}
