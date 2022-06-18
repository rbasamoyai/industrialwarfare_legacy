package rbasamoyai.industrialwarfare.utils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.mojang.datafixers.util.Pair;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

public class TextureUtils {

	
	/**
	 * Based on <a href="https://forums.minecraftforge.net/topic/66634-solved-112113-get-color-from-texture/?do=findComment&comment=319893">this post</a>,
	 * with some tweaks. 
	 */
	public static List<Integer> getColors(ResourceLocation loc, List<Pair<Integer, Integer>> coords) {
		InputStream is;
		NativeImage image;
		
		try {
			is = Minecraft.getInstance().getResourceManager().getResource(loc).getInputStream();
			image = NativeImage.read(is);
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<>();
		}
		
		return coords
				.stream()
				.map(c -> {
					int i = image.getPixelRGBA(c.getFirst(), c.getSecond());
					int alpha = i & 0xFF000000;
					int red = (i & 0x000000FF) << 16;
					int blue = i & 0x0000FF00;
					int green = (i & 0x00FF0000) >> 16;
					int argb = alpha | red | blue | green;
					return argb;
				})
				.collect(Collectors.toList());
	}
	
	public static ResourceLocation getWeaponSkinTexture(ItemStack stack) {
		CompoundNBT nbt = stack.getOrCreateTag();
		int skin = nbt.getInt("weaponSkin");
		ResourceLocation regName = stack.getItem().getRegistryName();
		String path = String.format("textures/item/%s%s.png", regName.getPath(), skin > 0 ? Integer.toString(skin) : "");
		ResourceLocation tex = new ResourceLocation(regName.getNamespace(), path);
		
		Minecraft mc = Minecraft.getInstance();
		if (mc.textureManager.getTexture(tex) == null) {
			mc.textureManager.register(tex, new SimpleTexture(tex)); 
		}
		return mc.textureManager.getTexture(tex) == null ? new ResourceLocation(regName.getNamespace(), "textures/item/" + regName.getPath() + ".png") : tex;
	}
	
}
