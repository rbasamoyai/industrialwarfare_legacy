package rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.common;

import net.minecraft.network.chat.Component;

/**
 * Somewhat of a hack to add titling for ItemArgWidgets in TaskScrollScreen.
 * @author rbasamoyai
 */

public class ItemArgTitleArgSelector extends EmptyArgSelector {

	public final Component title;
	
	public ItemArgTitleArgSelector(Component title) {
		super();
		this.title = title;
	}

	@Override
	public Component getTitle() {
		return this.title;
	}

}
