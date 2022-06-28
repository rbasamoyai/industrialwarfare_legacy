package rbasamoyai.industrialwarfare.core.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.core.network.messages.CNPCBrainDataSyncMessage;
import rbasamoyai.industrialwarfare.core.network.messages.CQueueEntityAnimMessage;
import rbasamoyai.industrialwarfare.core.network.messages.DiplomacyScreenMessages;
import rbasamoyai.industrialwarfare.core.network.messages.FirearmActionMessages;
import rbasamoyai.industrialwarfare.core.network.messages.ResourceStationMessages;
import rbasamoyai.industrialwarfare.core.network.messages.SEditLabelSyncMessage;
import rbasamoyai.industrialwarfare.core.network.messages.SEditScheduleSyncMessage;
import rbasamoyai.industrialwarfare.core.network.messages.SMatchCoilSyncMessage;
import rbasamoyai.industrialwarfare.core.network.messages.SNPCContainerActivateMessage;
import rbasamoyai.industrialwarfare.core.network.messages.SOpenItemScreen;
import rbasamoyai.industrialwarfare.core.network.messages.SSetProneMessage;
import rbasamoyai.industrialwarfare.core.network.messages.STaskScrollSyncMessage;
import rbasamoyai.industrialwarfare.core.network.messages.SWorkstationPlayerActionMessage;
import rbasamoyai.industrialwarfare.core.network.messages.WhistleScreenMessages;

@Mod.EventBusSubscriber(modid = IndustrialWarfare.MOD_ID, bus = Bus.MOD)
public class IWNetwork {

	public static final String NETWORK_VERSION = "0.12.0";

	public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(IndustrialWarfare.MOD_ID, "network"), () -> NETWORK_VERSION, NETWORK_VERSION::equals,
			NETWORK_VERSION::equals);

	public static void init() {
		int id = 0;
		CHANNEL.registerMessage(id++, SEditLabelSyncMessage.class, SEditLabelSyncMessage::encode, SEditLabelSyncMessage::decode, SEditLabelSyncMessage::handle);
		CHANNEL.registerMessage(id++, SEditScheduleSyncMessage.class, SEditScheduleSyncMessage::encode, SEditScheduleSyncMessage::decode, SEditScheduleSyncMessage::handle);
		CHANNEL.registerMessage(id++, SNPCContainerActivateMessage.class, SNPCContainerActivateMessage::encode, SNPCContainerActivateMessage::decode, SNPCContainerActivateMessage::handle);
		CHANNEL.registerMessage(id++, STaskScrollSyncMessage.class, STaskScrollSyncMessage::encode, STaskScrollSyncMessage::decode, STaskScrollSyncMessage::handle);
		CHANNEL.registerMessage(id++, SWorkstationPlayerActionMessage.class, SWorkstationPlayerActionMessage::encode, SWorkstationPlayerActionMessage::decode, SWorkstationPlayerActionMessage::handle);
		CHANNEL.registerMessage(id++, CNPCBrainDataSyncMessage.class, CNPCBrainDataSyncMessage::encode, CNPCBrainDataSyncMessage::decode, CNPCBrainDataSyncMessage::handle);
		CHANNEL.registerMessage(id++, DiplomacyScreenMessages.SOpenScreen.class, DiplomacyScreenMessages.SOpenScreen::encode, DiplomacyScreenMessages.SOpenScreen::decode, DiplomacyScreenMessages.SOpenScreen::handle);
		CHANNEL.registerMessage(id++, DiplomacyScreenMessages.SRequestUpdate.class, DiplomacyScreenMessages.SRequestUpdate::encode, DiplomacyScreenMessages.SRequestUpdate::decode, DiplomacyScreenMessages.SRequestUpdate::handle);
		CHANNEL.registerMessage(id++, DiplomacyScreenMessages.CBroadcastChanges.class, DiplomacyScreenMessages.CBroadcastChanges::encode, DiplomacyScreenMessages.CBroadcastChanges::decode, DiplomacyScreenMessages.CBroadcastChanges::handle);
		CHANNEL.registerMessage(id++, DiplomacyScreenMessages.SDiplomaticStatusChangeSync.class, DiplomacyScreenMessages.SDiplomaticStatusChangeSync::encode, DiplomacyScreenMessages.SDiplomaticStatusChangeSync::decode, DiplomacyScreenMessages.SDiplomaticStatusChangeSync::handle);
		CHANNEL.registerMessage(id++, FirearmActionMessages.SInputAction.class, FirearmActionMessages.SInputAction::encode, FirearmActionMessages.SInputAction::decode, FirearmActionMessages.SInputAction::handle);
		CHANNEL.registerMessage(id++, FirearmActionMessages.CNotifyHeadshot.class, FirearmActionMessages.CNotifyHeadshot::encode, FirearmActionMessages.CNotifyHeadshot::decode, FirearmActionMessages.CNotifyHeadshot::handle);
		CHANNEL.registerMessage(id++, SOpenItemScreen.class, SOpenItemScreen::encode, SOpenItemScreen::decode, SOpenItemScreen::handle);
		CHANNEL.registerMessage(id++, CQueueEntityAnimMessage.class, CQueueEntityAnimMessage::encode, CQueueEntityAnimMessage::decode, CQueueEntityAnimMessage::handle);
		CHANNEL.registerMessage(id++, SSetProneMessage.class, SSetProneMessage::encode, SSetProneMessage::decode, SSetProneMessage::handle);
		CHANNEL.registerMessage(id++, WhistleScreenMessages.SWhistleScreenSync.class, WhistleScreenMessages.SWhistleScreenSync::encode, WhistleScreenMessages.SWhistleScreenSync::decode, WhistleScreenMessages.SWhistleScreenSync::handle);
		CHANNEL.registerMessage(id++, WhistleScreenMessages.SStopAction.class, WhistleScreenMessages.SStopAction::encode, WhistleScreenMessages.SStopAction::decode, WhistleScreenMessages.SStopAction::handle);
		CHANNEL.registerMessage(id++, WhistleScreenMessages.SStopAllFormationLeaders.class, WhistleScreenMessages.SStopAllFormationLeaders::encode, WhistleScreenMessages.SStopAllFormationLeaders::decode, WhistleScreenMessages.SStopAllFormationLeaders::handle);
		CHANNEL.registerMessage(id++, SMatchCoilSyncMessage.class, SMatchCoilSyncMessage::encode, SMatchCoilSyncMessage::decode, SMatchCoilSyncMessage::handle);
		CHANNEL.registerMessage(id++, ResourceStationMessages.SSelectTab.class, ResourceStationMessages.SSelectTab::encode, ResourceStationMessages.SSelectTab::decode, ResourceStationMessages.SSelectTab::handle);
		CHANNEL.registerMessage(id++, ResourceStationMessages.CSyncRequests.class, ResourceStationMessages.CSyncRequests::encode, ResourceStationMessages.CSyncRequests::decode, ResourceStationMessages.CSyncRequests::handle);
		CHANNEL.registerMessage(id++, ResourceStationMessages.SSetRunning.class, ResourceStationMessages.SSetRunning::encode, ResourceStationMessages.SSetRunning::decode, ResourceStationMessages.SSetRunning::handle);
		CHANNEL.registerMessage(id++, ResourceStationMessages.SSetExtraStock.class, ResourceStationMessages.SSetExtraStock::encode, ResourceStationMessages.SSetExtraStock::decode, ResourceStationMessages.SSetExtraStock::handle);
		CHANNEL.registerMessage(id++, ResourceStationMessages.CSyncExtraStock.class, ResourceStationMessages.CSyncExtraStock::encode, ResourceStationMessages.CSyncExtraStock::decode, ResourceStationMessages.CSyncExtraStock::handle);
		CHANNEL.registerMessage(id++, ResourceStationMessages.SRemoveExtraStock.class, ResourceStationMessages.SRemoveExtraStock::encode, ResourceStationMessages.SRemoveExtraStock::decode, ResourceStationMessages.SRemoveExtraStock::handle);
	}
	
	@SubscribeEvent
	public static void onCommonSetup(FMLCommonSetupEvent event) {
		init();
	}

}
