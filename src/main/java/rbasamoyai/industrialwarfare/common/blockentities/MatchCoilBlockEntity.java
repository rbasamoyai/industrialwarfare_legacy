package rbasamoyai.industrialwarfare.common.blockentities;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import rbasamoyai.industrialwarfare.common.blocks.MatchCoilBlock;
import rbasamoyai.industrialwarfare.common.items.MatchCoilItem;
import rbasamoyai.industrialwarfare.core.init.BlockEntityTypeInit;
import rbasamoyai.industrialwarfare.core.init.BlockInit;

public class MatchCoilBlockEntity extends BlockEntity {

	private static final String TAG_MAX_DAMAGE = "maxDamage";
	private static final String TAG_COIL_DAMAGE = "coilDamage";
	
	private int maxDamage = 1;
	private int coilDamage;
	
	public MatchCoilBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityTypeInit.MATCH_COIL.get(), pos, state);
	}
	
	public void setMaxDamage(int maxDamage) {
		this.maxDamage = maxDamage;
		this.setChanged();
	}
	public int getMaxDamage() { return this.maxDamage; }
	
	public void setCoilDamage(int coilDamage) {
		this.coilDamage = coilDamage;
		this.setChanged();
	}
	public int getCoilDamage() { return this.coilDamage; }
	
	@Override
	public void setChanged() {
		double amount = (double) this.coilDamage / (double) MatchCoilItem.DEFAULT_MAX_DAMAGE;
		int state = (int) Math.floor(Mth.clamp(amount * 4.0d, 0.0d, 4.0d));
		
		if (state == 4) {
			this.level.setBlock(this.worldPosition, BlockInit.SPOOL.get().defaultBlockState(), Block.UPDATE_ALL);
			this.setRemoved();
		} else {
			this.level.setBlock(this.worldPosition, this.getBlockState().setValue(MatchCoilBlock.COIL_AMOUNT, state), Block.UPDATE_ALL);
		}
		super.setChanged();
	}
	
	@Override
	public CompoundTag getUpdateTag() {
		return this.saveWithFullMetadata();
	}
	
	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	protected void saveAdditional(CompoundTag tag) {
		tag.putInt(TAG_MAX_DAMAGE, this.maxDamage);
		tag.putInt(TAG_COIL_DAMAGE, this.coilDamage);
	}
	
	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		this.maxDamage = tag.getInt(TAG_MAX_DAMAGE);
		this.coilDamage = tag.getInt(TAG_COIL_DAMAGE);
	}
	
}
