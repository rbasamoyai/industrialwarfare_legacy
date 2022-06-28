package rbasamoyai.industrialwarfare.common.items.taskscroll;

import java.util.Optional;

import javax.annotation.Nullable;

import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import rbasamoyai.industrialwarfare.IndustrialWarfare;

public class ArgWrapper {
	
	private static final String TAG_VALUE = "value";
	private static final String TAG_POS = "pos";
	private static final String TAG_ITEM = "item";
	private static final String TAG_LOC = "id";
	
	public static final ArgWrapper EMPTY = new ArgWrapper(0, null, null, null);

	private final int argNum;
	private final Optional<BlockPos> posOptional;
	private final Optional<ItemStack> itemOptional;
	private final Optional<ResourceLocation> locOptional;
	
	public ArgWrapper(int argNum, @Nullable Optional<BlockPos> posOptional, @Nullable Optional<ItemStack> itemOptional, @Nullable Optional<ResourceLocation> locOptional) {
		this.argNum = argNum;
		this.posOptional = posOptional == null ? Optional.empty() : posOptional;
		this.itemOptional = itemOptional == null ? Optional.empty() : itemOptional;
		this.locOptional = locOptional == null ? Optional.empty() : locOptional;
	}
	
	public ArgWrapper(int argNum) {
		this(argNum, null, null, null);
	}
	
	public ArgWrapper(BlockPos pos) {
		this(0, Optional.ofNullable(pos), null, null);
	}
	
	public ArgWrapper(ItemStack item) {
		this(0, null, Optional.ofNullable(item), null);
	}
	
	public ArgWrapper(ResourceLocation loc) {
		this(0, null, null, Optional.ofNullable(loc));
	}
	
	public int getArgNum() { return this.argNum; }	
	public Optional<BlockPos> getPos() { return this.posOptional; }
	public Optional<ItemStack> getItem() { return this.itemOptional; }
	public Optional<ResourceLocation> getLoc() { return this.locOptional; }
	
	public CompoundTag serializeNBT() {
		CompoundTag tag = new CompoundTag();
		if (this.argNum != 0) tag.putInt(TAG_VALUE, this.argNum);
		this.posOptional.ifPresent(pos -> tag.putIntArray(TAG_POS, new int[] {pos.getX(), pos.getY(), pos.getZ()}));
		this.itemOptional.ifPresent(stack -> tag.put(TAG_ITEM, stack.serializeNBT()));
		this.locOptional.ifPresent(loc -> tag.putString(TAG_LOC, loc.toString()));
		return tag;
	}
	
	public static ArgWrapper fromNBT(CompoundTag nbt) {
		int value = nbt.getInt(TAG_VALUE);
		Optional<BlockPos> posOptional;
		if (nbt.contains(TAG_POS)) {
			int[] pos = nbt.getIntArray(TAG_POS);
			if (pos.length != 3) pos = new int[] {0, 0, 0};
			posOptional = Optional.of(new BlockPos(pos[0], pos[1], pos[2]));
		} else {
			posOptional = Optional.empty();
		}
		Optional<ItemStack> itemOptional = nbt.contains(TAG_ITEM) ? Optional.of(ItemStack.of(nbt.getCompound(TAG_ITEM))) : Optional.empty();
		Optional<ResourceLocation> locOptional = nbt.contains(TAG_LOC) ? Optional.of(new ResourceLocation(nbt.getString(TAG_LOC))) : Optional.empty();
		return new ArgWrapper(value, posOptional, itemOptional, locOptional);
	}
	
	public void toNetwork(FriendlyByteBuf buf) {
		byte flag = 0;
		if (this.argNum != 0) flag |= Flags.NUM;
		if (this.posOptional.isPresent()) flag |= Flags.POS;
		if (this.itemOptional.isPresent()) flag |= Flags.ITEM;
		if (this.locOptional.isPresent()) flag |= Flags.LOC;
		buf.writeByte(flag);
		
		if ((flag & Flags.NUM) != 0) buf.writeVarInt(this.argNum);
		if ((flag & Flags.POS) != 0) buf.writeBlockPos(this.posOptional.orElse(BlockPos.ZERO));
		if ((flag & Flags.ITEM) != 0) buf.writeItem(this.itemOptional.orElse(ItemStack.EMPTY));
		if ((flag & Flags.LOC) != 0) buf.writeResourceLocation(this.locOptional.orElseGet(() -> new ResourceLocation(IndustrialWarfare.MOD_ID, "empty")));
	}
	
	public static ArgWrapper fromNetwork(FriendlyByteBuf buf) {
		byte flag = buf.readByte();
		
		int value = 0;
		BlockPos pos = null;
		ItemStack item = null;
		ResourceLocation loc = null;
		
		if ((flag & Flags.NUM) != 0) value = buf.readVarInt(); 
		if ((flag & Flags.POS) != 0) pos = buf.readBlockPos();
		if ((flag & Flags.ITEM) != 0) item = buf.readItem();
		if ((flag & Flags.LOC) != 0) loc = buf.readResourceLocation();
		
		return new ArgWrapper(value, Optional.ofNullable(pos), Optional.ofNullable(item), Optional.ofNullable(loc));
	}
	
	@Override
	public String toString() {
		return "ArgWrapper["
				+ this.argNum + ", "
				+ this.posOptional.toString() + ", "
				+ this.itemOptional.toString() + ", "
				+ this.locOptional.toString()
				+ "]";
	}
	
	public boolean matches(ArgWrapper other) {
		return this.argNum == other.argNum
				&& this.posOptional.equals(other.posOptional)
				&& this.itemOptional.equals(other.itemOptional)
				&& this.locOptional.equals(other.locOptional);
	}
	
	private static class Flags {
		private static final byte NUM = 1;
		private static final byte POS = 2;
		private static final byte ITEM = 4;
		private static final byte LOC = 8;
	}
	
}
