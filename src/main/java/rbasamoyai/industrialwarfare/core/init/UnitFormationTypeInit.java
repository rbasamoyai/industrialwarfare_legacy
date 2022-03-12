package rbasamoyai.industrialwarfare.core.init;

import java.util.ArrayList;
import java.util.HashMap;

import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.entityai.formation.UnitFormationType;
import rbasamoyai.industrialwarfare.common.entityai.formation.formations.LineFormation;
import rbasamoyai.industrialwarfare.common.entityai.formation.formations.PointFormation;

public class UnitFormationTypeInit {

	public static final DeferredRegister<UnitFormationType<?>> UNIT_FORMATION_TYPES = DeferredRegister.create(UnitFormationType.CLASS_GENERIC, IndustrialWarfare.MOD_ID);
	
	public static final RegistryObject<UnitFormationType<LineFormation>> LINE = UNIT_FORMATION_TYPES.register("line",
			() -> new UnitFormationType<>(() -> new LineFormation(-1, 0, 0)));
	
	public static final RegistryObject<UnitFormationType<PointFormation>> POINTS = UNIT_FORMATION_TYPES.register("points",
			() -> new UnitFormationType<>(() -> new PointFormation(new HashMap<>(), new ArrayList<>())));
	
}
