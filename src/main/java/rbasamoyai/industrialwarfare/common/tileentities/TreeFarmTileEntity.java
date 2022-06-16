package rbasamoyai.industrialwarfare.common.tileentities;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags.Blocks;
import net.minecraftforge.common.util.Constants;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.entityai.BlockInteraction;
import rbasamoyai.industrialwarfare.common.entityai.SupplyRequestPredicate;
import rbasamoyai.industrialwarfare.common.entityai.SupplyRequestPredicate.IntBound;
import rbasamoyai.industrialwarfare.common.tags.IWBlockTags;
import rbasamoyai.industrialwarfare.common.tags.IWItemTags;
import rbasamoyai.industrialwarfare.core.init.TileEntityTypeInit;

public class TreeFarmTileEntity extends ResourceStationTileEntity implements IConfigurableBounds {

	private static final String TAG_STARTING_CORNER = "startingCorner";
	private static final String TAG_ENDING_CORNER = "endingCorner";
	
	private BlockPos startingCorner;
	private BlockPos endingCorner;
	
	private final Map<LivingEntity, Tree> treeWorkers = new HashMap<>();
	private int searchCooldown = 0;
	
	public TreeFarmTileEntity() {
		this(TileEntityTypeInit.TREE_FARM.get());
	}
	
	public TreeFarmTileEntity(TileEntityType<? extends TreeFarmTileEntity> type) {
		super(type);
	}
	
	@Override
	public void trySettingBounds(@Nullable PlayerEntity player, @Nullable ItemStack stack, BlockPos pos1, BlockPos pos2) {
		if (pos1 == null || pos2 == null) {
			if (player != null) {
				
			}
			return;
		}
		AxisAlignedBB bounds = new AxisAlignedBB(pos1, pos2.offset(1, 1, 1));
		if (bounds.getXsize() > 64 || bounds.getYsize() > 4 || bounds.getZsize() > 64) {
			if (player != null) {
				
			}
			return;
		}
		if (!bounds.inflate(5.0d, 2.0d, 5.0d).contains(Vector3d.atCenterOf(this.worldPosition))) {
			if (player != null) {
				
			}
			return;
		}
		this.startingCorner = new BlockPos(bounds.minX, bounds.minY, bounds.minZ);
		this.endingCorner = new BlockPos(bounds.maxX - 1.0d, bounds.maxY - 1.0d, bounds.maxZ - 1.0d);
		if (player != null) {
			String posString = this.worldPosition.getX() + " " + this.worldPosition.getY() + " " + this.worldPosition.getZ();
			String posString1 = this.startingCorner.getX() + " " + this.startingCorner.getY() + " " + this.startingCorner.getZ();
			String posString2 = this.endingCorner.getX() + " " + this.endingCorner.getY() + " " + this.endingCorner.getZ();
			player.displayClientMessage(new TranslationTextComponent("gui." + IndustrialWarfare.MOD_ID + ".set_bounds", posString, posString1, posString2), true);
		}
		this.setChanged();
	}
	
	@Override
	public boolean isFinished() {
		return !this.hasCornersSetUp();
	}
	
	@Override
	public CompoundNBT save(CompoundNBT nbt) {
		if (this.startingCorner != null && this.endingCorner != null) {
			CompoundNBT start = new CompoundNBT();
			start.putInt("x", this.startingCorner.getX());
			start.putInt("y", this.startingCorner.getY());
			start.putInt("z", this.startingCorner.getZ());
			nbt.put(TAG_STARTING_CORNER, start);
			CompoundNBT end = new CompoundNBT();
			end.putInt("x", this.endingCorner.getX());
			end.putInt("y", this.endingCorner.getY());
			end.putInt("z", this.endingCorner.getZ());
			nbt.put(TAG_ENDING_CORNER, end);
		}
		return super.save(nbt);
	}
	
	@Override
	public void load(BlockState state, CompoundNBT nbt) {
		super.load(state, nbt);
		if (nbt.contains(TAG_STARTING_CORNER, Constants.NBT.TAG_COMPOUND) && nbt.contains(TAG_ENDING_CORNER, Constants.NBT.TAG_COMPOUND)) {
			CompoundNBT start = nbt.getCompound(TAG_STARTING_CORNER);
			BlockPos startPos = new BlockPos(start.getInt("x"), start.getInt("y"), start.getInt("z"));
			CompoundNBT end = nbt.getCompound(TAG_ENDING_CORNER);
			BlockPos endPos = new BlockPos(end.getInt("x"), end.getInt("y"), end.getInt("z"));
			this.trySettingBounds(null, null, startPos, endPos);
		}
	}
	
	@Override
	public void setRunning(boolean running) {
		super.setRunning(running);
		this.searchCooldown = 0;
	}
	
	@Override
	protected void purgeEntries() {
		super.purgeEntries();
		for (LivingEntity worker : this.treeWorkers.keySet()) {
			if (worker.isDeadOrDying()) {
				this.treeWorkers.remove(worker);
			}
		}
	}

	@Nullable
	@Override
	public BlockInteraction getInteraction(LivingEntity entity) {
		if (!this.isRunning() || this.searchCooldown > 0) return null;
		
		if (!this.treeWorkers.containsKey(entity)) {
			Tree result = this.findTree();
			if (result == null) {
				this.searchCooldown = 600;
				return null;
			}
			this.treeWorkers.put(entity, result);
		}
		
		Tree tree = this.treeWorkers.get(entity);
		if (tree.interactions().isEmpty()) {
			return this.getFinalInteraction(tree);
		} else {
			return tree.interactions().poll();
		}
	}
	
	private BlockInteraction getFinalInteraction(Tree tree) {
		BlockPos pos = tree.pos().immutable();
		BlockState stateBelow = this.level.getBlockState(pos.below());
		
		if (stateBelow.is(IWBlockTags.CAN_PLANT_SAPLING)) {
			return BlockInteraction.placeBlockAtAs(
					GlobalPos.of(this.level.dimension(), pos),
					new SupplyRequestPredicate(ItemTags.SAPLINGS, null, IntBound.ANY, null, IntBound.ANY),
					this::placeSapling,
					this::isSapling);
		}
		if (stateBelow.is(IWBlockTags.CAN_PLANT_FUNGUS)) {
			return BlockInteraction.placeBlockAtAs(
					GlobalPos.of(this.level.dimension(), pos),
					new SupplyRequestPredicate(IWItemTags.FUNGUS, null, IntBound.ANY, null, IntBound.ANY),
					this::placeFungus,
					this::isFungus);
		}
		return null;
	}
	
	private Tree findTree() {
		if (!this.hasCornersSetUp()) {
			return null;
		}
		AxisAlignedBB box = new AxisAlignedBB(this.startingCorner, this.endingCorner);
		return BlockPos.betweenClosedStream(box)
				.filter(p -> this.level.getBlockState(p).is(BlockTags.LOGS))
				.filter(p -> !this.level.getBlockState(p.below()).is(Blocks.COBBLESTONE))
				.filter(p -> !this.level.getBlockState(p.below()).is(BlockTags.LOGS))
				.filter(p -> !this.hasTreeAt(p))
				.findAny()
				.map(this::addTreeInteractions)
				.orElse(null);
	}
	
	private Tree addTreeInteractions(BlockPos pos) {
		VoxelShape box = VoxelShapes.create(new AxisAlignedBB(this.startingCorner, this.endingCorner.offset(1, 32, 1)));
		VoxelShape box1 = VoxelShapes.create(new AxisAlignedBB(pos.offset(-4, -1, -4), pos.offset(5, 32, 5)));
		VoxelShape limit = VoxelShapes.join(box, box1, IBooleanFunction.AND);
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
	
	@SuppressWarnings("deprecation")
	@Override
	protected void findItemsToPickUp() {
		AxisAlignedBB pickupArea = new AxisAlignedBB(this.startingCorner.offset(-5, 0, -5), this.endingCorner.offset(6, 1, 6));
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
	
	@Override
	public void tick() {
		super.tick();
		if (!this.hasCornersSetUp()) {
			this.setRunning(false);
		}
		if (this.searchCooldown > 0) {
			--this.searchCooldown;
		}
	}
	
	private void placeSapling(World level, BlockPos pos, LivingEntity entity) {
		ItemStack stack = entity.getOffhandItem();
		Item item = stack.getItem();
		if (item.is(ItemTags.SAPLINGS) && item instanceof BlockItem) {
			level.setBlockAndUpdate(pos, ((BlockItem) item).getBlock().getStateDefinition().any());
		}
	}
	
	private boolean isSapling(World level, BlockPos pos, LivingEntity entity) {
		if (level.getBlockState(pos).is(BlockTags.SAPLINGS)) {
			this.treeWorkers.remove(entity);
			return true;
		}
		return false;
	}
	
	private void placeFungus(World level, BlockPos pos, LivingEntity entity) {
		ItemStack stack = entity.getOffhandItem();
		Item item = stack.getItem();
		if (item.is(IWItemTags.FUNGUS) && item instanceof BlockItem) {
			level.setBlockAndUpdate(pos, ((BlockItem) item).getBlock().getStateDefinition().any());
		}
	}
	
	private boolean isFungus(World level, BlockPos pos, LivingEntity entity) {
		if (level.getBlockState(pos).is(IWBlockTags.FUNGUS)) {
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
	
}
