package rbasamoyai.industrialwarfare.client.screen.resourcestation;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.client.gui.widget.ForgeSlider;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.client.screen.widgets.WidgetUtils;
import rbasamoyai.industrialwarfare.common.containers.resourcestation.LivestockPenMenu;
import rbasamoyai.industrialwarfare.core.network.IWNetwork;
import rbasamoyai.industrialwarfare.core.network.messages.ResourceStationMessages.SSyncLivestockCount;

public class LivestockPenScreen extends ResourceStationScreen<LivestockPenMenu> {

	private static final int TOP_MENU_START_X = 7;
	private static final int TOP_MENU_START_Y = 17;
	
	private ForgeSlider countSlider;
	
	public LivestockPenScreen(LivestockPenMenu menu, Inventory playerInv, Component title) {
		super(menu, playerInv, title);
	}
	
	@Override
	protected void init() {
		super.init();
		this.countSlider = this.addRenderableWidget(new ForgeSlider(
						this.leftPos + TOP_MENU_START_X + 62,
						this.topPos + TOP_MENU_START_Y + 30,
						100, 20,
						new TranslatableComponent("gui." + IndustrialWarfare.MOD_ID + ".livestock_pen.minimum_livestock"),
						TextComponent.EMPTY,
						2, 16,
						this.menu.getMinimumLivestock(),
						true));
		WidgetUtils.setActiveAndVisible(this.countSlider, this.menu.getSelected() == 0);
	}
	
	@Override
	public void containerTick() {
		super.containerTick();
		
		int tab = this.menu.getSelected();
		WidgetUtils.setActiveAndVisible(this.countSlider, tab == 0);
		if (tab == 0) {
			int count = this.countSlider.getValueInt();
			boolean flag = this.menu.getMinimumLivestock() != count;
			if (flag) {
				this.menu.setMinimumLivestock(count);
				IWNetwork.CHANNEL.sendToServer(new SSyncLivestockCount(count));
			}
		}
	}
	
}
