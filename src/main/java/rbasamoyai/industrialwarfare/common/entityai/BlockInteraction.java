package rbasamoyai.industrialwarfare.common.entityai;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;

public class BlockInteraction {

	private final GlobalPos pos;
	@Nullable
	private final Item item;
	@Nullable
	private final BiConsumer<World, BlockPos> placeAction;
	private final BiPredicate<World, BlockPos> checkState;
	private final Type action;
	
	private BlockInteraction(GlobalPos pos, @Nullable Item item, @Nullable BiConsumer<World, BlockPos> placeAction,
			@Nonnull BiPredicate<World, BlockPos> checkState, Type action) {
		this.pos = pos;
		this.item = item;
		this.placeAction = placeAction;
		this.checkState = checkState;
		this.action = action;
	}
	
	public GlobalPos pos() { return this.pos; }
	public Item item() { return this.item; }
	public Type action() { return this.action; }
	
	@SuppressWarnings("deprecation")
	public boolean needsToBreakBlock(World level) {
		switch (this.action) {
		case BREAK_BLOCK: return true;
		case PLACE_BLOCK:
			BlockState blockState = level.getBlockState(this.pos.pos());
			return !blockState.isAir() && !(blockState.getBlock() instanceof FlowingFluidBlock) && !this.checkState(level);
		}
		return false;
	}
	
	public void executePlaceActionIfPossible(World level) {
		if (this.placeAction != null) {
			this.placeAction.accept(level, this.pos.pos());
		}
	}
	
	public boolean checkState(World level) {
		return this.checkState.test(level, this.pos.pos());
	}
	
	public static enum Type {
		BREAK_BLOCK,
		PLACE_BLOCK
	}
	
	public static BlockInteraction placeBlockAtAs(GlobalPos pos, @Nonnull Item item, @Nonnull BiConsumer<World, BlockPos> action, @Nonnull BiPredicate<World, BlockPos> checkState) {
		return new BlockInteraction(pos, item, action, checkState, Type.PLACE_BLOCK);
	}
	
	@SuppressWarnings("deprecation")
	public static BlockInteraction breakBlockAt(GlobalPos pos) {
		return new BlockInteraction(pos, null, null, (world, bpos) -> world.getBlockState(bpos).isAir(), Type.BREAK_BLOCK);
	}
	
	@Override
	public int hashCode() {
		return this.pos.pos().hashCode();
	}
	
}
