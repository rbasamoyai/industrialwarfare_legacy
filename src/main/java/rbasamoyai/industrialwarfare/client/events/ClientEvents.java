package rbasamoyai.industrialwarfare.client.events;

import net.minecraft.client.gui.ScreenManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.client.entities.renderers.NPCRenderer;
import rbasamoyai.industrialwarfare.client.entities.renderers.NothingRenderer;
import rbasamoyai.industrialwarfare.client.screen.NormalWorkstationScreen;
import rbasamoyai.industrialwarfare.client.screen.TaskScrollShelfScreen;
import rbasamoyai.industrialwarfare.client.screen.WhistleScreen;
import rbasamoyai.industrialwarfare.client.screen.attachmentitems.AttachmentsRifleScreen;
import rbasamoyai.industrialwarfare.client.screen.diplomacy.DiplomacyScreen;
import rbasamoyai.industrialwarfare.client.screen.editlabel.EditLabelScreen;
import rbasamoyai.industrialwarfare.client.screen.npc.NPCBaseScreen;
import rbasamoyai.industrialwarfare.client.screen.schedule.EditScheduleScreen;
import rbasamoyai.industrialwarfare.client.screen.taskscroll.TaskScrollScreen;
import rbasamoyai.industrialwarfare.client.tileentities.renderers.TaskScrollShelfTileEntityRenderer;
import rbasamoyai.industrialwarfare.core.init.ContainerInit;
import rbasamoyai.industrialwarfare.core.init.EntityTypeInit;
import rbasamoyai.industrialwarfare.core.init.TileEntityTypeInit;

@Mod.EventBusSubscriber(modid = IndustrialWarfare.MOD_ID, bus = Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {

	@SubscribeEvent
	public static void onClientSetup(FMLClientSetupEvent event) {
		ScreenManager.register(ContainerInit.ATTACHMENTS_RIFLE.get(), AttachmentsRifleScreen::new);
		ScreenManager.register(ContainerInit.DIPLOMACY.get(), DiplomacyScreen::new);
		ScreenManager.register(ContainerInit.EDIT_LABEL.get(), EditLabelScreen::new);
		ScreenManager.register(ContainerInit.NORMAL_WORKSTATION.get(), NormalWorkstationScreen::new);
		ScreenManager.register(ContainerInit.NPC_BASE.get(), NPCBaseScreen::new);
		ScreenManager.register(ContainerInit.SCHEDULE.get(), EditScheduleScreen::new);
		ScreenManager.register(ContainerInit.TASK_SCROLL.get(), TaskScrollScreen::new);
		ScreenManager.register(ContainerInit.TASK_SCROLL_SHELF.get(), TaskScrollShelfScreen::new);
		ScreenManager.register(ContainerInit.WHISTLE.get(), WhistleScreen::new);
		
		RenderingRegistry.registerEntityRenderingHandler(EntityTypeInit.BULLET.get(), NothingRenderer::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityTypeInit.NPC.get(), NPCRenderer::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityTypeInit.FORMATION_LEADER.get(), NothingRenderer::new);
		
		ClientRegistry.bindTileEntityRenderer(TileEntityTypeInit.TASK_SCROLL_SHELF.get(), TaskScrollShelfTileEntityRenderer::new);
	}
	
	@SubscribeEvent
	public static void onTextureStitchEventPre(TextureStitchEvent.Pre event) {
		event.addSprite(new ResourceLocation(IndustrialWarfare.MOD_ID, "item/task_icon"));
		event.addSprite(new ResourceLocation(IndustrialWarfare.MOD_ID, "item/schedule_icon"));
		event.addSprite(new ResourceLocation(IndustrialWarfare.MOD_ID, "item/label_icon"));
		event.addSprite(new ResourceLocation(IndustrialWarfare.MOD_ID, "item/recipe_manual_icon"));
		
		event.addSprite(new ResourceLocation(IndustrialWarfare.MOD_ID, "entity/task_scroll"));
	}
	
}
