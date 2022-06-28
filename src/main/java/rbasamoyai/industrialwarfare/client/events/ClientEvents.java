package rbasamoyai.industrialwarfare.client.events;

import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeableArmorItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.client.blockentities.renderers.TaskScrollShelfRenderer;
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
import rbasamoyai.industrialwarfare.client.screen.resourcestation.ResourceStationScreen;
import rbasamoyai.industrialwarfare.client.screen.schedule.EditScheduleScreen;
import rbasamoyai.industrialwarfare.client.screen.taskscroll.TaskScrollScreen;
import rbasamoyai.industrialwarfare.common.items.MatchCordItem;
import rbasamoyai.industrialwarfare.common.items.firearms.FirearmItem;
import rbasamoyai.industrialwarfare.core.init.BlockEntityTypeInit;
import rbasamoyai.industrialwarfare.core.init.BlockInit;
import rbasamoyai.industrialwarfare.core.init.EntityTypeInit;
import rbasamoyai.industrialwarfare.core.init.MenuInit;
import rbasamoyai.industrialwarfare.core.init.items.ItemInit;

@Mod.EventBusSubscriber(modid = IndustrialWarfare.MOD_ID, bus = Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {

	private static final String TAG_IS_LIT = MatchCordItem.TAG_IS_LIT;
	
	@SubscribeEvent
	public static void onClientSetup(FMLClientSetupEvent event) {
		MenuScreens.register(MenuInit.ATTACHMENTS_RIFLE.get(), AttachmentsRifleScreen::new);
		MenuScreens.register(MenuInit.DIPLOMACY.get(), DiplomacyScreen::new);
		MenuScreens.register(MenuInit.EDIT_LABEL.get(), EditLabelScreen::new);
		MenuScreens.register(MenuInit.MATCH_COIL.get(), MatchCoilScreen::new);
		MenuScreens.register(MenuInit.NORMAL_WORKSTATION.get(), NormalWorkstationScreen::new);
		MenuScreens.register(MenuInit.NPC_BASE.get(), NPCBaseScreen::new);
		MenuScreens.register(MenuInit.RESOURCE_STATION.get(), ResourceStationScreen::new);
		MenuScreens.register(MenuInit.SCHEDULE.get(), EditScheduleScreen::new);
		MenuScreens.register(MenuInit.TASK_SCROLL.get(), TaskScrollScreen::new);
		MenuScreens.register(MenuInit.TASK_SCROLL_SHELF.get(), TaskScrollShelfScreen::new);
		MenuScreens.register(MenuInit.WHISTLE.get(), WhistleScreen::new);
		
		ForgeRegistries.ITEMS.forEach(i -> {
			if (i instanceof FirearmItem) {
				MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, ((FirearmItem) i)::onCameraSetup);
			}
		});
		
		ItemProperties.register(ItemInit.MATCH_CORD.get(), new ResourceLocation(IndustrialWarfare.MOD_ID, "is_lit"),
				(stack, level, living, seed) -> {
					return stack.getOrCreateTag().getBoolean(TAG_IS_LIT) ? 1.0f : 0.0f;
				});
		
		ItemProperties.register(ItemInit.INFINITE_MATCH_CORD.get(), new ResourceLocation(IndustrialWarfare.MOD_ID, "is_lit"),
				(stack, level, living, seed) -> {
					return stack.getOrCreateTag().getBoolean(TAG_IS_LIT) ? 1.0f : 0.0f;
				});
		
		ItemProperties.register(ItemInit.MATCH_COIL.get(), new ResourceLocation(IndustrialWarfare.MOD_ID, "coil_amount"),
				(stack, level, living, seed) -> {
					return (float) stack.getDamageValue() * 4.0f / (float) stack.getMaxDamage();
				});
		
		ItemBlockRenderTypes.setRenderLayer(BlockInit.WORKER_SUPPORT.get(), RenderType.cutout());
	}
	
	@SubscribeEvent
	public static void onEntityRenderersRegister(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(EntityTypeInit.BULLET.get(), ThrownItemRenderer::new);
		event.registerEntityRenderer(EntityTypeInit.NPC.get(), NPCRenderer::new);
		event.registerEntityRenderer(EntityTypeInit.FORMATION_LEADER.get(), NothingRenderer::new);
		
		event.registerBlockEntityRenderer(BlockEntityTypeInit.TASK_SCROLL_SHELF.get(), TaskScrollShelfRenderer::new);
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
			return layer > 0 ? -1 : ((DyeableArmorItem) stack.getItem()).getColor(stack);
		}, ItemInit.AMERICAN_KEPI.get());
		
		itemColors.register((stack, layer) -> {
			return layer > 0 ? -1 : ((DyeableArmorItem) stack.getItem()).getColor(stack);
		}, ItemInit.PICKELHAUBE_HIGH.get());
	}
	
}
