package rbasamoyai.industrialwarfare.core.init.items;

import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.items.PartItem;

public class PartItemInit {

	public static final DeferredRegister<Item> PARTS = DeferredRegister.create(ForgeRegistries.ITEMS, IndustrialWarfare.MOD_ID);
	
	public static final RegistryObject<Item> PART_BULLET = PARTS.register("part_bullet", PartItem::new);
	public static final RegistryObject<Item> PART_IRON_WIRE = PARTS.register("part_iron_wire", PartItem::new);
	public static final RegistryObject<Item> PART_SCREW = PARTS.register("part_screw", PartItem::new);
	
}
