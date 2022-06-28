package rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.common;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.TaskScrollCommand;
import rbasamoyai.industrialwarfare.common.items.taskscroll.ArgSelector;
import rbasamoyai.industrialwarfare.common.items.taskscroll.ArgWrapper;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollOrder;
import rbasamoyai.industrialwarfare.utils.TooltipUtils;

public class TaskCommandArgSelector extends ArgSelector<TaskScrollCommand> {
	
	private static final MutableComponent TOOLTIP_HEADER = new TranslatableComponent("selector.tooltip." + IndustrialWarfare.MOD_ID + ".command").withStyle(ArgSelector.HEADER_STYLE);
	
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
	public List<Component> getComponentTooltip() {
		List<Component> tooltip = new ArrayList<>();
		tooltip.add(TOOLTIP_HEADER);
		
		if (this.selectedArg < 0 || this.selectedArg >= this.possibleArgs.size()) this.warnInvalidSelection();
		
		for (int i = 0; i < this.possibleArgs.size(); i++) {
			MutableComponent tc = (MutableComponent) getCmdLocalization(this.possibleArgs.get(i));
			if (i == this.selectedArg) tc = ArgSelector.formatAsSelected(tc);
			else tc = TooltipUtils.formatAsStyle(tc, ArgSelector.NOT_SELECTED_STYLE);
			tooltip.add(tc);
		}
		
		return tooltip;
	}

	@Override
	public Component getTitle() {
		return getCmdLocalization(this.possibleArgs.get(this.selectedArg));
	}
	
	private static Component getCmdLocalization(TaskScrollCommand cmd) {
		ResourceLocation registryName = cmd.getRegistryName();
		return new TranslatableComponent("command." + registryName.getNamespace() + "." + registryName.getPath());
	}

}
