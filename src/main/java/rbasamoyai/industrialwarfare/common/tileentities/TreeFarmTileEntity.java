package rbasamoyai.industrialwarfare.common.tileentities;

import java.util.Iterator;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
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

	@Nullable
	@Override
	public BlockInteraction getInteraction(LivingEntity entity) {
		if (!this.isRunning()) return null;
		
		for (Map.Entry<BlockPos, LivingEntity> entry : this.currentTasks.entrySet()) {
			if (entry.getValue() == entity) {
				this.posCache.remove(entry.getKey());
				this.currentTasks.remove(entry.getKey());
				break;
			}
		}
		
		if (this.posCache.isEmpty()) {
			this.generateCache();
		}
		if (this.posCache.isEmpty()) return null;
		
		BlockInteraction interaction = null;
		Iterator<BlockInteraction> iter = this.posCache.values().iterator();
		while (iter.hasNext()) {
			BlockInteraction newInteraction = iter.next();
			if (this.currentTasks.keySet().contains(newInteraction.pos().pos())) continue;
			interaction = newInteraction;
			break;
		}
		if (interaction == null) return null;
		this.currentTasks.put(interaction.pos().pos(), entity);
		return interaction;
	}
	
	private void generateCache() {
		if (!this.hasCornersSetUp()) return;
		AxisAlignedBB box = new AxisAlignedBB(this.startingCorner, this.endingCorner);
		BlockPos.betweenClosedStream(box)
		.filter(p -> this.level.getBlockState(p).is(BlockTags.LOGS))
		.filter(p -> !this.level.getBlockState(p.below()).is(Blocks.COBBLESTONE))
		.filter(p -> !this.level.getBlockState(p.below()).is(BlockTags.LOGS))
		.findAny()
		.ifPresent(this::addTreeInteractions);
	}
	
	private void addTreeInteractions(BlockPos pos) {
		BlockState state = this.level.getBlockState(pos);
		BlockState stateBelow = this.level.getBlockState(pos.below());
		BlockPos immutable = pos.immutable();
		
		VoxelShape box = VoxelShapes.create(new AxisAlignedBB(this.startingCorner, this.endingCorner.offset(1, 32, 1)));
		VoxelShape box1 = VoxelShapes.create(new AxisAlignedBB(pos.offset(-5, -1, -5), pos.offset(6, 32, 6)));
		VoxelShape limit = VoxelShapes.join(box, box1, IBooleanFunction.AND);
		if (limit.isEmpty()) return;
		TreeBlockIterator iter = new TreeBlockIterator(this.level, pos, limit.bounds());
		
		iter.findBlockInteractions()
		.stream()
		.sorted((p1, p2) -> this.comparePos(p1.pos().pos(), p2.pos().pos(), immutable))
		.forEach(b -> {
			this.posCache.put(b.pos().pos(), b);
		});
		
		if (state.is(BlockTags.LOGS_THAT_BURN) && stateBelow.is(IWBlockTags.CAN_PLANT_SAPLING)) {
			this.posCache.put(immutable, BlockInteraction.placeBlockAtAs(
					GlobalPos.of(this.level.dimension(), immutable),
					new SupplyRequestPredicate(ItemTags.SAPLINGS, null, IntBound.ANY, null, IntBound.ANY),
					TreeFarmTileEntity::placeSapling,
					TreeFarmTileEntity::isSapling));
		} else if (stateBelow.is(IWBlockTags.CAN_PLANT_FUNGUS)) {
			this.posCache.put(immutable, BlockInteraction.placeBlockAtAs(
					GlobalPos.of(this.level.dimension(), immutable),
					new SupplyRequestPredicate(IWItemTags.FUNGUS, null, IntBound.ANY, null, IntBound.ANY),
					TreeFarmTileEntity::placeFungus,
					TreeFarmTileEntity::isFungus));
		}
		
	}
	
	private int comparePos(BlockPos p1, BlockPos p2, BlockPos src) {
		return Integer.compare(p1.distManhattan(src), p2.distManhattan(src));
	}
	
	private boolean hasCornersSetUp() {
		return this.startingCorner != null && this.endingCorner != null;
	}
	
	@Override
	public void tick() {
		super.tick();
		if (!this.hasCornersSetUp()) {
			this.setRunning(false);
		}
	}
	
	private static void placeSapling(World level, BlockPos pos, LivingEntity entity) {
		ItemStack stack = entity.getOffhandItem();
		Item item = stack.getItem();
		if (item.is(ItemTags.SAPLINGS) && item instanceof BlockItem) {
			level.setBlockAndUpdate(pos, ((BlockItem) item).getBlock().getStateDefinition().any());
		}
	}
	
	private static boolean isSapling(World level, BlockPos pos, LivingEntity entity) {
		return level.getBlockState(pos).is(BlockTags.SAPLINGS);
	}
	
	private static void placeFungus(World level, BlockPos pos, LivingEntity entity) {
		ItemStack stack = entity.getOffhandItem();
		Item item = stack.getItem();
		if (item.is(IWItemTags.FUNGUS) && item instanceof BlockItem) {
			level.setBlockAndUpdate(pos, ((BlockItem) item).getBlock().getStateDefinition().any());
		}
	}
	
	private static boolean isFungus(World level, BlockPos pos, LivingEntity entity) {
		return level.getBlockState(pos).is(IWBlockTags.FUNGUS);
	}
	
}
