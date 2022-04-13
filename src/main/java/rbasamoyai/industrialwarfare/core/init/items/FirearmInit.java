package rbasamoyai.industrialwarfare.core.init.items;

import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.items.firearms.complete.ColtSAAFirearmItem;
import rbasamoyai.industrialwarfare.common.items.firearms.complete.MartiniHenryFirearmItem;
import rbasamoyai.industrialwarfare.common.items.firearms.complete.VetterliFirearmItem;

public class FirearmInit {

	public static final DeferredRegister<Item> FIREARMS = DeferredRegister.create(ForgeRegistries.ITEMS, IndustrialWarfare.MOD_ID);
	
	public static final RegistryObject<Item> VETTERLI = FIREARMS.register("vetterli", () -> new VetterliFirearmItem());
	public static final RegistryObject<Item> MARTINI_HENRY = FIREARMS.register("martini_henry", () -> new MartiniHenryFirearmItem());
	public static final RegistryObject<Item> COLT_SAA = FIREARMS.register("colt_saa", () -> new ColtSAAFirearmItem());
	
}
