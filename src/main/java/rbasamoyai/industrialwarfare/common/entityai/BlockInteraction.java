package rbasamoyai.industrialwarfare.common.entityai;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;

public class BlockInteraction {

	private final GlobalPos pos;
	private final SupplyRequestPredicate item;
	@Nullable
	private final IBlockLevelAction blockAction;
	private final ICheckBlockState checkState;
	private final Type action;
	private final int reachDistance;
	
	public BlockInteraction(GlobalPos pos, SupplyRequestPredicate item, @Nullable IBlockLevelAction placeAction, 
			@Nonnull ICheckBlockState checkState, Type action, int reachDistance) {
		this.pos = pos;
		this.item = item;
		this.blockAction = placeAction;
		this.checkState = checkState;
		this.action = action;
		this.reachDistance = reachDistance;
	}
	
	public GlobalPos pos() { return this.pos; }
	public SupplyRequestPredicate item() { return this.item; }
	public Type action() { return this.action; }
	public int reachDistance() { return this.reachDistance; }
	
	public boolean needsToBreakBlock(Level level, LivingEntity entity) {
		switch (this.action) {
		case BREAK_BLOCK: return true;
		case PLACE_BLOCK:
			BlockState blockState = level.getBlockState(this.pos.pos());
			return !blockState.isAir() && !(blockState.getBlock() instanceof LiquidBlock) && !this.checkState(level, entity);
		default: return false;
		}
	}
	
	public void executeBlockActionIfPossible(Level level, LivingEntity entity) {
		if (this.blockAction != null && level.dimension() == this.pos.dimension()) {
			this.blockAction.doBlockAction(level, this.pos.pos(), entity);
		}
	}
	
	public boolean checkState(Level level, LivingEntity entity) {
		return this.checkState.test(level, this.pos.pos(), entity);
	}
	
	@Override
	public int hashCode() {
		return this.pos.pos().hashCode();
	}
	
	@Override
	public String toString() {
		return this.action.toString() + " @ " + this.pos.toString();
	}
	
	public static BlockInteraction placeBlockAtAs(GlobalPos pos, SupplyRequestPredicate item, @Nonnull IBlockLevelAction action, @Nonnull ICheckBlockState checkState) {
		return new BlockInteraction(pos, item, action, checkState, Type.PLACE_BLOCK, 4);
	}
	
	public static BlockInteraction breakBlockAt(GlobalPos pos, int reachDistance) {
		return new BlockInteraction(pos, SupplyRequestPredicate.ANY, null, (level, bpos, e) -> level.getBlockState(bpos).isAir(), Type.BREAK_BLOCK, reachDistance);
	}
	
	public static BlockInteraction modifyBlockAt(GlobalPos pos, SupplyRequestPredicate item, @Nonnull IBlockLevelAction action, @Nonnull ICheckBlockState checkState) {
		return new BlockInteraction(pos, item, action, checkState, Type.MODIFY_BLOCK, 4);
	}
	
	@FunctionalInterface
	public static interface IBlockLevelAction {
		void doBlockAction(Level level, BlockPos pos, LivingEntity entity);
	}
	
	@FunctionalInterface
	public static interface ICheckBlockState {
		boolean test(Level level, BlockPos pos, LivingEntity entity);
	}
	
	public static enum Type {
		BREAK_BLOCK,
		PLACE_BLOCK,
		MODIFY_BLOCK
	}
}
