package rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.items.taskscroll.ArgSelector;
import rbasamoyai.industrialwarfare.common.items.taskscroll.ArgWrapper;
import rbasamoyai.industrialwarfare.utils.TooltipUtils;

public class StorageSideAccessArgSelector extends ArgSelector<Direction> {

	private static final String TOOLTIP_TRANSLATION_KEY = "selector.tooltip." + IndustrialWarfare.MOD_ID + ".storage_side";
	private static final MutableComponent TOOLTIP_HEADER = new TranslatableComponent(TOOLTIP_TRANSLATION_KEY).withStyle(ArgSelector.HEADER_STYLE);
	
	public StorageSideAccessArgSelector(int index) {
		super(Arrays.asList(Direction.values()), index); // Direction#values is only used for size
	}
	
	@Override
	public ArgWrapper getSelectedArg() {
		return this.getPossibleArg(this.selectedArg);
	}
	
	@Override
	public ArgWrapper getPossibleArg(int argIndex) {
		return new ArgWrapper(this.selectedArg);
	}

	@Override
	public List<Component> getComponentTooltip() {
		ArrayList<Component> tooltip = new ArrayList<>();
		tooltip.add(TOOLTIP_HEADER.copy());
		
		if (this.selectedArg < 0 || this.selectedArg >= this.possibleArgs.size()) this.warnInvalidSelection();
		
		this.possibleArgs.forEach(dir -> {
			MutableComponent tc = getTooltip(dir);
			if (dir == Direction.from3DDataValue(this.selectedArg)) tc = ArgSelector.formatAsSelected(tc);
			else tc = TooltipUtils.formatAsStyle(tc, ArgSelector.NOT_SELECTED_STYLE);
			tooltip.add(tc);
		});
		
		return tooltip;
	}

	@Override
	public Component getTitle() {
		return getTooltip(Direction.from3DDataValue(this.selectedArg));
	}
	
	private static MutableComponent getTooltip(Direction dir) {
		return new TranslatableComponent(TOOLTIP_TRANSLATION_KEY + "." + dir.getName());
	}

}
