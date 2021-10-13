package rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.common;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.TaskScrollCommand;
import rbasamoyai.industrialwarfare.common.items.taskscroll.ArgSelector;
import rbasamoyai.industrialwarfare.common.items.taskscroll.ArgWrapper;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollOrder;
import rbasamoyai.industrialwarfare.utils.TooltipUtils;

public class TaskCommandArgSelector extends ArgSelector<TaskScrollCommand> {
	
	private static final IFormattableTextComponent TOOLTIP_HEADER = new TranslationTextComponent("gui." + IndustrialWarfare.MOD_ID + ".task_scroll.tooltip.selector.command").withStyle(ArgSelector.HEADER_STYLE);
	
	public TaskCommandArgSelector(List<TaskScrollCommand> possibleArgs, TaskScrollOrder order) {
		super(possibleArgs, possibleArgs.indexOf(order.getCommand()));
	}
	
	@Override
	public ArgWrapper getSelectedArg() {
		return this.getPossibleArg(this.selectedArg);
	}
	
	@Override
	public ArgWrapper getPossibleArg(int i) {
		return new ArgWrapper(this.possibleArgs.get(i).getRegistryName());
	}

	@Override
	public List<ITextComponent> getComponentTooltip() {
		List<ITextComponent> tooltip = new ArrayList<>();
		tooltip.add(TOOLTIP_HEADER);
		
		if (this.selectedArg < 0 || this.selectedArg >= this.possibleArgs.size()) this.warnInvalidSelection();
		
		for (int i = 0; i < this.possibleArgs.size(); i++) {
			IFormattableTextComponent tc = (IFormattableTextComponent) getCmdLocalization(this.possibleArgs.get(i));
			if (i == this.selectedArg) tc = ArgSelector.formatAsSelected(tc);
			else tc = TooltipUtils.formatAsStyle(tc, ArgSelector.NOT_SELECTED_STYLE);
			tooltip.add(tc);
		}
		
		return tooltip;
	}

	@Override
	public ITextComponent getTitle() {
		return getCmdLocalization(this.possibleArgs.get(this.selectedArg));
	}
	
	private static ITextComponent getCmdLocalization(TaskScrollCommand cmd) {
		ResourceLocation registryName = cmd.getRegistryName();
		return new TranslationTextComponent("command." + registryName.getNamespace() + "." + registryName.getPath());
	}

}
