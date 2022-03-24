package rbasamoyai.industrialwarfare.client;

import java.awt.event.KeyEvent;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.settings.ToggleableKeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import rbasamoyai.industrialwarfare.IndustrialWarfare;

@Mod.EventBusSubscriber(modid = IndustrialWarfare.MOD_ID, bus = Bus.MOD, value = Dist.CLIENT)
public class KeyBindingsInit {

	public static final String KEY_ROOT = "key." + IndustrialWarfare.MOD_ID;
	public static final String CATEGORY = KEY_ROOT + ".category";
	
	public static KeyBinding ITEM_SCREEN = new KeyBinding(KEY_ROOT + ".item_screen", KeyEvent.VK_C, CATEGORY);
	public static KeyBinding DIPLOMACY_SCREEN = new KeyBinding(KEY_ROOT + ".diplomacy_screen", KeyConflictContext.IN_GAME, KeyModifier.ALT, InputMappings.Type.KEYSYM, KeyEvent.VK_1, CATEGORY);
	public static KeyBinding PRONE = new ToggleableKeyBinding(KEY_ROOT + ".prone", KeyEvent.VK_Z, CATEGORY, () -> {
		Minecraft mc = Minecraft.getInstance();
		return mc.options.toggleCrouch;
	});
	public static KeyBinding RELOAD_FIREARM = new KeyBinding(KEY_ROOT + ".reload_firearm", KeyEvent.VK_R, CATEGORY);
	
	@SubscribeEvent
	public static void onClientSetup(FMLClientSetupEvent event) {
		ClientRegistry.registerKeyBinding(ITEM_SCREEN);
		ClientRegistry.registerKeyBinding(DIPLOMACY_SCREEN);
		ClientRegistry.registerKeyBinding(PRONE);
		ClientRegistry.registerKeyBinding(RELOAD_FIREARM);
	}
	
}
