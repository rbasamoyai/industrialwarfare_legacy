package rbasamoyai.industrialwarfare.core.init;

import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.entityai.NPCComplaint;

public class NPCComplaintInit {
	
	public static final DeferredRegister<NPCComplaint> COMPLAINTS = DeferredRegister.create(NPCComplaint.class, IndustrialWarfare.MOD_ID);
	
	public static final RegistryObject<NPCComplaint> CANT_ACCESS = COMPLAINTS.register("cant_access", NPCComplaint::new);
	public static final RegistryObject<NPCComplaint> CANT_DEPOSIT_ITEM = COMPLAINTS.register("cant_deposit_item", NPCComplaint::new);
	public static final RegistryObject<NPCComplaint> CANT_GET_ITEM = COMPLAINTS.register("cant_get_item", NPCComplaint::new);
	public static final RegistryObject<NPCComplaint> CANT_OPEN = COMPLAINTS.register("cant_open", NPCComplaint::new);
	public static final RegistryObject<NPCComplaint> CANT_UNEQUIP_ITEM = COMPLAINTS.register("cant_unequip_item", NPCComplaint::new);
	public static final RegistryObject<NPCComplaint> CANT_WEAR_ARMOR = COMPLAINTS.register("cant_wear_armor", NPCComplaint::new);
	public static final RegistryObject<NPCComplaint> CLEAR = COMPLAINTS.register("clear", NPCComplaint::new);
	public static final RegistryObject<NPCComplaint> INVALID_WORKSTATION = COMPLAINTS.register("invalid_workstation", NPCComplaint::new);
	public static final RegistryObject<NPCComplaint> INVALID_ORDER = COMPLAINTS.register("invalid_order", NPCComplaint::new);
	public static final RegistryObject<NPCComplaint> NO_DATA_HANDLER = COMPLAINTS.register("no_data_handler", NPCComplaint::new);
	public static final RegistryObject<NPCComplaint> NOTHING_HERE = COMPLAINTS.register("nothing_here", NPCComplaint::new);
	public static final RegistryObject<NPCComplaint> NOT_ENOUGH_SUPPLIES = COMPLAINTS.register("not_enough_supplies", NPCComplaint::new);
	public static final RegistryObject<NPCComplaint> TIME_STOPPED = COMPLAINTS.register("time_stopped", NPCComplaint::new);
	public static final RegistryObject<NPCComplaint> TOO_FAR = COMPLAINTS.register("too_far", NPCComplaint::new);
	public static final RegistryObject<NPCComplaint> WRONG_TOOL = COMPLAINTS.register("wrong_tool", NPCComplaint::new);
	
}
