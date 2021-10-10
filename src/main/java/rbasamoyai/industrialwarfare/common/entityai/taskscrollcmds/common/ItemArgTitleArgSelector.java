package rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.common;

import net.minecraft.util.text.ITextComponent;

/**
 * Somewhat of a hack to add titling for ItemArgWidgets in TaskScrollScreen.
 * @author rbasamoyai
 */

public class ItemArgTitleArgSelector extends EmptyArgSelector {

	public final ITextComponent title;
	
	public ItemArgTitleArgSelector(ITextComponent title) {
		super();
		this.title = title;
	}

	@Override
	public ITextComponent getTitle() {
		return this.title;
	}

}
