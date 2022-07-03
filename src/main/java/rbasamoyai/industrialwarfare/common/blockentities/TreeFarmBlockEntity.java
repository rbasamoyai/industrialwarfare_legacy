package rbasamoyai.industrialwarfare.common.blockentities;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.ModTags;
import rbasamoyai.industrialwarfare.common.ModTags.Blocks;
import rbasamoyai.industrialwarfare.common.entityai.BlockInteraction;
import rbasamoyai.industrialwarfare.common.entityai.SupplyRequestPredicate;
import rbasamoyai.industrialwarfare.common.entityai.SupplyRequestPredicate.IntBound;
import rbasamoyai.industrialwarfare.core.init.BlockEntityTypeInit;

public class TreeFarmBlockEntity extends BlockResourcesBlockEntity implements ConfigurableBounds {

	private static final String TAG_STARTING_CORNER = "startingCorner";
	private static final String TAG_ENDING_CORNER = "endingCorner";
	
	private BlockPos startingCorner;
	private BlockPos endingCorner;
	
	private final Map<LivingEntity, Tree> treeWorkers = new HashMap<>();
	private int searchCooldown = 0;
	
	public TreeFarmBlockEntity(BlockPos pos, BlockState state) {
		this(BlockEntityTypeInit.TREE_FARM.get(), pos, state);
	}
	
	public TreeFarmBlockEntity(BlockEntityType<? extends TreeFarmBlockEntity> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}
	
	@Override
	public void trySettingBounds(@Nullable Player player, @Nullable ItemStack stack, BlockPos pos1, BlockPos pos2) {
		if (pos1 == null || pos2 == null) {
			if (player != null) {
				
			}
			return;
		}
		AABB bounds = new AABB(pos1, pos2.offset(1, 1, 1));
		if (bounds.getXsize() > 64 || bounds.getYsize() > 4 || bounds.getZsize() > 64) {
			if (player != null) {
				player.displayClientMessage(new TranslatableComponent("gui." + IndustrialWarfare.MOD_ID + ".too_large", 64, 4, 64, bounds.getXsize(), bounds.getYsize(), bounds.getZsize()).withStyle(ChatFormatting.RED), true);
			}
			return;
		}
		if (!bounds.contains(Vec3.atCenterOf(this.worldPosition))) {
			if (player != null) {
				player.displayClientMessage(new TranslatableComponent("gui." + IndustrialWarfare.MOD_ID + ".must_contain_block", this.worldPosition.toShortString()).withStyle(ChatFormatting.RED), true);
			}
			return;
		}
		this.startingCorner = new BlockPos(bounds.minX, bounds.minY, bounds.minZ);
		this.endingCorner = new BlockPos(bounds.maxX - 1.0d, bounds.maxY - 1.0d, bounds.maxZ - 1.0d);
		if (player != null) {
			player.displayClientMessage(new TranslatableComponent("gui." + IndustrialWarfare.MOD_ID + ".set_bounds", this.worldPosition.toShortString(), this.startingCorner.toShortString(), this.endingCorner.toShortString()), true);
		}
		this.setChanged();
	}
	
	@Override
	public AABB getBoxForRenderingCurrentBounds(ItemStack stack) {
		return this.startingCorner != null && this.endingCorner != null ? new AABB(this.startingCorner.immutable(), this.endingCorner.immutable().offset(1, 1, 1)) : null;
	}
	
	@Override public BlockPos startingCorner() { return this.startingCorner; }
	@Override public BlockPos endingCorner() { return this.endingCorner; }
	
	@Override
	public boolean isFinished() {
		return !this.hasCornersSetUp();
	}
	
	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		if (this.startingCorner != null && this.endingCorner != null) {
			CompoundTag start = new CompoundTag();
			start.putInt("x", this.startingCorner.getX());
			start.putInt("y", this.startingCorner.getY());
			start.putInt("z", this.startingCorner.getZ());
			tag.put(TAG_STARTING_CORNER, start);
			CompoundTag end = new CompoundTag();
			end.putInt("x", this.endingCorner.getX());
			end.putInt("y", this.endingCorner.getY());
			end.putInt("z", this.endingCorner.getZ());
			tag.put(TAG_ENDING_CORNER, end);
		}
	}
	
	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		if (tag.contains(TAG_STARTING_CORNER, Tag.TAG_COMPOUND) && tag.contains(TAG_ENDING_CORNER, Tag.TAG_COMPOUND)) {
			CompoundTag start = tag.getCompound(TAG_STARTING_CORNER);
			BlockPos startPos = new BlockPos(start.getInt("x"), start.getInt("y"), start.getInt("z"));
			CompoundTag end = tag.getCompound(TAG_ENDING_CORNER);
			BlockPos endPos = new BlockPos(end.getInt("x"), end.getInt("y"), end.getInt("z"));
			this.trySettingBounds(null, null, startPos, endPos);
		}
	}
	
	@Override
	public void setRunning(boolean running) {
		super.setRunning(running);
		this.searchCooldown = 0;
		if (!this.isRunning()) {
			this.treeWorkers.clear();
		}
	}
	
	protected void purgeEntries() {
		for (LivingEntity worker : this.treeWorkers.keySet()) {
			if (worker.isDeadOrDying()) {
				this.treeWorkers.remove(worker);
			}
		}
	}

	@Nullable
	@Override
	public BlockInteraction getInteraction(LivingEntity entity) {
		if (!this.isRunning()) return null;
		
		if (!this.treeWorkers.containsKey(entity)) {
			if (this.searchCooldown > 0) return null;
			Tree result = this.findTree();
			if (result == null) {
				this.searchCooldown = 600;
				return null;
			}
			this.treeWorkers.put(entity, result);
		}
		
		Tree tree = this.treeWorkers.get(entity);
		if (tree.interactions().isEmpty()) {
			return this.getSaplingInteraction(tree);
		} else {
			return tree.interactions().poll();
		}
	}
	
	private BlockInteraction getSaplingInteraction(Tree tree) {
		BlockPos pos = tree.pos().immutable();
		BlockState stateBelow = this.level.getBlockState(pos.below());
		
		if (stateBelow.is(Blocks.CAN_PLANT_SAPLING)) {
			return BlockInteraction.placeBlockAtAs(
					GlobalPos.of(this.level.dimension(), pos),
					SupplyRequestPredicate.forItem(ItemTags.SAPLINGS, IntBound.ANY),
					this::placeSapling,
					this::isSapling);
		}
		if (stateBelow.is(Blocks.CAN_PLANT_FUNGUS)) {
			return BlockInteraction.placeBlockAtAs(
					GlobalPos.of(this.level.dimension(), pos),
					SupplyRequestPredicate.forItem(ModTags.Items.FUNGUS, IntBound.ANY),
					this::placeFungus,
					this::isFungus);
		}
		return null;
	}
	
	private Tree findTree() {
		if (!this.hasCornersSetUp()) {
			return null;
		}
		return BlockPos.betweenClosedStream(this.startingCorner, this.endingCorner)
				.filter(p -> this.level.getBlockState(p).is(BlockTags.LOGS))
				.filter(p -> this.level.getBlockState(p.below()).is(Blocks.CAN_PLANT_FORESTRY))
				.filter(p -> !this.hasTreeAt(p))
				.findAny()
				.map(this::addTreeInteractions)
				.orElse(null);
	}
	
	private Tree addTreeInteractions(BlockPos pos) {
		VoxelShape box = Shapes.create(new AABB(this.startingCorner, this.endingCorner.offset(1, 48, 1)));
		VoxelShape box1 = Shapes.create(new AABB(pos.offset(-5, -1, -5), pos.offset(6, 48, 6)));
		VoxelShape limit = Shapes.join(box, box1, BooleanOp.AND);
		if (limit.isEmpty()) {
			return null;
		}
		
		TreeBlockIterator iter = new TreeBlockIterator(this.level, pos, limit.bounds());
		Queue<BlockInteraction> interactions = new LinkedList<>();
		BlockPos immutable = pos.immutable();
		iter.findBlockInteractions()
		.stream()
		.sorted((b1, b2) -> comparePos(b1.pos().pos(), b2.pos().pos(), immutable))
		.forEach(interactions::offer);
		
		return interactions.isEmpty() ? null : new Tree(immutable, interactions);
	}
	
	private static int comparePos(BlockPos p1, BlockPos p2, BlockPos src) {
		return Integer.compare(p1.distManhattan(src), p2.distManhattan(src));
	}
	
	@Override
	protected void findItemsToPickUp() {
		AABB pickupArea = new AABB(this.startingCorner.offset(-5, 0, -5), this.endingCorner.offset(6, 1, 6));
		this.itemsToPickUp.addAll(this.level.getEntities(EntityType.ITEM, pickupArea, item -> {
			BlockPos pos = item.blockPosition();
			return item.level.getBlockState(pos).isAir() && item.level.getBlockState(pos.above()).isAir();
		}));
	}
	
	private boolean hasCornersSetUp() {
		return this.startingCorner != null && this.endingCorner != null;
	}
	
	private boolean hasTreeAt(BlockPos pos) {
		return this.treeWorkers.values().stream().map(Tree::pos).anyMatch(pos::equals);
	}
	
	private void placeSapling(Level level, BlockPos pos, LivingEntity entity) {
		ItemStack stack = entity.getOffhandItem();
		Item item = stack.getItem();
		if (stack.is(ItemTags.SAPLINGS) && item instanceof BlockItem) {
			level.setBlockAndUpdate(pos, ((BlockItem) item).getBlock().getStateDefinition().any());
		}
	}
	
	private boolean isSapling(Level level, BlockPos pos, LivingEntity entity) {
		if (level.getBlockState(pos).is(BlockTags.SAPLINGS)) {
			this.treeWorkers.remove(entity);
			return true;
		}
		return false;
	}
	
	private void placeFungus(Level level, BlockPos pos, LivingEntity entity) {
		ItemStack stack = entity.getOffhandItem();
		Item item = stack.getItem();
		if (stack.is(ModTags.Items.FUNGUS) && item instanceof BlockItem) {
			level.setBlockAndUpdate(pos, ((BlockItem) item).getBlock().getStateDefinition().any());
		}
	}
	
	private boolean isFungus(Level level, BlockPos pos, LivingEntity entity) {
		if (level.getBlockState(pos).is(Blocks.FUNGUS)) {
			this.treeWorkers.remove(entity);
			return true;
		}
		return false;
	}
	
	private static class Tree {
		private final BlockPos start;
		private final Queue<BlockInteraction> interactions;
		
		public Tree(BlockPos start, Queue<BlockInteraction> interactions) {
			this.start = start;
			this.interactions = interactions;
		}
		
		public BlockPos pos() { return this.start; }
		public Queue<BlockInteraction> interactions() { return this.interactions; }
	}
	
	public static void serverTicker(Level level, BlockPos pos, BlockState state, TreeFarmBlockEntity treeFarm) {
		++treeFarm.clockTicks;
		if (treeFarm.clockTicks >= 20) {
			treeFarm.clockTicks = 0;
			treeFarm.purgeEntries();
		}
		
		if (!treeFarm.hasCornersSetUp()) {
			treeFarm.setRunning(false);
		}
		if (treeFarm.searchCooldown > 0) {
			--treeFarm.searchCooldown;
		}
	}
	
}
