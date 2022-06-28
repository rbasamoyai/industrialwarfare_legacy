package rbasamoyai.industrialwarfare.core.network.handlers;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;
import rbasamoyai.industrialwarfare.common.containers.DiplomacyMenu;
import rbasamoyai.industrialwarfare.core.network.messages.DiplomacyScreenMessages;

public class DiplomacyScreenCHandlers {

	public static void handleCBroadcastChanges(DiplomacyScreenMessages.CBroadcastChanges msg, Supplier<NetworkEvent.Context> contextSupplier) {
		Minecraft mc = Minecraft.getInstance();
		AbstractContainerMenu ct = mc.player.containerMenu;
		if (!(ct instanceof DiplomacyMenu)) return;
		
		DiplomacyMenu diplomacyCt = (DiplomacyMenu) ct;
		diplomacyCt.setDiplomaticStatuses(msg.diplomaticStatuses);
		diplomacyCt.setRelationships(msg.npcFactionRelationships);
		diplomacyCt.setDirty();
	}
	
}
