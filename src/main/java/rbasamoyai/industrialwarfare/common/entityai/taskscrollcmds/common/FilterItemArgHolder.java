package rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.common;

import java.util.Optional;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.client.screen.widgets.ArgSelector;
import rbasamoyai.industrialwarfare.common.containers.TaskScrollContainer;

public class FilterItemArgHolder extends ItemArgHolder {

	private static final ITextComponent TITLE = new TranslationTextComponent("command." + IndustrialWarfare.MOD_ID + ".args.filter");
	
	@Override
	public Optional<ArgSelector<?>> getSelector(TaskScrollContainer container) {
		return Optional.of(new ItemArgTitleArgSelector(TITLE));
	}
	
}
