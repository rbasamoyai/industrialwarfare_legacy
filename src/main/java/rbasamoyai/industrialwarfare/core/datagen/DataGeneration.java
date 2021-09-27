package rbasamoyai.industrialwarfare.core.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.core.datagen.loottables.IWLootTableProvider;

@Mod.EventBusSubscriber(modid = IndustrialWarfare.MOD_ID, bus = Bus.MOD)
public class DataGeneration {

	@SubscribeEvent
	public static void registerDataProviders(GatherDataEvent event) {
		DataGenerator datagen = event.getGenerator();
		
		if (event.includeClient()) {
			datagen.addProvider(new ItemModels(datagen, event.getExistingFileHelper()));
			datagen.addProvider(new BlockStates(datagen, event.getExistingFileHelper()));
		}
		
		if (event.includeServer()) {
			datagen.addProvider(new Recipes(datagen));
			datagen.addProvider(new IWLootTableProvider(datagen));
		}
	}
		
}
