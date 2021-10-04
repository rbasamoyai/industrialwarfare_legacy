package rbasamoyai.industrialwarfare.common.containers.schedule;

import java.util.ArrayList;
import java.util.List;

import com.mojang.datafixers.util.Pair;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.IContainerProvider;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.IIntArray;
import net.minecraft.util.IntArray;
import rbasamoyai.industrialwarfare.core.init.ContainerInit;
import rbasamoyai.industrialwarfare.utils.TimeUtils;

public class EditScheduleContainer extends Container {

	private static final float WEEK_MINUTES = (float) TimeUtils.WEEK_MINUTES;
	
	private final Hand hand;
	private final IIntArray data;
	private final List<Pair<Integer, Integer>> schedule;
	
	public static EditScheduleContainer getClientContainer(int windowId, PlayerInventory playerInv, PacketBuffer buf) {
		Hand hand = buf.readBoolean() ? Hand.MAIN_HAND : Hand.OFF_HAND;
		int maxMinutes = buf.readVarInt();
		int maxShifts = buf.readVarInt();
		int size = buf.readVarInt();
		List<Pair<Integer, Integer>> schedule = new ArrayList<>(size);
		
		for (int i = 0; i < size; i++) {
			int start = buf.readInt();
			int end = buf.readInt();
			schedule.add(Pair.of(start, end));
		}
		
		IIntArray array = new IntArray(3);
		array.set(1, maxMinutes);
		array.set(2, maxShifts);
		
		return new EditScheduleContainer(windowId, array, schedule, hand);
	}
	
	public static IContainerProvider getServerContainerProvider(List<Pair<Integer, Integer>> schedule, int maxMinutes, int maxShifts, Hand hand) {
		return (windowId, playerInv, data) -> new EditScheduleContainer(windowId, new EditScheduleDataSync(playerInv.player.level, maxMinutes, maxShifts), schedule, hand);
	}
	
	public EditScheduleContainer(int windowId, IIntArray data, List<Pair<Integer, Integer>> schedule, Hand hand) {
		super(ContainerInit.SCHEDULE, windowId);
		
		this.hand = hand;
		
		this.schedule = schedule;
		
		this.data = data;
		this.addDataSlots(data);
	}
	
	public Hand getHand() {
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
	public boolean stillValid(PlayerEntity player) {
		return true;
	}

}
