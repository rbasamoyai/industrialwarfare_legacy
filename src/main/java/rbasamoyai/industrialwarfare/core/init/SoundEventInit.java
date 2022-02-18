package rbasamoyai.industrialwarfare.core.init;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import rbasamoyai.industrialwarfare.IndustrialWarfare;

public class SoundEventInit {

	public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, IndustrialWarfare.MOD_ID);
	
	public static final RegistryObject<SoundEvent> HEAVY_RIFLE_FIRED = register("item.heavy_rifle.fired");
	public static final RegistryObject<SoundEvent> INSERT_AMMO = register("item.firearms.insert_ammo");
	public static final RegistryObject<SoundEvent> LEVER_OPEN = register("item.firearms.lever_open");
	public static final RegistryObject<SoundEvent> LEVER_CLOSE = register("item.firearms.lever_close");
	public static final RegistryObject<SoundEvent> RIFLE_CYCLE_END = register("item.rifle.cycle_end");
	public static final RegistryObject<SoundEvent> RIFLE_CYCLE_START = register("item.rifle.cycle_start");
	public static final RegistryObject<SoundEvent> RIFLE_FIRED = register("item.rifle.fired");
	
	private static RegistryObject<SoundEvent> register(String eventId) {
		return SOUND_EVENTS.register(eventId, () -> new SoundEvent(new ResourceLocation(IndustrialWarfare.MOD_ID, eventId)));
	}
}
