package rbasamoyai.industrialwarfare.client;

import java.awt.event.KeyEvent;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import rbasamoyai.industrialwarfare.IndustrialWarfare;

public class KeyBindingsInit {

	public static final String KEY_ROOT = "key." + IndustrialWarfare.MOD_ID;
	public static final String CATEGORY = KEY_ROOT + ".category";
	
	public static KeyBinding DIPLOMACY_SCREEN = new KeyBinding(KEY_ROOT + ".diplomacy_screen", KeyConflictContext.IN_GAME, KeyModifier.ALT, InputMappings.Type.KEYSYM, KeyEvent.VK_1, CATEGORY);
	
	public static void register() {
		ClientRegistry.registerKeyBinding(DIPLOMACY_SCREEN);
	}
	
}
