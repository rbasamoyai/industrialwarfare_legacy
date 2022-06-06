package rbasamoyai.industrialwarfare.client.events;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.entity.SpriteRenderer;
import net.minecraft.item.IDyeableArmorItem;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
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
import rbasamoyai.industrialwarfare.client.screen.MatchCoilScreen;
import rbasamoyai.industrialwarfare.client.screen.NormalWorkstationScreen;
import rbasamoyai.industrialwarfare.client.screen.TaskScrollShelfScreen;
import rbasamoyai.industrialwarfare.client.screen.WhistleScreen;
import rbasamoyai.industrialwarfare.client.screen.attachmentitems.AttachmentsRifleScreen;
import rbasamoyai.industrialwarfare.client.screen.diplomacy.DiplomacyScreen;
import rbasamoyai.industrialwarfare.client.screen.editlabel.EditLabelScreen;
import rbasamoyai.industrialwarfare.client.screen.npc.NPCBaseScreen;
import rbasamoyai.industrialwarfare.client.screen.resource_station.ResourceStationScreen;
import rbasamoyai.industrialwarfare.client.screen.schedule.EditScheduleScreen;
import rbasamoyai.industrialwarfare.client.screen.taskscroll.TaskScrollScreen;
import rbasamoyai.industrialwarfare.client.tileentities.renderers.TaskScrollShelfTileEntityRenderer;
import rbasamoyai.industrialwarfare.common.items.MatchCordItem;
import rbasamoyai.industrialwarfare.core.init.BlockInit;
import rbasamoyai.industrialwarfare.core.init.ContainerInit;
import rbasamoyai.industrialwarfare.core.init.EntityTypeInit;
import rbasamoyai.industrialwarfare.core.init.TileEntityTypeInit;
import rbasamoyai.industrialwarfare.core.init.items.ItemInit;

@Mod.EventBusSubscriber(modid = IndustrialWarfare.MOD_ID, bus = Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {

	private static final String TAG_IS_LIT = MatchCordItem.TAG_IS_LIT;
	
	@SubscribeEvent
	public static void onClientSetup(FMLClientSetupEvent event) {
		ScreenManager.register(ContainerInit.ATTACHMENTS_RIFLE.get(), AttachmentsRifleScreen::new);
		ScreenManager.register(ContainerInit.DIPLOMACY.get(), DiplomacyScreen::new);
		ScreenManager.register(ContainerInit.EDIT_LABEL.get(), EditLabelScreen::new);
		ScreenManager.register(ContainerInit.MATCH_COIL.get(), MatchCoilScreen::new);
		ScreenManager.register(ContainerInit.NORMAL_WORKSTATION.get(), NormalWorkstationScreen::new);
		ScreenManager.register(ContainerInit.NPC_BASE.get(), NPCBaseScreen::new);
		ScreenManager.register(ContainerInit.RESOURCE_STATION.get(), ResourceStationScreen::new);
		ScreenManager.register(ContainerInit.SCHEDULE.get(), EditScheduleScreen::new);
		ScreenManager.register(ContainerInit.TASK_SCROLL.get(), TaskScrollScreen::new);
		ScreenManager.register(ContainerInit.TASK_SCROLL_SHELF.get(), TaskScrollShelfScreen::new);
		ScreenManager.register(ContainerInit.WHISTLE.get(), WhistleScreen::new);
		
		Minecraft mc = Minecraft.getInstance();
		
		RenderingRegistry.registerEntityRenderingHandler(EntityTypeInit.BULLET.get(), erm -> new SpriteRenderer<>(erm, mc.getItemRenderer()));
		RenderingRegistry.registerEntityRenderingHandler(EntityTypeInit.NPC.get(), NPCRenderer::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityTypeInit.FORMATION_LEADER.get(), NothingRenderer::new);
		
		ClientRegistry.bindTileEntityRenderer(TileEntityTypeInit.TASK_SCROLL_SHELF.get(), TaskScrollShelfTileEntityRenderer::new);
		
		event.enqueueWork(() -> {
			ItemModelsProperties.register(ItemInit.MATCH_CORD.get(), new ResourceLocation(IndustrialWarfare.MOD_ID, "is_lit"),
					(stack, level, living) -> {
						return stack.getOrCreateTag().getBoolean(TAG_IS_LIT) ? 1.0f : 0.0f;
					});
			
			ItemModelsProperties.register(ItemInit.INFINITE_MATCH_CORD.get(), new ResourceLocation(IndustrialWarfare.MOD_ID, "is_lit"),
					(stack, level, living) -> {
						return stack.getOrCreateTag().getBoolean(TAG_IS_LIT) ? 1.0f : 0.0f;
					});
			
			ItemModelsProperties.register(ItemInit.MATCH_COIL.get(), new ResourceLocation(IndustrialWarfare.MOD_ID, "coil_amount"),
					(stack, level, living) -> {
						return (float) stack.getItem().getDurabilityForDisplay(stack) * 4.0f;
					});
			
			RenderTypeLookup.setRenderLayer(BlockInit.WORKER_SUPPORT.get(), RenderType.cutout());
			
		});
	}
	
	@SubscribeEvent
	public static void onTextureStitchEventPre(TextureStitchEvent.Pre event) {
		event.addSprite(new ResourceLocation(IndustrialWarfare.MOD_ID, "item/task_icon"));
		event.addSprite(new ResourceLocation(IndustrialWarfare.MOD_ID, "item/schedule_icon"));
		event.addSprite(new ResourceLocation(IndustrialWarfare.MOD_ID, "item/label_icon"));
		event.addSprite(new ResourceLocation(IndustrialWarfare.MOD_ID, "item/recipe_manual_icon"));
		
		event.addSprite(new ResourceLocation(IndustrialWarfare.MOD_ID, "entity/task_scroll"));
	}
	
	@SubscribeEvent
	public static void onColorHandlerItem(ColorHandlerEvent.Item event) {
		ItemColors itemColors = event.getItemColors();
		itemColors.register((stack, layer) -> {
			return layer > 0 ? -1 : ((IDyeableArmorItem) stack.getItem()).getColor(stack);
		}, ItemInit.AMERICAN_KEPI.get());
	}
	
}
