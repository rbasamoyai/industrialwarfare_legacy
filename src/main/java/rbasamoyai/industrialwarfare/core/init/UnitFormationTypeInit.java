package rbasamoyai.industrialwarfare.core.init;

import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.entityai.formation.UnitFormationType;
import rbasamoyai.industrialwarfare.common.entityai.formation.formations.ColumnFormation;
import rbasamoyai.industrialwarfare.common.entityai.formation.formations.DeferredFollowPointFormation;
import rbasamoyai.industrialwarfare.common.entityai.formation.formations.FollowPointFormation;
import rbasamoyai.industrialwarfare.common.entityai.formation.formations.LineFormation;
import rbasamoyai.industrialwarfare.common.entityai.formation.formations.SegmentFormation;
import rbasamoyai.industrialwarfare.common.entityai.formation.formations.UnitFormation;
import rbasamoyai.industrialwarfare.common.entityai.formation.formations.UnitFormation.Point;

public class UnitFormationTypeInit {

	public static final DeferredRegister<UnitFormationType<?>> UNIT_FORMATION_TYPES = DeferredRegister.create(UnitFormationType.CLASS_GENERIC, IndustrialWarfare.MOD_ID);
	
	public static final RegistryObject<UnitFormationType<LineFormation>> LINE = UNIT_FORMATION_TYPES.register("line",
			() -> new UnitFormationType<>(LineFormation::new, 0xF00BA000));
	
	public static final RegistryObject<UnitFormationType<ColumnFormation>> COLUMN = UNIT_FORMATION_TYPES.register("column",
			() -> new UnitFormationType<>(ColumnFormation::new, 0xF00BA010));
	
	public static final RegistryObject<UnitFormationType<SegmentFormation>> COLUMN_SEGMENT = UNIT_FORMATION_TYPES.register("column_segment",
			() -> new UnitFormationType<>(SegmentFormation::new, 0xF00BA011));
	
	public static final RegistryObject<UnitFormationType<FollowPointFormation>> POINTS = UNIT_FORMATION_TYPES.register("points",
			() -> new UnitFormationType<>(FollowPointFormation::new, 0xF00BA020));
	
	public static final RegistryObject<UnitFormationType<DeferredFollowPointFormation>> DEFERRED_FOLLOW = UNIT_FORMATION_TYPES.register("deferred_follow",
			() -> new UnitFormationType<>(DeferredFollowPointFormation::new, 0xF00BA021));
	
	public static final RegistryObject<UnitFormationType<UnitFormation>> THREE_LINES = UNIT_FORMATION_TYPES.register("three_lines",
			() -> new UnitFormationType<>(type -> new DeferredFollowPointFormation.Builder(
							new Point(0, -2),
							new LineFormation(UnitFormationTypeInit.LINE.get(), 0, 10, 3))
					.addRegularPoint(new Point(0, 0), 0)
					.build(), -1));
	
}
