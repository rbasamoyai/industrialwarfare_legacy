package rbasamoyai.industrialwarfare.core.init;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import rbasamoyai.industrialwarfare.IndustrialWarfare;

public class SoundEventInit {

	public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, IndustrialWarfare.MOD_ID);
	
	public static final RegistryObject<SoundEvent> BREATHE_ON_MATCH = register("item.firearms.breathe_on_match");
	public static final RegistryObject<SoundEvent> HAMMER_CLICK = register("item.firearms.hammer_click");
	public static final RegistryObject<SoundEvent> HEAVY_RIFLE_FIRED = register("item.heavy_rifle.fired");
	public static final RegistryObject<SoundEvent> INSERT_AMMO = register("item.firearms.insert_ammo");
	public static final RegistryObject<SoundEvent> LEVER_OPEN = register("item.firearms.lever_open");
	public static final RegistryObject<SoundEvent> LEVER_CLOSE = register("item.firearms.lever_close");
	public static final RegistryObject<SoundEvent> REVOLVER_FIRED = register("item.revolver.fired");
	public static final RegistryObject<SoundEvent> RIFLE_CYCLE_END = register("item.rifle.cycle_end");
	public static final RegistryObject<SoundEvent> RIFLE_CYCLE_START = register("item.rifle.cycle_start");
	public static final RegistryObject<SoundEvent> RIFLE_FIRED = register("item.rifle.fired");
	public static final RegistryObject<SoundEvent> SPIT_CARTRIDGE_TAB = register("item.firearms.spit_cartridge_tab");
	
	private static RegistryObject<SoundEvent> register(String eventId) {
		return SOUND_EVENTS.register(eventId, () -> new SoundEvent(new ResourceLocation(IndustrialWarfare.MOD_ID, eventId)));
	}
}
