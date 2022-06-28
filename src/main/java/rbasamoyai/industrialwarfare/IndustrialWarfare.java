package rbasamoyai.industrialwarfare;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import rbasamoyai.industrialwarfare.core.config.IWConfig;
import rbasamoyai.industrialwarfare.core.init.BlockInit;
import rbasamoyai.industrialwarfare.core.init.MenuInit;
import rbasamoyai.industrialwarfare.core.init.EntityTypeInit;
import rbasamoyai.industrialwarfare.core.init.FormationAttackTypeInit;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;
import rbasamoyai.industrialwarfare.core.init.NPCCombatSkillInit;
import rbasamoyai.industrialwarfare.core.init.NPCComplaintInit;
import rbasamoyai.industrialwarfare.core.init.NPCProfessionInit;
import rbasamoyai.industrialwarfare.core.init.SoundEventInit;
import rbasamoyai.industrialwarfare.core.init.TaskScrollCommandInit;
import rbasamoyai.industrialwarfare.core.init.BlockEntityTypeInit;
import rbasamoyai.industrialwarfare.core.init.UnitFormationTypeInit;
import rbasamoyai.industrialwarfare.core.init.items.FirearmInit;
import rbasamoyai.industrialwarfare.core.init.items.ItemInit;
import rbasamoyai.industrialwarfare.core.init.items.PartItemInit;
import software.bernie.example.GeckoLibMod;
import software.bernie.geckolib3.GeckoLib;

@Mod(IndustrialWarfare.MOD_ID)
public class IndustrialWarfare {

	public static final Logger LOGGER = LogManager.getLogger();
	public static final String MOD_ID = "industrialwarfare";
	
	public IndustrialWarfare() {
		GeckoLibMod.DISABLE_IN_DEV = true;
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		
		BlockInit.BLOCKS.register(modEventBus);
		
		ItemInit.ITEMS.register(modEventBus);
		FirearmInit.FIREARMS.register(modEventBus);
		PartItemInit.PARTS.register(modEventBus);
		
		BlockEntityTypeInit.TILE_ENTITY_TYPES.register(modEventBus);
		MenuInit.CONTAINER_TYPES.register(modEventBus);
		EntityTypeInit.ENTITY_TYPES.register(modEventBus);
		MemoryModuleTypeInit.MEMORY_MODULE_TYPES.register(modEventBus);
		SoundEventInit.SOUND_EVENTS.register(modEventBus);
		
		FormationAttackTypeInit.FORMATION_ATTACK_TYPES.register(modEventBus);
		NPCCombatSkillInit.COMBAT_SKILLS.register(modEventBus);
		NPCComplaintInit.COMPLAINTS.register(modEventBus);
		NPCProfessionInit.PROFESSIONS.register(modEventBus);
		TaskScrollCommandInit.TASK_SCROLL_COMMANDS.register(modEventBus);
		UnitFormationTypeInit.UNIT_FORMATION_TYPES.register(modEventBus);
		
		ModLoadingContext.get().registerConfig(Type.SERVER, IWConfig.SPEC, "industrialwarfare-server.toml");
		
		GeckoLib.initialize();
	}
	
}
