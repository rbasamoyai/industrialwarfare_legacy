package rbasamoyai.industrialwarfare.common.entityai;

import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tags.ITag;
import net.minecraft.tags.TagCollectionManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.registries.ForgeRegistries;

public class SupplyRequestPredicate {
	
	@Nullable
	private final ITag<Item> tag;
	@Nullable
	private final Item item;
	private final IntBound count;
	@Nullable
	private final ToolType toolType;
	private final IntBound harvestLevel;
	
	private int hash = 0;
	
	public SupplyRequestPredicate() {
		this.tag = null;
		this.item = null;
		this.count = IntBound.ANY;
		this.toolType = null;
		this.harvestLevel = IntBound.ANY;
	}
	
	public SupplyRequestPredicate(@Nullable ITag<Item> tag, @Nullable Item item, IntBound count, @Nullable ToolType type, IntBound harvestLevel) {
		this.tag = tag;
		this.item = item;
		this.count = count;
		this.toolType = type;
		this.harvestLevel = harvestLevel;
	}
	
	public static SupplyRequestPredicate canBreak(BlockState state) {
		return forTool(state.getHarvestTool(), IntBound.atLeast(state.getHarvestLevel()));
	}
	
	public static SupplyRequestPredicate forTool(ToolType type, IntBound harvestLevel) {
		return new SupplyRequestPredicate(null, null, IntBound.ANY, type, harvestLevel);
	}
	
	public static SupplyRequestPredicate forItem(Item item, IntBound count) {
		return new SupplyRequestPredicate(null, item, count, null, IntBound.ANY);
	}
	
	public static SupplyRequestPredicate forItem(ITag<Item> tag, IntBound count) {
		return new SupplyRequestPredicate(tag, null, count, null, IntBound.ANY);
	}
	
	public boolean matches(ItemStack stack) {
		if (this.tag != null && !this.tag.contains(stack.getItem())) return false;
		if (this.item != null && this.item != stack.getItem()) return false;
		if (!this.count.matches(stack.getCount())) return false;
		if (this.toolType == null) return true;
		return stack.getToolTypes().contains(this.toolType) && this.harvestLevel.matches(stack.getHarvestLevel(this.toolType, null, null));
	}
	
	public int getMaxCount(int noMaximum) {
		return this.count.getMax() == null ? noMaximum : this.count.getMax();
	}
	
	public void toNetwork(PacketBuffer buf) {
		buf.writeBoolean(this.tag != null);
		if (this.tag != null) {
			buf.writeUtf(TagCollectionManager.getInstance().getItems().getIdOrThrow(this.tag).toString());
		}
		buf.writeBoolean(this.item != null);
		if (this.item != null) {
			buf.writeRegistryIdUnsafe(ForgeRegistries.ITEMS, this.item);
		}
		writeBound(buf, this.count);
		buf.writeBoolean(this.toolType != null);
		if (this.toolType != null) {
			buf.writeUtf(this.toolType.getName());
		}
		writeBound(buf, this.harvestLevel);
	}
	
	public static SupplyRequestPredicate fromNetwork(PacketBuffer buf) {
		ITag<Item> tag = null;
		if (buf.readBoolean()) {
			ResourceLocation loc = new ResourceLocation(buf.readUtf());
			tag = TagCollectionManager.getInstance().getItems().getTag(loc);
		}
		Item item = buf.readBoolean() ? buf.readRegistryIdUnsafe(ForgeRegistries.ITEMS) : null;
		IntBound count = readBound(buf);
		ToolType toolType = buf.readBoolean() ? ToolType.get(buf.readUtf()) : null;
		IntBound harvestLevel = readBound(buf);
		return new SupplyRequestPredicate(tag, item, count, toolType, harvestLevel);
	}
	
	public static void writeBound(PacketBuffer buf, IntBound bound) {
		buf.writeBoolean(bound.getMin() != null);
		if (bound.getMin() != null) {
			buf.writeVarInt(bound.getMin());
		}
		buf.writeBoolean(bound.getMax() != null);
		if (bound.getMax() != null) {
			buf.writeVarInt(bound.getMax());
		}
	}
	
	public static IntBound readBound(PacketBuffer buf) {
		Integer min = buf.readBoolean() ? buf.readVarInt() : null;
		Integer max = buf.readBoolean() ? buf.readVarInt() : null;
		return new IntBound(min, max);
	}
	
	@Override
	public int hashCode() {
		if (this.hash == 0) {
			if (this.tag != null) {
				this.hash ^= TagCollectionManager.getInstance().getItems().getIdOrThrow(this.tag).hashCode();
			}
			if (this.item != null) {
				this.hash ^= this.item.getRegistryName().hashCode() * 64;
			}
			this.hash ^= this.count.hashCode();
			if (this.toolType != null) {
				this.hash ^= this.toolType.getName().hashCode() * 128;
			}
			this.hash ^= this.harvestLevel.hashCode() * 32;
		}
		return this.hash;
	}
	
	/**
	 * A recreation of {@link net.minecraft.advancements.criterion.MinMaxBounds.IntBound} but with less fluff as it does not get serialized to JSON.
	 * 
	 * @author rbasamoyai
	 */
	
	public static class IntBound {
		public static final IntBound ANY = new IntBound(null, null);
		
		private final Integer min;
		private final Integer max;
		
		public IntBound(@Nullable Integer min, @Nullable Integer max) {
			if (min != null && max != null && min.intValue() > max.intValue()) {
				this.max = min;
				this.min = max;
			} else {
				this.min = min;
				this.max = max;
			}
		}
		
		public boolean matches(int value) {
			if (this.min != null && value < this.min) return false;
			return this.max == null || this.max >= value;
		}
		
		public Integer getMin() { return this.min; }
		public Integer getMax() { return this.max; }
		
		@Override
		public int hashCode() {
			return (this.min == null ? 0 : this.min.intValue() * 32) ^ (this.max == null ? 0 : this.max.intValue());
		}
		
		public static IntBound atLeast(int value) { return new IntBound(value, null); }
		public static IntBound exactly(int value) { return new IntBound(value, value); }
	}
	
}
