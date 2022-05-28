package rbasamoyai.industrialwarfare.common.tileentities;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import rbasamoyai.industrialwarfare.common.blocks.QuarryBlock;
import rbasamoyai.industrialwarfare.common.entityai.BlockInteraction;
import rbasamoyai.industrialwarfare.common.tags.IWBlockTags;
import rbasamoyai.industrialwarfare.core.init.BlockInit;
import rbasamoyai.industrialwarfare.core.init.TileEntityTypeInit;
import rbasamoyai.industrialwarfare.core.init.items.ItemInit;

public class QuarryTileEntity extends TileEntity implements ITickableTileEntity {

	public static final String TAG_BUFFER = "buffer";
	public static final String TAG_SUPPLIES = "supplies";
	public static final String TAG_RUNNING = "running";
	public static final String TAG_Y_LEVEL = "yLevel";
	
	protected final ItemStackHandler buffer = new ItemStackHandler(18);
	protected final ItemStackHandler supplies = new ItemStackHandler(27);
	
	protected final LazyOptional<IItemHandler> bufferOptional = LazyOptional.of(() -> this.buffer);
	protected final LazyOptional<IItemHandler> suppliesOptional = LazyOptional.of(() -> this.supplies);
	
	protected final Map<BlockPos, BlockInteraction> posCache = new LinkedHashMap<>();
	protected final BiMap<BlockPos, LivingEntity> currentTasks = HashBiMap.create();
	protected int clockTicks;
	protected int currentYLevel;
	protected boolean isRunning = true;
	
	public QuarryTileEntity() {
		this(TileEntityTypeInit.QUARRY.get());
	}
	
	public QuarryTileEntity(TileEntityType<? extends QuarryTileEntity> type) {
		super(type);
	}
	
	public void setYLevel(int y) {
		this.currentYLevel = y;
		this.setChanged();
	}
	
	@Override
	public void setRemoved() {
		super.setRemoved();
		this.bufferOptional.invalidate();
		this.suppliesOptional.invalidate();
	}
	
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return side.getAxis().isVertical()
					? this.bufferOptional.cast()
					: this.suppliesOptional.cast();
		}
		return super.getCapability(cap, side);
	}
	
	@Override
	public CompoundNBT getUpdateTag() {
		return this.save(new CompoundNBT());
	}
	
	@Override
	public void handleUpdateTag(BlockState state, CompoundNBT tag) {
		super.handleUpdateTag(state, tag);
	}
	
	@Override
	public SUpdateTileEntityPacket getUpdatePacket() {
		return new SUpdateTileEntityPacket(this.worldPosition, 0, this.save(new CompoundNBT()));
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
		BlockState state = this.level.getBlockState(pkt.getPos());
		this.load(state, pkt.getTag());
	}

	@Override
	public CompoundNBT save(CompoundNBT nbt) {
		nbt.put(TAG_BUFFER, this.buffer.serializeNBT());
		nbt.put(TAG_SUPPLIES, this.supplies.serializeNBT());
		nbt.putBoolean(TAG_RUNNING, this.isRunning);
		nbt.putInt(TAG_Y_LEVEL, this.currentYLevel);
		return super.save(nbt);
	}
	
	@Override
	public void load(BlockState state, CompoundNBT nbt) {
		super.load(state, nbt);
		this.buffer.deserializeNBT(nbt.getCompound(TAG_BUFFER));
		this.supplies.deserializeNBT(nbt.getCompound(TAG_SUPPLIES));
		this.isRunning = nbt.getBoolean(TAG_RUNNING);
		this.currentYLevel = nbt.getInt(TAG_Y_LEVEL);
	}
	
	@Override
	public void tick() {
		++this.clockTicks;
		if (this.clockTicks >= 20) {
			this.clockTicks = 0;
			
			for (Map.Entry<BlockPos, LivingEntity> entry : this.currentTasks.entrySet()) {
				if (entry.getValue().isDeadOrDying()) {
					this.currentTasks.remove(entry.getKey());
				}
			}
		}
	}
	
	@Nullable
	public BlockInteraction getInteraction(LivingEntity entity) {
		if (!this.isRunning) {
			return null;
		}
		
		for (Map.Entry<BlockPos, LivingEntity> entry : this.currentTasks.entrySet()) {
			if (entry.getValue() == entity) {
				this.posCache.remove(entry.getKey());
				this.currentTasks.remove(entry.getKey());
				break;
			}
		}	
		
		if (this.posCache.isEmpty()) {
			this.generateCache(false);
		}
		if (this.posCache.isEmpty()) {
			this.setYLevel(this.currentYLevel - 1);
			if (this.worldPosition.getY() - this.currentYLevel > 16 || this.currentYLevel <= 4 && (this.level.dimension() == World.OVERWORLD || this.level.dimension() == World.NETHER)) {
				this.isRunning = false;
				return null;
			}
			this.generateCache(false);
		}
		
		if (this.posCache.isEmpty()) {
			return null;
		}
		BlockInteraction interaction = null;
		Iterator<BlockInteraction> iter = this.posCache.values().iterator();
		while (iter.hasNext()) {
			BlockInteraction newInteraction = iter.next();
			if (!this.currentTasks.keySet().contains(newInteraction.pos().pos())) {
				interaction = newInteraction;
				break;
			}
		}
		if (interaction == null) return null;
		this.currentTasks.put(interaction.pos().pos(), entity);
		return interaction;
	}
	
	public static final int[] RAMP_START_I_TABLE	= new int[] { 00, 14, 02, 00, 00, 00, 14, 02, 00, 14, 02, 00, 00, 00, 14, 02 };
	public static final int[] RAMP_START_J_TABLE	= new int[] { 00, 00, 14, 02, 00, 14, 02, 00, 00, 00, 14, 02, 00, 14, 02, 00 };
	public static final int[] RAMP_END_I_TABLE		= new int[] { 14, 16, 16, 02, 02, 14, 16, 16, 14, 16, 16, 02, 02, 14, 16, 16 };
	public static final int[] RAMP_END_J_TABLE		= new int[] { 02, 14, 16, 16, 14, 16, 16, 02, 02, 14, 16, 16, 14, 16, 16, 02 };
	
	@SuppressWarnings("deprecation")
	protected void generateCache(boolean clear) {
		if (World.isOutsideBuildHeight(this.currentYLevel)) {
			return;
		}
		
		if (clear) {
			this.posCache.clear();
		}
		
		int startX;
		int startZ;
		int signX;
		int signZ;
		
		Direction direction = this.getBlockState().getValue(QuarryBlock.FACING);	
		
		switch (direction) {
		case WEST:
			startX = this.worldPosition.getX() + 1;
			startZ = this.worldPosition.getZ() - 8;
			signX = 1;
			signZ = 1;
			break;
		case EAST:
			startX = this.worldPosition.getX() - 1;
			startZ = this.worldPosition.getZ() + 8;
			signX = -1;
			signZ = -1;
			break;
		case SOUTH:
			startX = this.worldPosition.getX() - 8;
			startZ = this.worldPosition.getZ() - 1;
			signX = 1;
			signZ = -1;
			break;
		default:
			startX = this.worldPosition.getX() + 8;
			startZ = this.worldPosition.getZ() + 1;
			signX = -1;
			signZ = 1;
			break;
		}
		
		int rampStartI = -1;
		int rampStartJ = -1;
		int rampEndI = -1;
		int rampEndJ = -1;
		
		int currentDepth = this.worldPosition.getY() - this.currentYLevel - 1;
		int rampSide = currentDepth < 0 ? -1 : currentDepth & 3;
		int valueIndex = rampSide | direction.get2DDataValue() << 2;
		
		if (valueIndex != -1) {
			rampStartI = RAMP_START_I_TABLE[valueIndex];
			rampStartJ = RAMP_START_J_TABLE[valueIndex];
			rampEndI = RAMP_END_I_TABLE[valueIndex];
			rampEndJ = RAMP_END_J_TABLE[valueIndex];
		}
		
		for (int i = 0; i < 16; ++i) {
			for (int j = 0; j < 16; ++j) {
				BlockPos pos = new BlockPos(startX + i * signX, this.currentYLevel, startZ + j * signZ);
				BlockState candidate = this.level.getBlockState(pos);
				
				if (candidate.is(IWBlockTags.IGNORE_WHEN_MINING) || candidate.getDestroySpeed(this.level, pos) < 0.0f) {
					continue;
				}
				
				if (rampStartI <= i && i < rampEndI && rampStartJ <= j && j < rampEndJ) {
					this.posCache.put(pos, BlockInteraction.placeBlockAtAs(
							GlobalPos.of(this.level.dimension(), pos),
							ItemInit.WORKER_SUPPORT.get(),
							QuarryTileEntity::placeSupportAction,
							QuarryTileEntity::checkState));
				} else if (!candidate.isAir()) {
					this.posCache.put(pos, BlockInteraction.breakBlockAt(GlobalPos.of(this.level.dimension(), pos)));
				}
			}
		}
	}
	
	public void stopWorking(LivingEntity entity) {
		this.currentTasks.inverse().remove(entity);
	}
	
	public boolean isRunning() {
		return this.isRunning;
	}
	
	public static void placeSupportAction(World level, BlockPos pos) {
		level.setBlock(pos, BlockInit.WORKER_SUPPORT.get().defaultBlockState(), Constants.BlockFlags.DEFAULT);
		SoundType stateSound = level.getBlockState(pos).getSoundType();
		level.playSound(null, pos, stateSound.getPlaceSound(), SoundCategory.NEUTRAL, stateSound.getVolume(), stateSound.getPitch());
	}
	
	public static boolean checkState(World level, BlockPos pos) {
		return level.getBlockState(pos).getBlock() == BlockInit.WORKER_SUPPORT.get();
	}
	
}
