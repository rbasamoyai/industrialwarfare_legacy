package rbasamoyai.industrialwarfare.common.diplomacy;

import java.util.UUID;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;

public class PlayerIDTag implements INBTSerializable<CompoundNBT>{

	/**
	 * NPC belongs to "Gaia" (thanks, Age of Empires!), may belong to an NPC faction which is stored in
	 * {@link PlayerIDTag#npcFactionUuid}.
	 */
	public static final UUID GAIA_UUID = new UUID(0L, 0L);
	public static final PlayerIDTag NO_OWNER = new PlayerIDTag(GAIA_UUID, GAIA_UUID);
	
	private static final String TAG_PLAYER_UUID = "playerUUID";
	private static final String TAG_NPC_FACTION_UUID = "factionUUID";
	
	private UUID playerUuid;
	private UUID npcFactionUuid;
	
	public PlayerIDTag(UUID playerUuid, UUID npcFactionUuid) {
		this.playerUuid = playerUuid;
		this.npcFactionUuid = npcFactionUuid;
	}
	
	public static PlayerIDTag of(PlayerEntity player) {
		return new PlayerIDTag(player.getUUID(), GAIA_UUID);
	}
	
	// TODO: npc factions #of method
	
	public static PlayerIDTag copy(PlayerIDTag other) {
		return new PlayerIDTag(other.getPlayerUuid(), other.getNpcFactionUuid());
	}
	
	public static PlayerIDTag fromNBT(CompoundNBT tag) {
		PlayerIDTag newTag = copy(NO_OWNER);
		newTag.deserializeNBT(tag);
		return newTag;
	}
	
	public UUID getPlayerUuid() { return this.playerUuid; }
	public UUID getNpcFactionUuid() { return this.npcFactionUuid; }
	public boolean isPlayer() { return !this.playerUuid.equals(GAIA_UUID); }
	
	@Override
	public CompoundNBT serializeNBT() {
		CompoundNBT tag = new CompoundNBT();
		tag.putUUID(TAG_PLAYER_UUID, this.playerUuid);
		tag.putUUID(TAG_NPC_FACTION_UUID, this.npcFactionUuid);
		return tag;
	}
	
	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		this.playerUuid = nbt.getUUID(TAG_PLAYER_UUID);
		this.npcFactionUuid = nbt.getUUID(TAG_NPC_FACTION_UUID);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof PlayerIDTag)) return false;
		PlayerIDTag other = (PlayerIDTag) obj;
		return this.playerUuid.equals(other.playerUuid) && this.npcFactionUuid.equals(other.npcFactionUuid);
	}
	
	@Override
	public int hashCode() {
		return this.playerUuid.hashCode() ^ this.npcFactionUuid.hashCode();
	}
	
	@Override
	public String toString() {
		return this.playerUuid.toString() + ":" + this.npcFactionUuid.toString();
	}
	
	public static PlayerIDTag fromString(String str) {
		int sepIndex = str.indexOf(":");
		if (sepIndex == -1) {
			throw new RuntimeException("Illegal string with no \":\" separator passed to PlayerIDTag#fromString");
		}
		UUID ownerUuid = UUID.fromString(str.substring(0, sepIndex));
		UUID npcFactionUuid = UUID.fromString(str.substring(sepIndex + 1));
		return new PlayerIDTag(ownerUuid, npcFactionUuid);
	}
	
}
