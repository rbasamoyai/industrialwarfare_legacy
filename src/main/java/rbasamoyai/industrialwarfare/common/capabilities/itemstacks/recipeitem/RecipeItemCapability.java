package rbasamoyai.industrialwarfare.common.capabilities.itemstacks.recipeitem;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;

public class RecipeItemCapability {

	public static final Capability<IRecipeItemData> INSTANCE = CapabilityManager.get(new CapabilityToken<>(){});

	public static void register(RegisterCapabilitiesEvent event) {
		event.register(IRecipeItemData.class);
	}

}
