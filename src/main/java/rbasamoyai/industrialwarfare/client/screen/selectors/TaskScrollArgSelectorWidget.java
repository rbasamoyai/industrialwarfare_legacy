package rbasamoyai.industrialwarfare.client.screen.selectors;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.IFormattableTextComponent;
import rbasamoyai.industrialwarfare.client.screen.taskscroll.BlockPosArgSelector;
import rbasamoyai.industrialwarfare.client.screen.taskscroll.TaskCmdArgSelector;
import rbasamoyai.industrialwarfare.client.screen.taskscroll.TaskScrollScreen;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollOrder;
import rbasamoyai.industrialwarfare.common.taskscrollcmds.TaskScrollCommand;
import rbasamoyai.industrialwarfare.utils.WidgetUtils;

public class TaskScrollArgSelectorWidget extends ArgSelectorWidget {
	
	private Optional<TaskScrollOrder> order;
	
	private int argPos;
	
	private TaskScrollScreen screen;
	
	public TaskScrollArgSelectorWidget(Minecraft minecraft, TaskScrollScreen screen, int x, int y, int width, Optional<TaskScrollOrder> initialOrder, int initialArgPos) {
		super(minecraft, x, y, width, getSelectorFromArgPos(screen, initialArgPos, initialOrder));
		
		this.screen = screen;
		
		this.order = initialOrder;
		this.argPos = initialArgPos;
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollDist) {
		if (!this.isMouseOver(mouseX, mouseY)) return false;
		super.mouseScrolled(mouseX, mouseY, scrollDist);
		if (this.selector.isPresent()) this.screen.updateSelectorRelatedFeatures();
		return true;
	}
	
	public void setOrder(Optional<TaskScrollOrder> order) {
		this.order = order;
		WidgetUtils.setActiveAndVisible(this, this.order.map(o -> this.argPos > o.getCmd().getArgCount()).orElse(false));
	}
	
	public void setSelector(Optional<ArgSelector<?>> selector) {
		this.selector = selector;
		WidgetUtils.setActiveAndVisible(this, this.selector.isPresent());
		IFormattableTextComponent ftc = (IFormattableTextComponent) this.selector.map(as -> as.getTitle()).orElse(NOT_AVAILABLE);
		this.shortenedTitle = this.getShortenedTitle(ftc);
	}
	
	private static Optional<ArgSelector<?>> getSelectorFromArgPos(TaskScrollScreen screen, int argPos, Optional<TaskScrollOrder> optional) {
		if (optional.isPresent() && argPos >= -2 && argPos < optional.map(o -> o.getCmd().getArgCount()).orElse(0)) {
			if (argPos == -2) {
				List<TaskScrollCommand> validCmds = screen.getMenu().getCommands().stream().collect(Collectors.toList());
				return Optional.of(new TaskCmdArgSelector(validCmds, optional.orElse(new TaskScrollOrder(validCmds.get(0)))));
			} else if (argPos == -1) {
				return Optional.of(new BlockPosArgSelector(screen.getPlayer(), optional.map(TaskScrollOrder::getPos).orElse(screen.getPlayer().blockPosition())));
			} else {
				ArgSelector<Byte> selector = optional.map(o -> o.getCmd().getSelectorAt(argPos).apply((int)(o.getArgs().get(argPos)))).orElse(null);
				return Optional.ofNullable(selector);
			}
		} else return Optional.empty();
	}
	
	@SuppressWarnings("unchecked")
	public void setOrderArgs() { // Maybe TODO: see if we can get around the use of instanceof here
		this.order.ifPresent(o -> this.selector.ifPresent(as -> {
			if (this.argPos == -2 && as.getSelectedArg() instanceof TaskScrollCommand) {
				o.setCmdFromSelector((ArgSelector<TaskScrollCommand>) as);
			} else if (this.argPos == -1 && as.getSelectedArg() instanceof BlockPos) {
				o.setPosFromSelector((ArgSelector<BlockPos>) as);
			} else if (this.argPos >= 0 && as.getSelectedArg() instanceof Byte) {
				o.setArgFromSelectorAndIndex((ArgSelector<Byte>) as, this.argPos);
			}
		}));
	}
	
}