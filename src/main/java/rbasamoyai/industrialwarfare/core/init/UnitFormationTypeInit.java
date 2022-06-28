package rbasamoyai.industrialwarfare.core.init;

import com.google.common.collect.ImmutableSet;

import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.entityai.formation.UnitFormationType;
import rbasamoyai.industrialwarfare.common.entityai.formation.formations.ColumnFormation;
import rbasamoyai.industrialwarfare.common.entityai.formation.formations.DeferredFollowPointFormation;
import rbasamoyai.industrialwarfare.common.entityai.formation.formations.FollowPointFormation;
import rbasamoyai.industrialwarfare.common.entityai.formation.formations.LineFormation;
import rbasamoyai.industrialwarfare.common.entityai.formation.formations.NoFormation;
import rbasamoyai.industrialwarfare.common.entityai.formation.formations.SegmentFormation;
import rbasamoyai.industrialwarfare.common.entityai.formation.formations.UnitFormation;
import rbasamoyai.industrialwarfare.common.entityai.formation.formations.UnitFormation.Point;
import rbasamoyai.industrialwarfare.common.items.WhistleItem.FormationCategory;
import rbasamoyai.industrialwarfare.core.IWModRegistries;

public class UnitFormationTypeInit {

	public static final DeferredRegister<UnitFormationType<?>> UNIT_FORMATION_TYPES = DeferredRegister.create(IWModRegistries.KEY_UNIT_FORMATION_TYPES, IndustrialWarfare.MOD_ID);
	
	public static final RegistryObject<UnitFormationType<NoFormation>> NO_FORMATION = UNIT_FORMATION_TYPES.register("no_formation",
			() -> new UnitFormationType<>(NoFormation::new, 0xF00BAFFF, FormationCategory.NO_FORMATION, ImmutableSet.of(
					FormationAttackTypeInit.NO_ATTACK.get(),
					FormationAttackTypeInit.FIRE_AT_WILL.get()
					)));
	
	public static final RegistryObject<UnitFormationType<LineFormation>> LINE = UNIT_FORMATION_TYPES.register("line",
			() -> new UnitFormationType<>(LineFormation::new, 0xF00BA000, FormationCategory.LINE, ImmutableSet.of(
					FormationAttackTypeInit.NO_ATTACK.get(),
					FormationAttackTypeInit.FIRE_AT_WILL.get(),
					FormationAttackTypeInit.FIRE_BY_COMPANY.get(),
					FormationAttackTypeInit.FIRE_BY_FILE.get(),
					FormationAttackTypeInit.FIRE_BY_RANK.get()
					)));
	
	public static final RegistryObject<UnitFormationType<ColumnFormation>> COLUMN = UNIT_FORMATION_TYPES.register("column",
			() -> new UnitFormationType<>(ColumnFormation::new, 0xF00BA010, FormationCategory.COLUMN, ImmutableSet.of(
					FormationAttackTypeInit.NO_ATTACK.get(),
					FormationAttackTypeInit.FIRE_AT_WILL.get()
					)));
	
	public static final RegistryObject<UnitFormationType<SegmentFormation>> COLUMN_SEGMENT = UNIT_FORMATION_TYPES.register("column_segment",
			() -> new UnitFormationType<>(SegmentFormation::new, 0xF00BA011, FormationCategory.NO_FORMATION, ImmutableSet.of(
					FormationAttackTypeInit.NO_ATTACK.get(),
					FormationAttackTypeInit.FIRE_AT_WILL.get()
					)));
	
	public static final RegistryObject<UnitFormationType<FollowPointFormation>> POINTS = UNIT_FORMATION_TYPES.register("points",
			() -> new UnitFormationType<>(FollowPointFormation::new, 0xF00BA020, FormationCategory.NO_WHISTLE, ImmutableSet.of(
					FormationAttackTypeInit.NO_ATTACK.get(),
					FormationAttackTypeInit.FIRE_AT_WILL.get()
					)));
	
	public static final RegistryObject<UnitFormationType<DeferredFollowPointFormation>> DEFERRED_FOLLOW = UNIT_FORMATION_TYPES.register("deferred_follow",
			() -> new UnitFormationType<>(DeferredFollowPointFormation::new, 0xF00BA021, FormationCategory.NO_WHISTLE, ImmutableSet.of(
					FormationAttackTypeInit.NO_ATTACK.get(),
					FormationAttackTypeInit.FIRE_AT_WILL.get()
					)));
	
	/* Whistle formations, dimensions are specified in (width)W(depth)D */
	
	public static final RegistryObject<UnitFormationType<UnitFormation>> LINE_10W3D = UNIT_FORMATION_TYPES.register("line_10w3d",
			() -> new UnitFormationType<>(UnitFormationTypeInit::getLine10w3d, -1, FormationCategory.LINE, ImmutableSet.of()));
	
	public static final RegistryObject<UnitFormationType<UnitFormation>> LINE_15W2D = UNIT_FORMATION_TYPES.register("line_15w2d",
			() -> new UnitFormationType<>(UnitFormationTypeInit::getLine15w2d, -1, FormationCategory.LINE, ImmutableSet.of()));
	
	public static final RegistryObject<UnitFormationType<UnitFormation>> COLUMN_4W10D = UNIT_FORMATION_TYPES.register("column_4w10d",
			() -> new UnitFormationType<>(UnitFormationTypeInit::getColumn4w10d, -1, FormationCategory.COLUMN, ImmutableSet.of()));
	
	private static UnitFormation getLine10w3d(UnitFormationType<?> type, int rank) {
		return new DeferredFollowPointFormation.Builder(
						new Point(0, 0),
						new LineFormation(UnitFormationTypeInit.LINE.get(), 0, 10, 3))
				.addRegularPoint(new Point(-4, -4), 0)
				.build();
	}
	
	private static UnitFormation getLine15w2d(UnitFormationType<?> type, int rank) {
		return new DeferredFollowPointFormation.Builder(
						new Point(0, 0),
						new LineFormation(UnitFormationTypeInit.LINE.get(), 0, 15, 2))
				.addRegularPoint(new Point(-5, -3), 0)
				.build();
	}
	
	private static UnitFormation getColumn4w10d(UnitFormationType<?> type, int rank) {
		return new DeferredFollowPointFormation.Builder(
						new Point(0, -2),
						new ColumnFormation(UnitFormationTypeInit.COLUMN.get(), 0, 4, 10))
				.addRegularPoint(new Point(0, 0), 0)
				.build();
	}
	
}
