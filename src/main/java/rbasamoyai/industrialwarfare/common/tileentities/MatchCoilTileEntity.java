package rbasamoyai.industrialwarfare.common.tileentities;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.util.Constants;
import rbasamoyai.industrialwarfare.common.blocks.MatchCoilBlock;
import rbasamoyai.industrialwarfare.common.items.MatchCoilItem;
import rbasamoyai.industrialwarfare.core.init.BlockInit;
import rbasamoyai.industrialwarfare.core.init.TileEntityTypeInit;

public class MatchCoilTileEntity extends TileEntity {

	private static final String TAG_MAX_DAMAGE = "maxDamage";
	private static final String TAG_COIL_DAMAGE = "coilDamage";
	
	private int maxDamage = 1;
	private int coilDamage;
	
	public MatchCoilTileEntity() {
		super(TileEntityTypeInit.MATCH_COIL.get());
	}
	
	public void setMaxDamage(int maxDamage) { this.maxDamage = maxDamage; }
	public int getMaxDamage() { return this.maxDamage; }
	
	public void setCoilDamage(int coilDamage) { this.coilDamage = coilDamage; }
	public int getCoilDamage() { return this.coilDamage; }
	
	@Override
	public void setChanged() {
		double amount = (double) this.coilDamage / (double) MatchCoilItem.DEFAULT_MAX_DAMAGE;
		int state = (int) Math.floor(MathHelper.clamp(amount * 4.0d, 0.0d, 4.0d));
		
		if (state == 4) {
			this.level.setBlock(this.worldPosition, BlockInit.SPOOL.get().defaultBlockState(), Constants.BlockFlags.DEFAULT);
			this.setRemoved();
		} else {
			this.level.setBlock(this.worldPosition, this.getBlockState().setValue(MatchCoilBlock.COIL_AMOUNT, state), Constants.BlockFlags.DEFAULT);
		}
		super.setChanged();
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
		nbt.putInt(TAG_MAX_DAMAGE, this.maxDamage);
		nbt.putInt(TAG_COIL_DAMAGE, this.coilDamage);
		return super.save(nbt);
	}
	
	@Override
	public void load(BlockState state, CompoundNBT nbt) {
		super.load(state, nbt);
		this.maxDamage = nbt.getInt(TAG_MAX_DAMAGE);
		this.coilDamage = nbt.getInt(TAG_COIL_DAMAGE);
	}
	
}
