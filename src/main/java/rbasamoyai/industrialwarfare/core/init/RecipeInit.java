package rbasamoyai.industrialwarfare.core.init;

import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.recipes.NormalWorkstationRecipe;
import rbasamoyai.industrialwarfare.common.recipes.RecipeTypeNormalWorkstation;

@Mod.EventBusSubscriber(modid = IndustrialWarfare.MOD_ID, bus = Bus.MOD)
public class RecipeInit {
	
	public static final IRecipeType<NormalWorkstationRecipe> NORMAL_WORKSTATION_RECIPE_TYPE = new RecipeTypeNormalWorkstation();
	
	@SubscribeEvent
	public static void registerSerializers(RegistryEvent.Register<IRecipeSerializer<?>> event) {
		Registry.register(Registry.RECIPE_TYPE, new ResourceLocation(NORMAL_WORKSTATION_RECIPE_TYPE.toString()), NORMAL_WORKSTATION_RECIPE_TYPE);
		
		event.getRegistry().register(NormalWorkstationRecipe.SERIALIZER);
	}
	
}
