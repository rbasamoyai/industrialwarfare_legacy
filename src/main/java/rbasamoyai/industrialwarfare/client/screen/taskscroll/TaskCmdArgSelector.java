package rbasamoyai.industrialwarfare.client.screen.taskscroll;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.client.screen.selectors.ArgSelector;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollOrder;
import rbasamoyai.industrialwarfare.common.taskscrollcmds.TaskScrollCommand;
import rbasamoyai.industrialwarfare.utils.TooltipUtils;

public class TaskCmdArgSelector extends ArgSelector<TaskScrollCommand> {
	
	private static final IFormattableTextComponent TOOLTIP_HEADER = new TranslationTextComponent("gui." + IndustrialWarfare.MOD_ID + ".task_scroll.tooltip.selector.command").withStyle(ArgSelector.HEADER_STYLE);
	
	public TaskCmdArgSelector(List<TaskScrollCommand> possibleArgs, TaskScrollOrder order) {
		super(possibleArgs, possibleArgs.indexOf(order.getCommand()));
	}

	@Override
	public List<ITextComponent> getComponentTooltip() {
		ArrayList<ITextComponent> tooltip = new ArrayList<>();
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
		return getCmdLocalization(this.getSelectedArg());
	}
	
	private static ITextComponent getCmdLocalization(TaskScrollCommand cmd) {
		ResourceLocation registryName = cmd.getRegistryName();
		return new TranslationTextComponent("gui." + registryName.getNamespace() + ".task_scroll.command." + registryName.getPath());
	}

}
