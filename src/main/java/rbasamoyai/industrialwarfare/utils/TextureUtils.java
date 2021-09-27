package rbasamoyai.industrialwarfare.utils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.util.ResourceLocation;

public class TextureUtils {

	
	/**
	 * Based on <a href="https://forums.minecraftforge.net/topic/66634-solved-112113-get-color-from-texture/?do=findComment&comment=319893">this post</a>,
	 * with some tweaks. 
	 */
	public static List<Integer> getColors(ResourceLocation loc, List<Integer[]> coords) {
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
					int i = image.getPixelRGBA(c[0], c[1]);
					int alpha = i & 0xFF000000;
					int red = (i & 0x000000FF) << 16;
					int blue = i & 0x0000FF00;
					int green = (i & 0x00FF0000) >> 16;
					int argb = alpha | red | blue | green;
					return argb;
				})
				.collect(Collectors.toList());
	}
	
}
