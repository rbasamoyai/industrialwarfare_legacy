package rbasamoyai.industrialwarfare.core.init;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.DepositAtCommand;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.EquipCommand;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.JumpToCommand;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.MoveToCommand;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.PatrolCommand;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.SwitchOrderCommand;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.TakeFromCommand;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.TaskScrollCommand;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.UnequipCommand;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.WaitForCommand;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.WorkAtCommand;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollItem;

@Mod.EventBusSubscriber(modid = IndustrialWarfare.MOD_ID, bus = Bus.MOD)
public class TaskScrollCommandInit {

	public static final DeferredRegister<TaskScrollCommand> TASK_SCROLL_COMMANDS = DeferredRegister.create(TaskScrollCommand.class, IndustrialWarfare.MOD_ID);
	
	public static final RegistryObject<TaskScrollCommand> MOVE_TO = TASK_SCROLL_COMMANDS.register("move_to", MoveToCommand::new);
	public static final RegistryObject<TaskScrollCommand> TAKE_FROM = TASK_SCROLL_COMMANDS.register("take_from", TakeFromCommand::new);
	public static final RegistryObject<TaskScrollCommand> DEPOSIT_AT = TASK_SCROLL_COMMANDS.register("deposit_at", DepositAtCommand::new);
	public static final RegistryObject<TaskScrollCommand> WAIT_FOR = TASK_SCROLL_COMMANDS.register("wait_for", WaitForCommand::new);
	public static final RegistryObject<TaskScrollCommand> JUMP_TO = TASK_SCROLL_COMMANDS.register("jump_to", JumpToCommand::new);
	public static final RegistryObject<TaskScrollCommand> WORK_AT = TASK_SCROLL_COMMANDS.register("work_at", WorkAtCommand::new);
	public static final RegistryObject<TaskScrollCommand> SWITCH_ORDER = TASK_SCROLL_COMMANDS.register("switch_order", SwitchOrderCommand::new);
	public static final RegistryObject<TaskScrollCommand> EQUIP = TASK_SCROLL_COMMANDS.register("equip", EquipCommand::new);
	public static final RegistryObject<TaskScrollCommand> UNEQUIP = TASK_SCROLL_COMMANDS.register("unequip", UnequipCommand::new);
	public static final RegistryObject<TaskScrollCommand> PATROL = TASK_SCROLL_COMMANDS.register("patrol", PatrolCommand::new);
	
	@SubscribeEvent
	public static void onCommonSetup(FMLCommonSetupEvent event) {
		TaskScrollItem.initValidCommands();
	}
	
}
