package rbasamoyai.industrialwarfare.client.screen.taskscroll;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;
import rbasamoyai.industrialwarfare.common.items.taskscroll.ArgWrapper;
import rbasamoyai.industrialwarfare.common.items.taskscroll.IArgHolder;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollOrder;

public class ItemArgWidget extends AbstractWidget {

	private static final int SLOT_WIDTH = 16;
	private static final int HOVER_COLOR = 0x80FFFFFF;
	
	private final List<TaskScrollOrder> orderList;
	private ItemRenderer itemRenderer;
	private int orderIndex;
	private int argIndex;
	
	public ItemArgWidget(int x, int y, int orderIndex, int argIndex, List<TaskScrollOrder> orderList, ItemRenderer itemRenderer) {
		super(x, y, SLOT_WIDTH, SLOT_WIDTH, TextComponent.EMPTY);
		
		this.orderList = orderList;
		
		this.itemRenderer = itemRenderer;
		this.orderIndex = orderIndex;
		this.argIndex = argIndex;
	}
	
	@Override
	public void renderButton(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
		if (0 <= this.orderIndex && this.orderIndex < this.orderList.size()) {
			IArgHolder holder = this.orderList.get(this.orderIndex).getArgHolder(this.argIndex);
			if (holder.isItemStackArg()) {
				ItemStack filterStack = holder.getWrapper().getItem().orElse(ItemStack.EMPTY);
				this.itemRenderer.renderAndDecorateFakeItem(filterStack, this.x, this.y);
				if (this.isHovered) {
					RenderSystem.disableDepthTest();
					this.fillGradient(stack, this.x, this.y, this.x + SLOT_WIDTH, this.y + SLOT_WIDTH, HOVER_COLOR, HOVER_COLOR);
					RenderSystem.enableDepthTest();
				}
			}
		}
	}
	
	public void setItem(ItemStack stack) {
		IArgHolder holder = this.orderList.get(this.orderIndex).getArgHolder(this.argIndex);
		if (holder.isItemStackArg()) {
			ItemStack copy = stack.copy();
			copy.setCount(1);
			holder.accept(new ArgWrapper(copy));
		}
	}
	
	public ItemStack getItem() {
		IArgHolder holder = this.orderList.get(this.orderIndex).getArgHolder(this.argIndex);
		return holder.isItemStackArg() ? holder.getWrapper().getItem().orElse(ItemStack.EMPTY) : ItemStack.EMPTY;
	}
	
	public void setOrderIndex(int index) {
		this.orderIndex = index;
	}
	
	public void setArgIndex(int index) {
		this.argIndex = index;
	}
	
	@Override public void playDownSound(SoundManager manager) {}
	
	@Override
	public void updateNarration(NarrationElementOutput pNarrationElementOutput) {
		
	}
	
}
