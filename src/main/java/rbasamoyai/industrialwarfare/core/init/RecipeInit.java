package rbasamoyai.industrialwarfare.core.init;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.recipes.ManufactureRecipe;
import rbasamoyai.industrialwarfare.common.recipes.RecipeTypeNormalWorkstation;

@Mod.EventBusSubscriber(modid = IndustrialWarfare.MOD_ID, bus = Bus.MOD)
public class RecipeInit {
	
	public static final RecipeType<ManufactureRecipe> NORMAL_WORKSTATION_RECIPE_TYPE = new RecipeTypeNormalWorkstation();
	
	@SubscribeEvent
	public static void registerSerializers(RegistryEvent.Register<RecipeSerializer<?>> event) {
		Registry.register(Registry.RECIPE_TYPE, new ResourceLocation(NORMAL_WORKSTATION_RECIPE_TYPE.toString()), NORMAL_WORKSTATION_RECIPE_TYPE);
		
		event.getRegistry().register(ManufactureRecipe.SERIALIZER);
	}
	
}
