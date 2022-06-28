package rbasamoyai.industrialwarfare.client;

import java.awt.event.KeyEvent;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ToggleKeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import rbasamoyai.industrialwarfare.IndustrialWarfare;

@Mod.EventBusSubscriber(modid = IndustrialWarfare.MOD_ID, bus = Bus.MOD, value = Dist.CLIENT)
public class KeyBindingsInit {

	public static final String KEY_ROOT = "key." + IndustrialWarfare.MOD_ID;
	public static final String CATEGORY = KEY_ROOT + ".category";
	
	public static KeyMapping ITEM_SCREEN = new KeyMapping(KEY_ROOT + ".item_screen", KeyEvent.VK_C, CATEGORY);
	public static KeyMapping DIPLOMACY_SCREEN = new KeyMapping(KEY_ROOT + ".diplomacy_screen", KeyConflictContext.IN_GAME, KeyModifier.ALT, InputConstants.Type.KEYSYM, KeyEvent.VK_1, CATEGORY);
	public static KeyMapping PRONE = new ToggleKeyMapping(KEY_ROOT + ".prone", KeyEvent.VK_Z, CATEGORY, () -> {
		Minecraft mc = Minecraft.getInstance();
		return mc.options.toggleCrouch;
	});
	public static KeyMapping RELOAD_FIREARM = new KeyMapping(KEY_ROOT + ".reload_firearm", KeyEvent.VK_R, CATEGORY);
	public static KeyMapping PREVIOUS_FIREARM_STAGE = new KeyMapping(KEY_ROOT + ".previous_firearm_stage", KeyEvent.VK_V, CATEGORY);
	
	@SubscribeEvent
	public static void onClientSetup(FMLClientSetupEvent event) {
		ClientRegistry.registerKeyBinding(ITEM_SCREEN);
		ClientRegistry.registerKeyBinding(DIPLOMACY_SCREEN);
		ClientRegistry.registerKeyBinding(PREVIOUS_FIREARM_STAGE);
		ClientRegistry.registerKeyBinding(PRONE);
		ClientRegistry.registerKeyBinding(RELOAD_FIREARM);
	}
	
}
