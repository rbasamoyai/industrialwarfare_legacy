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
				.with(Sound.sound(loc("firearms/rifle/cycle_end1"), SoundDefinition.SoundType.SOUND)
						.volume(1.0f)
						.pitch(0.9f)
						.attenuationDistance(4))
				.with(Sound.sound(loc("firearms/rifle/cycle_end1"), SoundDefinition.SoundType.SOUND)
						.volume(1.0f)
						.pitch(1.0f)
						.attenuationDistance(4))
				.with(Sound.sound(loc("firearms/rifle/cycle_end2"), SoundDefinition.SoundType.SOUND)
						.volume(1.0f)
						.pitch(0.9f)
						.attenuationDistance(4))
				.with(Sound.sound(loc("firearms/rifle/cycle_end2"), SoundDefinition.SoundType.SOUND)
						.volume(1.0f)
						.pitch(1.0f)
						.attenuationDistance(4)));

		add(loc("item.rifle.cycle_start"), SoundDefinition.definition()
				.with(Sound.sound(loc("firearms/rifle/cycle_start1"), SoundDefinition.SoundType.SOUND)
						.volume(1.0f)
						.pitch(0.9f)
						.attenuationDistance(4))
				.with(Sound.sound(loc("firearms/rifle/cycle_start1"), SoundDefinition.SoundType.SOUND)
						.volume(1.0f)
						.pitch(1.0f)
						.attenuationDistance(4))
				.with(Sound.sound(loc("firearms/rifle/cycle_start2"), SoundDefinition.SoundType.SOUND)
						.volume(1.0f)
						.pitch(0.9f)
						.attenuationDistance(4))
				.with(Sound.sound(loc("firearms/rifle/cycle_start2"), SoundDefinition.SoundType.SOUND)
						.volume(1.0f)
						.pitch(1.0f)
						.attenuationDistance(4)));

		add(loc("item.rifle.fired"), SoundDefinition.definition()
				.subtitle("subtitle.industrialwarfare.item.rifle.fired")
				.with(Sound.sound(loc("firearms/rifle/fired"), SoundDefinition.SoundType.SOUND)
						.volume(2.0f)
						.pitch(0.8f)
						.attenuationDistance(32))
				.with(Sound.sound(loc("firearms/rifle/fired"), SoundDefinition.SoundType.SOUND)
						.volume(2.0f)
						.pitch(0.9f)
						.attenuationDistance(32))
				.with(Sound.sound(loc("firearms/rifle/fired"), SoundDefinition.SoundType.SOUND)
						.volume(2.0f)
						.pitch(1.0f)
						.attenuationDistance(32)));
		
		add(loc("item.heavy_rifle.fired"), SoundDefinition.definition()
				.subtitle("subtitle.industrialwarfare.item.heavy_rifle.fired")
				.with(Sound.sound(loc("firearms/heavy_rifle/fired"), SoundDefinition.SoundType.SOUND)
						.volume(2.5f)
						.pitch(0.8f)
						.attenuationDistance(40))
				.with(Sound.sound(loc("firearms/heavy_rifle/fired"), SoundDefinition.SoundType.SOUND)
						.volume(2.5f)
						.pitch(0.9f)
						.attenuationDistance(40))
				.with(Sound.sound(loc("firearms/heavy_rifle/fired"), SoundDefinition.SoundType.SOUND)
						.volume(2.5f)
						.pitch(1.0f)
						.attenuationDistance(40)));

		add(loc("item.firearms.lever_open"), SoundDefinition.definition()
				.with(Sound.sound(loc("firearms/lever_action_open"), SoundDefinition.SoundType.SOUND)
						.volume(1.0f)
						.pitch(0.9f)
						.attenuationDistance(4))
				.with(Sound.sound(loc("firearms/lever_action_open"), SoundDefinition.SoundType.SOUND)
						.volume(1.0f)
						.pitch(0.95f)
						.attenuationDistance(4))
				.with(Sound.sound(loc("firearms/lever_action_open"), SoundDefinition.SoundType.SOUND)
						.volume(1.0f)
						.pitch(1.0f)
						.attenuationDistance(4)));
		
		add(loc("item.firearms.lever_close"), SoundDefinition.definition()
				.with(Sound.sound(loc("firearms/lever_action_close"), SoundDefinition.SoundType.SOUND)
						.volume(1.0f)
						.pitch(0.9f)
						.attenuationDistance(4))
				.with(Sound.sound(loc("firearms/lever_action_close"), SoundDefinition.SoundType.SOUND)
						.volume(1.0f)
						.pitch(0.95f)
						.attenuationDistance(4))
				.with(Sound.sound(loc("firearms/lever_action_close"), SoundDefinition.SoundType.SOUND)
						.volume(1.0f)
						.pitch(1.0f)
						.attenuationDistance(4)));

	}
	
	private ResourceLocation loc(String id) {
		return new ResourceLocation(IndustrialWarfare.MOD_ID, id);
	}
	
	@Override
	public String getName() {
		return "Industrial Warfare sound definitions";
	}
	
}
