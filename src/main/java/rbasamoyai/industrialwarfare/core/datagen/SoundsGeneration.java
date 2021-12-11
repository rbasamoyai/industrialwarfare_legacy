package rbasamoyai.industrialwarfare.core.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.SoundDefinition;
import net.minecraftforge.common.data.SoundDefinition.Sound;
import net.minecraftforge.common.data.SoundDefinitionsProvider;
import rbasamoyai.industrialwarfare.IndustrialWarfare;

public class SoundsGeneration extends SoundDefinitionsProvider {
	
	protected SoundsGeneration(DataGenerator generator, ExistingFileHelper helper) {
		super(generator, IndustrialWarfare.MOD_ID, helper);
	}

	@Override
	public void registerSounds() {
		add(loc("item.rifle.cycle_end"), SoundDefinition.definition()
				.subtitle("subtitle.industrialwarfare.item.rifle.cycled")
				.with(Sound.sound(loc("firearms/rifle/cycle_end1"), SoundDefinition.SoundType.SOUND)
						.attenuationDistance(1)
						.volume(1.0f)
						.pitch(0.9f))
				.with(Sound.sound(loc("firearms/rifle/cycle_end1"), SoundDefinition.SoundType.SOUND)
						.attenuationDistance(1)
						.volume(1.0f)
						.pitch(1.0f))
				.with(Sound.sound(loc("firearms/rifle/cycle_end2"), SoundDefinition.SoundType.SOUND)
						.attenuationDistance(1)
						.volume(1.0f)
						.pitch(0.9f))
				.with(Sound.sound(loc("firearms/rifle/cycle_end2"), SoundDefinition.SoundType.SOUND)
						.attenuationDistance(1)
						.volume(1.0f)
						.pitch(1.0f)));

		add(loc("item.rifle.cycle_start"), SoundDefinition.definition()
				.subtitle("subtitle.industrialwarfare.item.rifle.cycled")
				.with(Sound.sound(loc("firearms/rifle/cycle_start1"), SoundDefinition.SoundType.SOUND)
						.attenuationDistance(1)
						.volume(1.0f)
						.pitch(0.9f))
				.with(Sound.sound(loc("firearms/rifle/cycle_start1"), SoundDefinition.SoundType.SOUND)
						.attenuationDistance(1)
						.volume(1.0f)
						.pitch(1.0f))
				.with(Sound.sound(loc("firearms/rifle/cycle_start2"), SoundDefinition.SoundType.SOUND)
						.attenuationDistance(1)
						.volume(1.0f)
						.pitch(0.9f))
				.with(Sound.sound(loc("firearms/rifle/cycle_start2"), SoundDefinition.SoundType.SOUND)
						.attenuationDistance(1)
						.volume(1.0f)
						.pitch(1.0f)));

		add(loc("item.rifle.fired"), SoundDefinition.definition()
				.subtitle("subtitle.industrialwarfare.item.rifle.fired")
				.with(Sound.sound(loc("firearms/rifle/fired"), SoundDefinition.SoundType.SOUND)
						.attenuationDistance(40)
						.volume(2.0f)
						.pitch(0.8f))
				.with(Sound.sound(loc("firearms/rifle/fired"), SoundDefinition.SoundType.SOUND)
						.attenuationDistance(40)
						.volume(2.0f)
						.pitch(0.9f))
				.with(Sound.sound(loc("firearms/rifle/fired"), SoundDefinition.SoundType.SOUND)
						.attenuationDistance(40)
						.volume(2.0f)
						.pitch(1.0f)));
	}
	
	private ResourceLocation loc(String id) {
		return new ResourceLocation(IndustrialWarfare.MOD_ID, id);
	}
	
	@Override
	public String getName() {
		return "Industrial Warfare sound definitions";
	}
	
}
