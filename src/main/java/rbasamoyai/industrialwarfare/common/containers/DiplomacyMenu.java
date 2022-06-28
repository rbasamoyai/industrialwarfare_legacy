package rbasamoyai.industrialwarfare.common.containers;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.mojang.datafixers.util.Pair;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraftforge.network.PacketDistributor;
import rbasamoyai.industrialwarfare.common.diplomacy.DiplomacySaveData;
import rbasamoyai.industrialwarfare.common.diplomacy.DiplomaticStatus;
import rbasamoyai.industrialwarfare.common.diplomacy.PlayerIDTag;
import rbasamoyai.industrialwarfare.core.init.MenuInit;
import rbasamoyai.industrialwarfare.core.network.IWNetwork;
import rbasamoyai.industrialwarfare.core.network.messages.DiplomacyScreenMessages;

public class DiplomacyMenu extends AbstractContainerMenu {

	public static DiplomacyMenu getClientContainer(int windowId, Inventory playerInv, FriendlyByteBuf buf) {
		return new DiplomacyMenu(windowId, playerInv.player, Optional.empty());
	}
	
	public static MenuConstructor getServerContainerProvider() {
		return (windowId, playerInv, player) -> new DiplomacyMenu(windowId, player, Optional.of(player.level.getServer().overworld()));
	}
	
	private final Optional<ServerLevel> overworld;
	private final Player player;
	
	private final Map<PlayerIDTag, Pair<DiplomaticStatus, DiplomaticStatus>> diplomaticStatuses = new HashMap<>();
	private final Map<UUID, Byte> npcFactionRelationships = new HashMap<>();
	
	private boolean dirty = false;
	
	protected DiplomacyMenu(int windowId, Player player, Optional<ServerLevel> overworld) {
		super(MenuInit.DIPLOMACY.get(), windowId);
		
		this.overworld = overworld;
		this.player = player;
		
		this.updateData();
	}
	
	@Override
	public boolean stillValid(Player player) {
		return true;
	}
	
	@Override
	public void broadcastChanges() {
		super.broadcastChanges();
		
		DiplomacyScreenMessages.CBroadcastChanges msg = new DiplomacyScreenMessages.CBroadcastChanges(this.diplomaticStatuses, this.npcFactionRelationships);
		IWNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) this.player), msg);
	}
	
	public void updateData() {
		if (!this.overworld.isPresent()) return;
		if (!(this.player instanceof ServerPlayer)) return;
		
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
	
	public Player getPlayer() { return this.player; }
	
}
