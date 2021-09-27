package rbasamoyai.industrialwarfare.client.screen.taskscroll;

import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollOrder;

public class FilterItemWidget extends Widget {

	private static final int SLOT_WIDTH = 16;
	private static final int HOVER_COLOR = 0x80FFFFFF;
	
	private final List<TaskScrollOrder> orderList;
	private ItemRenderer itemRenderer;
	private int index;
	
	public FilterItemWidget(int x, int y, int index, List<TaskScrollOrder> orderList, ItemRenderer itemRenderer) {
		super(x, y, SLOT_WIDTH, SLOT_WIDTH, StringTextComponent.EMPTY);
		
		this.orderList = orderList;
		this.itemRenderer = itemRenderer;
		this.index = index;
	}
	
	@Override
	public void renderButton(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
		if (this.index >= 0 && this.index < this.orderList.size()) {
			TaskScrollOrder order = this.orderList.get(this.index);
			if (order.canUseFilter()) {
				ItemStack filterStack = order.getFilter();
				this.itemRenderer.renderAndDecorateFakeItem(filterStack, this.x, this.y);
				if (this.isHovered) {
					RenderSystem.disableDepthTest();
					RenderSystem.colorMask(true, true, true, false);
					this.fillGradient(stack, this.x, this.y, this.x + SLOT_WIDTH, this.y + SLOT_WIDTH, HOVER_COLOR, HOVER_COLOR);
					RenderSystem.colorMask(true, true, true, true);
					RenderSystem.enableDepthTest();
				}
			}
		}
	}
	
	public void setItem(ItemStack stack) {
		this.orderList.get(this.index).setFilter(stack);
	}
	
	public void setIndex(int index) {
		this.index = index;
	}
	
	@Override
	public void playDownSound(SoundHandler handler) {
		
	}
}
