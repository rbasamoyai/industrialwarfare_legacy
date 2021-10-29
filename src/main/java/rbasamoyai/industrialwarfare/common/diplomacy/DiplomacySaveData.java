package rbasamoyai.industrialwarfare.common.diplomacy;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

public class DiplomacySaveData extends WorldSavedData {

	private static final String DATA_NAME = "diplomacySaveData";
	
	private static final String TAG_DIPLOMACY = "diplomacy";
	private static final String TAG_PLAYER = "player";
	private static final String TAG_STATUSES = "statuses";
	private static final String TAG_STATUS = "status";
	
	private static final String TAG_NPC_FACTION_RELATIONS = "npcFactionRelations";
	private static final String TAG_NPC_FACTION = "npcFaction";
	private static final String TAG_RELATIONS = "relations";
	private static final String TAG_RELATIONSHIP = "relationship";
	
	private Map<PlayerIDTag, Map<PlayerIDTag, DiplomaticStatus>> diplomacyTable = new HashMap<>();
	private Map<UUID, Map<PlayerIDTag, Byte>> npcFactionsRelationsTable = new HashMap<>(); // UUID only as relations only apply to NPCs, whose owner UUID is always OwnerTag#GAIA_UUID
	
	public DiplomacySaveData() {
		super(DATA_NAME);
	}
	
	public static DiplomacySaveData get(World world) {
		if (!(world instanceof ServerWorld)) {
			throw new RuntimeException("Attempted to get data from client world");
		}
		ServerWorld overworld = world.getServer().getLevel(World.OVERWORLD);
		DimensionSavedDataManager dataManager = overworld.getDataStorage();
		return dataManager.computeIfAbsent(DiplomacySaveData::new, DATA_NAME);
	}
	
	/* diplomacyTable methods */
	
	public Map<PlayerIDTag, DiplomaticStatus> getDiplomaticStatuses(PlayerIDTag player) {
		Map<PlayerIDTag, DiplomaticStatus> diplomaticStatuses = this.diplomacyTable.get(player);
		if (diplomaticStatuses == null) {
			diplomaticStatuses = new HashMap<>();
			this.diplomacyTable.put(player, diplomaticStatuses);
			this.setDirty();
		}
		return diplomaticStatuses;
	}
	
	public boolean hasPlayerIdTag(PlayerIDTag player) {
		return this.diplomacyTable.containsKey(player);
	}
	
	public void setDiplomaticStatus(PlayerIDTag setting, PlayerIDTag target, DiplomaticStatus status) {
		if (setting.equals(target)) return;
		Map<PlayerIDTag, DiplomaticStatus> diplomaticStatuses = this.getDiplomaticStatuses(setting);
		diplomaticStatuses.put(target, status);
		this.setDirty();
	}
	
	public void initPlayerDiplomacyStatuses(PlayerEntity player) {
		PlayerIDTag tag = PlayerIDTag.of(player);
		Map<PlayerIDTag, DiplomaticStatus> playerDiplomaticStatuses = new HashMap<>();
		
		for (PlayerIDTag keyTag : this.diplomacyTable.keySet()) {
			if (!keyTag.isPlayer()) continue;
			
			playerDiplomaticStatuses.put(keyTag, DiplomaticStatus.NEUTRAL);
			this.setDiplomaticStatus(keyTag, tag, DiplomaticStatus.NEUTRAL);
		}
		
		this.diplomacyTable.put(tag, playerDiplomaticStatuses);
		
		this.setDirty();
	}
	
	/* npcFactionsRelationsTable methods */
	
	public Map<PlayerIDTag, Byte> getRelations(UUID npcFactionUuid) {
		Map<PlayerIDTag, Byte> relations = this.npcFactionsRelationsTable.get(npcFactionUuid);
		if (relations == null) {
			relations = new HashMap<>();
			this.npcFactionsRelationsTable.put(npcFactionUuid, relations);
			this.setDirty();
		}
		return relations;
	}
	
	public void setRelations(UUID setting, PlayerIDTag target, byte relationship) {
		if (setting.equals(target.getNpcFactionUuid())) return;
		Map<PlayerIDTag, Byte> relations = this.getRelations(setting);
		relations.put(target, relationship);
		this.setDirty();
	}
	
	@Override
	public void load(CompoundNBT tag) {
		ListNBT diplomacyList = tag.getList(TAG_DIPLOMACY, Constants.NBT.TAG_COMPOUND);
		this.diplomacyTable.clear();
		for (int i = 0; i < diplomacyList.size(); i++) {
			CompoundNBT e1 = diplomacyList.getCompound(i);
			PlayerIDTag owner = PlayerIDTag.fromNBT(e1.getCompound(TAG_PLAYER));
			
			Map<PlayerIDTag, DiplomaticStatus> statusMap = new HashMap<>();
			ListNBT statusList = e1.getList(TAG_STATUSES, Constants.NBT.TAG_COMPOUND);
			for (int j = 0; j < statusList.size(); j++) {
				CompoundNBT e2 = statusList.getCompound(j);
				PlayerIDTag player = PlayerIDTag.fromNBT(e2.getCompound(TAG_PLAYER));
				DiplomaticStatus status = DiplomaticStatus.fromValue(e2.getByte(TAG_STATUS));
				statusMap.put(player, status);
			}
			this.diplomacyTable.put(owner, statusMap);
		}
		
		ListNBT npcFactionsRelationsList = tag.getList(TAG_NPC_FACTION_RELATIONS, Constants.NBT.TAG_COMPOUND);
		this.npcFactionsRelationsTable.clear();
		for (int i = 0; i < npcFactionsRelationsList.size(); i++) {
			CompoundNBT e1 = npcFactionsRelationsList.getCompound(i);
			UUID npcFaction = e1.getUUID(TAG_NPC_FACTION);
			
			Map<PlayerIDTag, Byte> relationsMap = new HashMap<>();
			ListNBT relationsList = e1.getList(TAG_STATUSES, Constants.NBT.TAG_COMPOUND);
			for (int j = 0; j < relationsList.size(); j++) {
				CompoundNBT e2 = relationsList.getCompound(j);
				PlayerIDTag player = PlayerIDTag.fromNBT(e2.getCompound(TAG_PLAYER));
				byte relationship = e2.getByte(TAG_RELATIONSHIP);
				relationsMap.put(player, relationship);
			}
			this.npcFactionsRelationsTable.put(npcFaction, relationsMap);
		}
	}

	@Override
	public CompoundNBT save(CompoundNBT tag) {
		ListNBT diplomacyList = new ListNBT();
		for (Entry<PlayerIDTag, Map<PlayerIDTag, DiplomaticStatus>> e1 : this.diplomacyTable.entrySet()) {
			CompoundNBT entry1 = new CompoundNBT();
			entry1.put(TAG_PLAYER, e1.getKey().serializeNBT());
			
			ListNBT statusList = new ListNBT();
			Map<PlayerIDTag, DiplomaticStatus> statusMap = e1.getValue();
			if (statusMap != null) {
				for (Entry<PlayerIDTag, DiplomaticStatus> e2 : statusMap.entrySet()) {
					CompoundNBT entry2 = new CompoundNBT();
					entry2.put(TAG_PLAYER, e2.getKey().serializeNBT());
					entry2.putByte(TAG_STATUS, e2.getValue().getValue());
					statusList.add(entry2);
				}
			}
			entry1.put(TAG_STATUSES, statusList);
			
			diplomacyList.add(entry1);
		}
		tag.put(TAG_DIPLOMACY, diplomacyList);
		
		ListNBT npcFactionsRelationsList = new ListNBT();
		for (Entry<UUID, Map<PlayerIDTag, Byte>> e1 : this.npcFactionsRelationsTable.entrySet()) {
			CompoundNBT entry1 = new CompoundNBT();
			entry1.putUUID(TAG_NPC_FACTION, e1.getKey());
			
			ListNBT relationsList = new ListNBT();
			Map<PlayerIDTag, Byte> relationshipMap = e1.getValue();
			if (relationshipMap != null) {
				for (Entry<PlayerIDTag, Byte> e2 : relationshipMap.entrySet()) {
					CompoundNBT entry2 = new CompoundNBT();
					entry2.put(TAG_PLAYER, e2.getKey().serializeNBT());
					entry2.putByte(TAG_RELATIONSHIP, e2.getValue());
					relationsList.add(entry2);
				}
			}
			entry1.put(TAG_RELATIONS, relationsList);
			
			npcFactionsRelationsList.add(entry1);
		}
		tag.put(TAG_NPC_FACTION_RELATIONS, npcFactionsRelationsList);
		return tag;
	}

}
