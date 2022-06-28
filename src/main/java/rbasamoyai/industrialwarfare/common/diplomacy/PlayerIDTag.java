package rbasamoyai.industrialwarfare.common.diplomacy;

import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.INBTSerializable;

public class PlayerIDTag implements INBTSerializable<CompoundTag>{

	public static final PlayerIDTag NO_OWNER = new PlayerIDTag(new UUID(0L, 0L), false);
	
	private static final String TAG_PLAYER_UUID = "playerUUID";
	private static final String TAG_IS_PLAYER = "isPlayer";
	
	private UUID uuid;
	private boolean isPlayer;
	
	public PlayerIDTag(UUID uuid, boolean isPlayer) {
		this.uuid = uuid;
		this.isPlayer = isPlayer;
	}
	
	public static PlayerIDTag of(Player player) {
		return new PlayerIDTag(player.getUUID(), true);
	}
	
	// TODO: npc factions #of method
	
	public static PlayerIDTag copy(PlayerIDTag other) {
		return new PlayerIDTag(other.getUUID(), other.isPlayer());
	}
	
	public static PlayerIDTag fromNBT(CompoundTag tag) {
		PlayerIDTag newTag = copy(NO_OWNER);
		newTag.deserializeNBT(tag);
		return newTag;
	}
	
	public UUID getUUID() { return this.uuid; }
	public boolean isPlayer() { return this.isPlayer; }
	
	public void toNetwork(FriendlyByteBuf buf) {
		buf
				.writeUUID(this.uuid)
				.writeBoolean(this.isPlayer);
	}
	
	public static PlayerIDTag fromNetwork(FriendlyByteBuf buf) {
		UUID playerUuid = buf.readUUID();
		boolean isPlayer = buf.readBoolean();
		return new PlayerIDTag(playerUuid, isPlayer);
	}
	
	@Override
	public CompoundTag serializeNBT() {
		CompoundTag tag = new CompoundTag();
		tag.putUUID(TAG_PLAYER_UUID, this.uuid);
		tag.putBoolean(TAG_IS_PLAYER, this.isPlayer);
		return tag;
	}
	
	@Override
	public void deserializeNBT(CompoundTag nbt) {
		this.uuid = nbt.getUUID(TAG_PLAYER_UUID);
		this.isPlayer = nbt.getBoolean(TAG_IS_PLAYER);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof PlayerIDTag)) return false;
		PlayerIDTag other = (PlayerIDTag) obj;
		return this.uuid.equals(other.uuid) && this.isPlayer == other.isPlayer;
	}
	
	@Override
	public int hashCode() {
		int result = this.uuid.hashCode();
		return this.isPlayer ? result : result ^ 0xffffffff;
	}
	
	@Override
	public String toString() {
		String prefix = this.isPlayer ? "player" : "npc_faction";
		return prefix + ":" + this.uuid.toString();
	}
	
}
