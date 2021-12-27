package rbasamoyai.industrialwarfare.core.init.items;

import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.client.items.renderers.FirearmRenderer;
import rbasamoyai.industrialwarfare.common.items.firearms.FirearmItem;
import rbasamoyai.industrialwarfare.common.items.firearms.InternalMagazineRifleItem;
import rbasamoyai.industrialwarfare.core.itemgroup.IWItemGroups;

public class FirearmInit {

	public static final DeferredRegister<Item> FIREARMS = DeferredRegister.create(ForgeRegistries.ITEMS, IndustrialWarfare.MOD_ID);
	
	public static final RegistryObject<Item> VETTERLI = FIREARMS.register("vetterli",
			() -> new InternalMagazineRifleItem(
					new Item.Properties()
							.stacksTo(1)
							.durability(1200)
							.tab(IWItemGroups.TAB_WEAPONS)
							.setISTER(() -> FirearmRenderer::new),
					new FirearmItem.Properties()
							.ammoPredicate(s -> s.getItem() == ItemInit.AMMO_GENERIC.get())
							.baseDamage(10.0f)
							.headshotMultiplier(3.0f)
							.spread(0.1f)
							.hipfireSpread(5.0f)
							.muzzleVelocity(7.5f)
							.horizontalRecoil(e -> 5.0f * (float) e.getRandom().nextGaussian())
							.verticalRecoil(e -> 8.0f + 2.0f * e.getRandom().nextFloat())
							.cooldownTime(10)
							.cycleTime(30)
							.drawTime(20)
							.reloadStartTime(30)
							.reloadTime(20)
							.reloadEndTime(40)
							.projectileRange(80)
							.fovModifier(0.5f),
							12,
							s -> false));
	
}
