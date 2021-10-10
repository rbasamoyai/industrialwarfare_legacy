package rbasamoyai.industrialwarfare.common.items.taskscroll;

import java.util.Optional;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public class ArgWrapper {
	
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
	
	@Override
	public String toString() {
		return "ArgWrapper["
				+ this.argNum + ", "
				+ this.posOptional.toString() + ", "
				+ this.itemOptional.toString() + ", "
				+ this.locOptional.toString()
				+ "]";
	}
	
}
