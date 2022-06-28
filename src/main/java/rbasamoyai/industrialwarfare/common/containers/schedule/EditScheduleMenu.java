package rbasamoyai.industrialwarfare.common.containers.schedule;

import java.util.ArrayList;
import java.util.List;

import com.mojang.datafixers.util.Pair;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.inventory.SimpleContainerData;
import rbasamoyai.industrialwarfare.core.init.MenuInit;
import rbasamoyai.industrialwarfare.utils.TimeUtils;

public class EditScheduleMenu extends AbstractContainerMenu {

	private static final float WEEK_MINUTES = (float) TimeUtils.WEEK_MINUTES;
	
	private final InteractionHand hand;
	private final ContainerData data;
	private final List<Pair<Integer, Integer>> schedule;
	
	public static EditScheduleMenu getClientContainer(int windowId, Inventory playerInv, FriendlyByteBuf buf) {
		InteractionHand hand = buf.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
		int maxMinutes = buf.readVarInt();
		int maxShifts = buf.readVarInt();
		int size = buf.readVarInt();
		List<Pair<Integer, Integer>> schedule = new ArrayList<>(size);
		
		for (int i = 0; i < size; i++) {
			int start = buf.readInt();
			int end = buf.readInt();
			schedule.add(Pair.of(start, end));
		}
		
		ContainerData array = new SimpleContainerData(3);
		array.set(1, maxMinutes);
		array.set(2, maxShifts);
		
		return new EditScheduleMenu(windowId, array, schedule, hand);
	}
	
	public static MenuConstructor getServerContainerProvider(List<Pair<Integer, Integer>> schedule, int maxMinutes, int maxShifts, InteractionHand hand) {
		return (windowId, playerInv, data) -> new EditScheduleMenu(windowId, new EditScheduleDataSync(playerInv.player.level, maxMinutes, maxShifts), schedule, hand);
	}
	
	public EditScheduleMenu(int windowId, ContainerData data, List<Pair<Integer, Integer>> schedule, InteractionHand hand) {
		super(MenuInit.SCHEDULE.get(), windowId);
		
		this.hand = hand;
		
		this.schedule = schedule;
		
		this.data = data;
		this.addDataSlots(data);
	}
	
	public InteractionHand getHand() {
		return this.hand;
	}
	
	public int getMinuteOfTheWeek() {
		return this.data.get(0);
	}
	
	public float getMinuteOfTheWeekScaled() {
		return (float) this.getMinuteOfTheWeek() / WEEK_MINUTES;
	}
	
	public int getMaxMinutes() {
		return this.data.get(1);
	}
	
	public int getMaxShifts() {
		return this.data.get(2);
	}
	
	public boolean canAddShift() {
		int minutes = schedule.stream()
				.map(shift -> shift.getSecond() - shift.getFirst())
				.reduce(0, Integer::sum);
		return this.schedule.size() + 1 <= this.getMaxShifts() && minutes < this.getMaxMinutes();
	}
	
	public List<Pair<Integer, Integer>> getSchedule() {
		return this.schedule;
	}
	
	@Override
	public boolean stillValid(Player player) {
		return true;
	}

}
