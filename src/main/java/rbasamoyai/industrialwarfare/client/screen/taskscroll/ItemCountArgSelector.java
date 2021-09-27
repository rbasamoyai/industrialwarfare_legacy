package rbasamoyai.industrialwarfare.client.screen.taskscroll;

import java.util.Arrays;
import java.util.List;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.client.screen.selectors.ArgSelector;

public class ItemCountArgSelector extends ArgSelector<Byte> {
	
	private static final List<ITextComponent> TOOLTIP = Arrays.asList(new TranslationTextComponent("gui." + IndustrialWarfare.MOD_ID + ".task_scroll.tooltip.selector.item_count").withStyle(ArgSelector.HEADER_STYLE));
	private static final byte MAX_ITEM_COUNT = 64;
	
	private byte count;
	
	public ItemCountArgSelector(int index) {
		super(null);
		this.count = (byte) index;
	}
	
	@Override
	public void scrollSelectedArg(double scrollDist) {
		this.count = (byte) MathHelper.floor(MathHelper.clamp((double) this.count - scrollDist, 0.0d, (double) MAX_ITEM_COUNT));
	}
	
	@Override
	public Byte getSelectedArg() {
		return this.count;
	}
	
	@Override
	public Byte getPossibleArg(int argIndex) {
		return this.count;
	}

	@Override
	public List<ITextComponent> getComponentTooltip() {
		return TOOLTIP;
	}

	@Override
	public ITextComponent getTitle() {
		return new StringTextComponent(this.count == 0 ? "*" : String.valueOf(this.count));
	}

}
