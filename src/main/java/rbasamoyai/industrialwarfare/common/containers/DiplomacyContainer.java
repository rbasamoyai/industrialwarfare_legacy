package rbasamoyai.industrialwarfare.common.containers;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.mojang.datafixers.util.Pair;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.IContainerProvider;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.PacketDistributor;
import rbasamoyai.industrialwarfare.common.diplomacy.DiplomacySaveData;
import rbasamoyai.industrialwarfare.common.diplomacy.DiplomaticStatus;
import rbasamoyai.industrialwarfare.common.diplomacy.PlayerIDTag;
import rbasamoyai.industrialwarfare.core.init.ContainerInit;
import rbasamoyai.industrialwarfare.core.network.IWNetwork;
import rbasamoyai.industrialwarfare.core.network.messages.DiplomacyScreenMessages;

public class DiplomacyContainer extends Container {

	public static DiplomacyContainer getClientContainer(int windowId, PlayerInventory playerInv, PacketBuffer buf) {
		return new DiplomacyContainer(windowId, playerInv.player, Optional.empty());
	}
	
	public static IContainerProvider getServerContainerProvider() {
		return (windowId, playerInv, player) -> new DiplomacyContainer(windowId, player, Optional.of(player.level.getServer().overworld()));
	}
	
	private final Optional<ServerWorld> overworld;
	private final PlayerEntity player;
	
	private final Map<PlayerIDTag, Pair<DiplomaticStatus, DiplomaticStatus>> diplomaticStatuses = new HashMap<>();
	private final Map<UUID, Byte> npcFactionRelationships = new HashMap<>();
	
	private boolean dirty = false;
	
	protected DiplomacyContainer(int windowId, PlayerEntity player, Optional<ServerWorld> overworld) {
		super(ContainerInit.DIPLOMACY.get(), windowId);
		
		this.overworld = overworld;
		this.player = player;
		
		this.updateData();
	}
	
	@Override
	public boolean stillValid(PlayerEntity player) {
		return true;
	}
	
	@Override
	public void broadcastChanges() {
		super.broadcastChanges();
		
		DiplomacyScreenMessages.CBroadcastChanges msg = new DiplomacyScreenMessages.CBroadcastChanges(this.diplomaticStatuses, this.npcFactionRelationships);
		IWNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) this.player), msg);
	}
	
	public void updateData() {
		if (!this.overworld.isPresent()) return;
		if (!(this.player instanceof ServerPlayerEntity)) return;
		
		DiplomacySaveData saveData = DiplomacySaveData.get(this.overworld.get());
		PlayerIDTag playerTag = PlayerIDTag.of(this.player);
		
		this.setDiplomaticStatuses(saveData.getDiplomaticStatusesBothWays(playerTag));
		this.setRelationships(saveData.getRelationsTowards(playerTag));
	}
	
	public void setDiplomaticStatuses(Map<PlayerIDTag, Pair<DiplomaticStatus, DiplomaticStatus>> diplomaticStatuses) {
		this.diplomaticStatuses.putAll(diplomaticStatuses);
	}
	
	public Map<PlayerIDTag, Pair<DiplomaticStatus, DiplomaticStatus>> getDiplomaticStatuses() {
		return this.diplomaticStatuses;
	}
	
	public void setRelationships(Map<UUID, Byte> relationships) {
		this.npcFactionRelationships.putAll(relationships);
	}
	
	public Map<UUID, Byte> getRelationships() {
		return this.npcFactionRelationships;
	}
	
	public boolean isDirty() { return this.dirty; }
	
	public void setDirty() { this.setDirty(true); }
	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}
	
	public PlayerEntity getPlayer() { return this.player; }
	
}
