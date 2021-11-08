package rbasamoyai.industrialwarfare.client;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import com.mojang.authlib.GameProfile;

import net.minecraft.tileentity.SkullTileEntity;

public class PlayerInfo {

	private static Map<UUID, GameProfile> profiles = new HashMap<>();
	
	public static boolean has(UUID uuid) {
		return profiles.containsKey(uuid);
	}
	
	public static GameProfile get(UUID uuid) {
		return profiles.get(uuid);
	}
	
	public static void queueProfileFill(UUID uuid, Executor exec) {
		CompletableFuture.supplyAsync(() -> {
			GameProfile profile = new GameProfile(uuid, "");
			if (SkullTileEntity.sessionService == null) return profile;
			try {
				return SkullTileEntity.sessionService.fillProfileProperties(profile, true);
			} catch (Exception e) {
				e.printStackTrace();
				return profile;
			}
		}).thenAcceptAsync(gp -> {
			profiles.put(uuid, gp);
		}, exec);
	}
	
	private PlayerInfo() {}
	
}
