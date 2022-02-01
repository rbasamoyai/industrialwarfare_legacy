package rbasamoyai.industrialwarfare.common.entityai.formation.formations;

import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Consumer;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;
import rbasamoyai.industrialwarfare.common.entities.FormationLeaderEntity;
import rbasamoyai.industrialwarfare.common.entityai.formation.FormationEntityWrapper;
import rbasamoyai.industrialwarfare.common.entityai.formation.UnitFormationType;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;

public abstract class UnitFormation implements INBTSerializable<CompoundNBT> {

	private final UnitFormationType<?> type;
	protected final World level;
	protected State formationState = State.BROKEN;
	
	private boolean loadDataOnNextTick = false;
	private CompoundNBT dataToDeserialize;
	
	public UnitFormation(UnitFormationType<?> type, World level) {
		this.type = type;
		this.level = level;
	}
	
	public void setState(State formationState) { this.formationState = formationState; } 
	public State getState() { return this.formationState; }
	
	public abstract boolean addEntity(CreatureEntity entity);
	public abstract void executeGroupAction(int group, Consumer<CreatureEntity> action);
	public abstract float getWidth();
	public abstract float getDepth();
	
	protected abstract void tick(FormationLeaderEntity leader);
	protected abstract void loadEntityData(CompoundNBT nbt);
	
	public UnitFormationType<?> getType() {
		return this.type;
	}
	
	public final void doTick(FormationLeaderEntity leader) {
		if (this.loadDataOnNextTick) {
			this.loadEntityData(this.dataToDeserialize);
			this.loadDataOnNextTick = false;
			this.dataToDeserialize = new CompoundNBT();
		}
		this.tick(leader);
	}
	
	public static boolean isSlotEmpty(FormationEntityWrapper<?> wrapper) {
		if (wrapper == null) return true;
		CreatureEntity entity = wrapper.getEntity();
		return entity == null || entity.isDeadOrDying();
	}
	
	public static boolean checkMemoriesForMovement(CreatureEntity entity) {
		Brain<?> brain = entity.getBrain();
		return brain.checkMemory(MemoryModuleType.MEETING_POINT, MemoryModuleStatus.REGISTERED)
			&& brain.checkMemory(MemoryModuleTypeInit.PRECISE_POS.get(), MemoryModuleStatus.REGISTERED);
	}
	
	private static final String TAG_STATE = "state";
	
	@Override
	public CompoundNBT serializeNBT() {
		CompoundNBT nbt = new CompoundNBT();
		nbt.putInt(TAG_STATE, this.formationState.getId());
		return nbt;
	}
	
	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		this.formationState = State.fromId(nbt.getInt(TAG_STATE));
		this.loadDataOnNextTick = true;
		this.dataToDeserialize = nbt;
	}
	
	public static enum State {
		BROKEN(0),
		FORMING(1),
		FORMED(2);
		
		private static final State[] BY_ID = Arrays.stream(values()).sorted(Comparator.comparingInt(State::getId)).toArray(sz -> new State[sz]);
		
		private final int id;
		
		private State(int id) {
			this.id = id;
		}
		
		public int getId() { return this.id; }
		public static State fromId(int id) {
			return 0 <= id && id < BY_ID.length ? BY_ID[id] : BROKEN;
		}
	}
	
}
