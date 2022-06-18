package rbasamoyai.industrialwarfare.core.network.handlers;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.inventory.container.Container;
import net.minecraftforge.fml.network.NetworkEvent;
import rbasamoyai.industrialwarfare.common.containers.DiplomacyContainer;
import rbasamoyai.industrialwarfare.core.network.messages.DiplomacyScreenMessages;

public class DiplomacyScreenCHandlers {

	public static void handleCBroadcastChanges(DiplomacyScreenMessages.CBroadcastChanges msg, Supplier<NetworkEvent.Context> contextSupplier) {
		Minecraft mc = Minecraft.getInstance();
		Container ct = mc.player.containerMenu;
		if (!(ct instanceof DiplomacyContainer)) return;
		
		DiplomacyContainer diplomacyCt = (DiplomacyContainer) ct;
		diplomacyCt.setDiplomaticStatuses(msg.diplomaticStatuses);
		diplomacyCt.setRelationships(msg.npcFactionRelationships);
		diplomacyCt.setDirty();
	}
	
}
