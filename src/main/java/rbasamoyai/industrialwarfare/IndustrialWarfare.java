package rbasamoyai.industrialwarfare;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.gui.ScreenManager;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import rbasamoyai.industrialwarfare.client.KeyBindingsInit;
import rbasamoyai.industrialwarfare.client.entities.renderers.NPCRenderer;
import rbasamoyai.industrialwarfare.client.screen.NormalWorkstationScreen;
import rbasamoyai.industrialwarfare.client.screen.TaskScrollShelfScreen;
import rbasamoyai.industrialwarfare.client.screen.diplomacy.DiplomacyScreen;
import rbasamoyai.industrialwarfare.client.screen.editlabel.EditLabelScreen;
import rbasamoyai.industrialwarfare.client.screen.npc.NPCBaseScreen;
import rbasamoyai.industrialwarfare.client.screen.schedule.EditScheduleScreen;
import rbasamoyai.industrialwarfare.client.screen.taskscroll.TaskScrollScreen;
import rbasamoyai.industrialwarfare.client.tileentities.renderers.TaskScrollShelfTileEntityRenderer;
import rbasamoyai.industrialwarfare.common.capabilities.CapabilityHandler;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.core.IWModRegistries;
import rbasamoyai.industrialwarfare.core.config.IWConfig;
import rbasamoyai.industrialwarfare.core.init.BlockInit;
import rbasamoyai.industrialwarfare.core.init.ContainerInit;
import rbasamoyai.industrialwarfare.core.init.EntityTypeInit;
import rbasamoyai.industrialwarfare.core.init.ItemInit;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;
import rbasamoyai.industrialwarfare.core.init.NPCComplaintInit;
import rbasamoyai.industrialwarfare.core.init.NPCProfessionInit;
import rbasamoyai.industrialwarfare.core.init.RecipeInit;
import rbasamoyai.industrialwarfare.core.init.TaskScrollCommandInit;
import rbasamoyai.industrialwarfare.core.init.TileEntityTypeInit;
import rbasamoyai.industrialwarfare.core.network.IWNetwork;

@Mod(IndustrialWarfare.MOD_ID)
public class IndustrialWarfare {

	public static final Logger LOGGER = LogManager.getLogger();
	public static final String MOD_ID = "industrialwarfare";
	
	public IndustrialWarfare() {
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		
		modEventBus.addListener(this::commonSetup);
		modEventBus.addListener(this::clientSetup);
		modEventBus.addListener(this::addEntityAttributes);
		
		modEventBus.register(IWModRegistries.class);
		
		BlockInit.BLOCKS.register(modEventBus);
		ItemInit.ITEMS.register(modEventBus);
		TileEntityTypeInit.TILE_ENTITY_TYPES.register(modEventBus);
		ContainerInit.CONTAINER_TYPES.register(modEventBus);
		EntityTypeInit.ENTITY_TYPES.register(modEventBus);
		MemoryModuleTypeInit.MEMORY_MODULE_TYPES.register(modEventBus);
		
		NPCComplaintInit.COMPLAINTS.register(modEventBus);
		NPCProfessionInit.PROFESSIONS.register(modEventBus);
		TaskScrollCommandInit.TASK_SCROLL_COMMANDS.register(modEventBus);
		
		modEventBus.addGenericListener(EntityType.class, EntityTypeInit::registerSpawnEggs);
		modEventBus.register(RecipeInit.class);
		
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
			modEventBus.addListener(this::addTexturesToStitcher);
		});
		
		ModLoadingContext.get().registerConfig(Type.SERVER, IWConfig.SPEC, "industrialwarfare-server.toml");
	}
	 
	public void commonSetup(FMLCommonSetupEvent event) {
		IWNetwork initNetwork = new IWNetwork();
		initNetwork.init();
		
		CapabilityHandler capHandler = new CapabilityHandler();
		capHandler.registerCapabilities();
		capHandler.addCapabilityListeners();
	}
	
	public void clientSetup(FMLClientSetupEvent event) {
		ScreenManager.register(ContainerInit.DIPLOMACY.get(), DiplomacyScreen::new);
		ScreenManager.register(ContainerInit.EDIT_LABEL.get(), EditLabelScreen::new);
		ScreenManager.register(ContainerInit.NORMAL_WORKSTATION.get(), NormalWorkstationScreen::new);
		ScreenManager.register(ContainerInit.NPC_BASE.get(), NPCBaseScreen::new);
		ScreenManager.register(ContainerInit.SCHEDULE.get(), EditScheduleScreen::new);
		ScreenManager.register(ContainerInit.TASK_SCROLL.get(), TaskScrollScreen::new);
		ScreenManager.register(ContainerInit.TASK_SCROLL_SHELF.get(), TaskScrollShelfScreen::new);
		
		RenderingRegistry.registerEntityRenderingHandler(EntityTypeInit.NPC.get(), NPCRenderer::new);
		
		ClientRegistry.bindTileEntityRenderer(TileEntityTypeInit.TASK_SCROLL_SHELF.get(), TaskScrollShelfTileEntityRenderer::new);
		
		KeyBindingsInit.register();
	}
	
	public void addTexturesToStitcher(TextureStitchEvent.Pre event) {
		event.addSprite(new ResourceLocation(MOD_ID, "item/task_icon"));
		event.addSprite(new ResourceLocation(MOD_ID, "item/schedule_icon"));
		event.addSprite(new ResourceLocation(MOD_ID, "item/label_icon"));
		event.addSprite(new ResourceLocation(MOD_ID, "item/recipe_manual_icon"));
		
		event.addSprite(new ResourceLocation(MOD_ID, "entity/task_scroll"));
	}

	public void addEntityAttributes(EntityAttributeCreationEvent event) {
		event.put(EntityTypeInit.NPC.get(), NPCEntity.setAttributes().build());
	}
	
}
