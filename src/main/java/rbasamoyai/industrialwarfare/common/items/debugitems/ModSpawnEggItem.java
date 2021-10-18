package rbasamoyai.industrialwarfare.common.items.debugitems;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import net.minecraft.block.DispenserBlock;
import net.minecraft.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class ModSpawnEggItem extends SpawnEggItem {

	protected static final List<ModSpawnEggItem> EGGS_TO_ADD = new ArrayList<>();
	
	protected static DefaultDispenseItemBehavior behavior = new DefaultDispenseItemBehavior() {
		@Override
		protected ItemStack execute(IBlockSource source, ItemStack stack) {
			Direction dir = source.getBlockState().getValue(DispenserBlock.FACING);
			EntityType<?> type = ((SpawnEggItem) stack.getItem()).getType(stack.getTag());
			type.spawn(source.getLevel(), stack, null, source.getPos().relative(dir), SpawnReason.DISPENSER, dir != Direction.UP, false);
			stack.shrink(1);
			return stack;
		}
	};
	
	private final Supplier<? extends EntityType<?>> lazyEntityType;
	
	public static void registerSpawnEggs() {
		final Map<EntityType<?>, SpawnEggItem> BY_ID = ObfuscationReflectionHelper.getPrivateValue(SpawnEggItem.class, null, "field_195987_b");
		for (SpawnEggItem item : EGGS_TO_ADD) {
			BY_ID.put(item.getType(null), item);
			DispenserBlock.registerBehavior(item, behavior);
		}
		
		EGGS_TO_ADD.clear();
	}
	
	public ModSpawnEggItem(Supplier<? extends EntityType<?>> type, int mainColor, int spotColor, Item.Properties properties) {
		super(null, mainColor, spotColor, properties);
		this.lazyEntityType = type;
		EGGS_TO_ADD.add(this);
	}
	
	@Override
	public EntityType<?> getType(CompoundNBT tag) {
		return this.lazyEntityType.get();
	}
	
}
