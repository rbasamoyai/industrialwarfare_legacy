package rbasamoyai.industrialwarfare.client.screen.taskscroll;

import java.util.Optional;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.IFormattableTextComponent;
import rbasamoyai.industrialwarfare.client.screen.widgets.ArgSelectorWidget;
import rbasamoyai.industrialwarfare.common.items.taskscroll.ArgSelector;
import rbasamoyai.industrialwarfare.utils.TooltipUtils;
import rbasamoyai.industrialwarfare.utils.WidgetUtils;

public class TaskScrollArgSelectorWidget extends ArgSelectorWidget {
	
	private Runnable runnable;
	
	public TaskScrollArgSelectorWidget(Minecraft minecraft, int x, int y, int width, Optional<ArgSelector<?>> initialSelector, Runnable runnable) {
		super(minecraft, x, y, width, initialSelector);
		this.runnable = runnable;
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollDist) {
		if (!this.isMouseOver(mouseX, mouseY)) return false;
		super.mouseScrolled(mouseX, mouseY, scrollDist);
		if (this.selector.isPresent()) this.runnable.run();
		return true;
	}
	
	public void setSelector(Optional<ArgSelector<?>> selector) {
		this.selector = selector;
		WidgetUtils.setActiveAndVisible(this, this.selector.isPresent());
		IFormattableTextComponent ftc = (IFormattableTextComponent) this.selector.map(ArgSelector::getTitle).orElse(TooltipUtils.NOT_AVAILABLE);
		this.shortenedTitle = TooltipUtils.getShortenedTitle(ftc, this.font, this.fieldWidth);
	}
	
}