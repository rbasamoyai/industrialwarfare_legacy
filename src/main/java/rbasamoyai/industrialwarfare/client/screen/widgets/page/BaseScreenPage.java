package rbasamoyai.industrialwarfare.client.screen.widgets.page;

import net.minecraft.client.gui.screen.Screen;

public class BaseScreenPage implements IScreenPage {
	
	protected final Screen screen;
	
	public BaseScreenPage(Screen screen) {
		this.screen = screen;
	}
	
	@Override public void tick() {}
	
	@Override public Screen getScreen() { return this.screen; }
	
}
