package rbasamoyai.industrialwarfare.client.screen;

import java.util.Map;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.resources.ResourceLocation;
import rbasamoyai.industrialwarfare.client.PlayerInfo;
import rbasamoyai.industrialwarfare.common.diplomacy.PlayerIDTag;

public class ScreenUtils {

	public static void drawFace(PlayerIDTag tag, PoseStack stack, int x, int y, float scaled) {
		if (!tag.isPlayer()) return;
		
		Minecraft mc = Minecraft.getInstance();
		
		GameProfile profile = PlayerInfo.get(tag.getUUID());
		boolean invertFace;
		if (profile == null) {
			RenderSystem.setShaderTexture(0, DefaultPlayerSkin.getDefaultSkin(tag.getUUID()));
			invertFace = false;
		} else {
			SkinManager sm = mc.getSkinManager();
			Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = sm.getInsecureSkinInformation(profile);
			ResourceLocation loc = map.containsKey(Type.SKIN)
					? sm.registerTexture(map.get(Type.SKIN), Type.SKIN)
					: DefaultPlayerSkin.getDefaultSkin(tag.getUUID());
			RenderSystem.setShaderTexture(0, loc);
			invertFace = profile.getName().equals("Dinnerbone") || profile.getName().equals("Grumm");
		}
		
		int texY = 8 + (invertFace ? 8 : 0);
		int height = 8 * (invertFace ? -1 : 1);
		
		float sr = 1.0f / scaled;
		
		stack.pushPose();
		stack.scale(scaled, scaled, 1.0f);
		stack.translate((float) x * sr, (float) y * sr, 0.0f);
		// Taken from PlayerTabOverlayGui#render
		GuiComponent.blit(stack, 0, 0, 8, 8, 8.0f, (float) texY, 8, height, 64, 64); // Head
		GuiComponent.blit(stack, 0, 0, 8, 8, 40.0f, (float) texY, 8, height, 64, 64); // Helmet
		stack.popPose();
	}
	
}
