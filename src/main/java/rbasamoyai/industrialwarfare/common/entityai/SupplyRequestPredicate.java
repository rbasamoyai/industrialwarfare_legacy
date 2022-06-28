package rbasamoyai.industrialwarfare.common.entityai;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.common.collect.Streams;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.TierSortingRegistry;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.registries.ForgeRegistries;

public class SupplyRequestPredicate {
	
	public static final SupplyRequestPredicate ANY = new SupplyRequestPredicate();
	
	@Nullable
	private final TagKey<Item> tag;
	@Nullable
	private final Item item;
	private final IntBound count;
	@Nullable
	private final BlockState blockState;
	@Nullable
	private final ToolAction action;
	@Nullable
	private final Tier toolTier;
	
	private int hash = 0;
	
	public SupplyRequestPredicate() {
		this.tag = null;
		this.item = null;
		this.count = IntBound.ANY;
		this.blockState = null;
		this.action = null;
		this.toolTier = null;
	}
	
	public SupplyRequestPredicate(@Nullable TagKey<Item> tag, @Nullable Item item, IntBound count, @Nullable BlockState toBreak, @Nullable ToolAction action, @Nullable Tier toolTier) {
		this.tag = tag;
		this.item = item;
		this.count = count;
		this.blockState = toBreak;
		this.action = action;
		this.toolTier = toolTier;
	}
	
	public static SupplyRequestPredicate canBreak(BlockState state) {
		return new SupplyRequestPredicate(null, null, IntBound.ANY, state, null, null);
	}
	
	public static SupplyRequestPredicate forItem(Item item, IntBound count) {
		return new SupplyRequestPredicate(null, item, count, null, null, null);
	}
	
	public static SupplyRequestPredicate forItem(TagKey<Item> tag, IntBound count) {
		return new SupplyRequestPredicate(tag, null, count, null, null, null);
	}
	
	public static SupplyRequestPredicate forTool(ToolAction action, Tier atLeastTier) {
		return new SupplyRequestPredicate(null, null, IntBound.ANY, null, action, atLeastTier);
	}
	
	public boolean matches(ItemStack stack) {
		if (this.tag != null && !stack.is(this.tag)) return false;
		if (this.item != null && !stack.is(this.item)) return false;
		if (!this.count.matches(stack.getCount())) return false;
		if (this.blockState != null && !stack.isCorrectToolForDrops(this.blockState)) return false;
		return this.action == null || stack.canPerformAction(this.action);
	}
	
	@SuppressWarnings("deprecation")
	public List<ItemStack> getItemsForDisplay() {
		if (this.tag != null) {
			return Streams.stream(Registry.ITEM.getTagOrEmpty(this.tag))
					.map(Holder::value)
					.map(ItemStack::new)
					.collect(Collectors.toList());
		}
		if (this.item != null) return Arrays.asList(new ItemStack(this.item));
		if (this.blockState != null) {
			return ForgeRegistries.ITEMS.getValues()
					.stream()
					.map(ItemStack::new)
					.filter(s -> s.isCorrectToolForDrops(this.blockState))
					.collect(Collectors.toList());
		}
		if (this.action != null) {
			return ForgeRegistries.ITEMS.getValues()
					.stream()
					.map(ItemStack::new)
					.filter(s -> s.getItem().canPerformAction(s, this.action))
					.collect(Collectors.toList());
		}
		return new ArrayList<>();
	}
	
	public int getMinCount(int noMinimum) {
		return this.count.getMin() == null ? noMinimum : this.count.getMin();
	}
	
	public int getMaxCount(int noMaximum) {
		return this.count.getMax() == null ? noMaximum : this.count.getMax();
	}
	
	@SuppressWarnings("deprecation")
	public void toNetwork(FriendlyByteBuf buf) {
		buf.writeBoolean(this.tag != null);
		if (this.tag != null) {
			buf.writeResourceLocation(this.tag.location());
		}
		buf.writeBoolean(this.item != null);
		if (this.item != null) {
			buf.writeRegistryIdUnsafe(ForgeRegistries.ITEMS, this.item);
		}
		this.count.write(buf);
		buf.writeBoolean(this.blockState != null);
		if (this.blockState != null) {
			buf.writeVarInt(Block.BLOCK_STATE_REGISTRY.getId(this.blockState));
		}
		buf.writeBoolean(this.action != null);
		if (this.action != null) {
			buf.writeUtf(this.action.name());
		}
		buf.writeBoolean(this.toolTier != null);
		if (this.toolTier != null) {
			buf.writeResourceLocation(TierSortingRegistry.getName(this.toolTier));
		}
	}
	
	public CompoundTag serializeNBT() {
		CompoundTag tag = new CompoundTag();
		if (this.tag != null) {
			tag.putString("tag", this.tag.location().toString());
		}
		if (this.item != null) {
			tag.putString("item", this.item.getRegistryName().toString());
		}
		tag.put("count", this.count.write(new CompoundTag()));
		if (this.action != null) {
			tag.putString("toolAction", this.action.name());
		}
		if (this.toolTier != null) {
			tag.putString("toolTier", TierSortingRegistry.getName(this.toolTier).toString());
		}
		return tag;
	}
	
	@SuppressWarnings("deprecation")
	public static SupplyRequestPredicate fromNetwork(FriendlyByteBuf buf) {
		TagKey<Item> tag = buf.readBoolean() ? ForgeRegistries.ITEMS.tags().createTagKey(buf.readResourceLocation()) : null;
		Item item = buf.readBoolean() ? buf.readRegistryIdUnsafe(ForgeRegistries.ITEMS) : null;
		IntBound count = IntBound.readBound(buf);
		BlockState toBreak = buf.readBoolean() ? Block.BLOCK_STATE_REGISTRY.byId(buf.readVarInt()) : null;
		ToolAction action = buf.readBoolean() ? ToolAction.get(buf.readUtf()) : null;
		Tier tier = buf.readBoolean() ? TierSortingRegistry.byName(buf.readResourceLocation()) : null;
		return new SupplyRequestPredicate(tag, item, count, toBreak, action, tier);
	}
	
	public static SupplyRequestPredicate fromNBT(CompoundTag nbt) {
		TagKey<Item> tag = nbt.contains("tag") ? ForgeRegistries.ITEMS.tags().createTagKey(new ResourceLocation(nbt.getString("tag"))) : null;
		Item item = nbt.contains("item") ? ForgeRegistries.ITEMS.getValue(new ResourceLocation(nbt.getString("count"))) : null;
		IntBound count = IntBound.readBound(nbt.getCompound("count"));
		ToolAction action = nbt.contains("toolAction") ? ToolAction.get(nbt.getString("toolAction")) : null;
		Tier tier = nbt.contains("toolTier") ? TierSortingRegistry.byName(new ResourceLocation(nbt.getString("toolTier"))) : null;
		return new SupplyRequestPredicate(tag, item, count, null, action, tier);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof SupplyRequestPredicate)) return false;
		SupplyRequestPredicate pred = (SupplyRequestPredicate) obj;
		return this.tag == pred.tag && this.item == pred.item && this.count.equals(pred.count) && this.blockState == pred.blockState && this.action == pred.action && this.toolTier == pred.toolTier;
	}
	
	@Override
	public int hashCode() {
		if (this.hash == 0) {
			if (this.item != null) {
				this.hash ^= this.item.getRegistryName().hashCode() * 64;
			}
			this.hash ^= this.count.hashCode();
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
		
		public static final String TAG_MIN = "min";
		public static final String TAG_MAX = "max";
		
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
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (!(obj instanceof IntBound)) return false;
			IntBound other = (IntBound) obj;
			if (this.min == null ? other.min != null : !this.min.equals(other.min)) return false;
			return this.max == null ? other.max == null : this.max.equals(other.max);
		}
		
		public void write(FriendlyByteBuf buf) {
			buf.writeBoolean(this.min != null);
			if (this.min != null) {
				buf.writeVarInt(this.min);
			}
			buf.writeBoolean(this.max != null);
			if (this.max != null) {
				buf.writeVarInt(this.max);
			}
		}
		
		public CompoundTag write(CompoundTag nbt) {
			if (this.min != null) nbt.putInt(TAG_MIN, this.min.intValue());
			if (this.max != null) nbt.putInt(TAG_MAX, this.max.intValue());
			return nbt;
		}
		
		public static IntBound readBound(FriendlyByteBuf buf) {
			Integer min = buf.readBoolean() ? buf.readVarInt() : null;
			Integer max = buf.readBoolean() ? buf.readVarInt() : null;
			return new IntBound(min, max);
		}
		
		public static IntBound readBound(CompoundTag nbt) {
			Integer min = nbt.contains(TAG_MIN) ? nbt.getInt(TAG_MIN) : null;
			Integer max = nbt.contains(TAG_MAX) ? nbt.getInt(TAG_MAX) : null;
			return new IntBound(min, max);
		}
	}
	
}
