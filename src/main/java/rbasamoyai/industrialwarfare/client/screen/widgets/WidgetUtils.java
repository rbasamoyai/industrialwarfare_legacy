package rbasamoyai.industrialwarfare.client.screen.widgets;

import net.minecraft.client.gui.components.AbstractWidget;

public class WidgetUtils {

	public static void setActiveAndVisible(AbstractWidget widget, boolean bool) {
		widget.active = bool;
		widget.visible = bool;
	}
	
}
