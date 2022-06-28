package rbasamoyai.industrialwarfare.core.init;

import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.entityai.formation.FormationAttackType;
import rbasamoyai.industrialwarfare.core.IWModRegistries;

public class FormationAttackTypeInit {

	public static final DeferredRegister<FormationAttackType> FORMATION_ATTACK_TYPES = DeferredRegister.create(IWModRegistries.KEY_FORMATION_ATTACK_TYPES, IndustrialWarfare.MOD_ID);
	
	public static final RegistryObject<FormationAttackType> NO_ATTACK = FORMATION_ATTACK_TYPES.register("no_attack", () -> new FormationAttackType("no_attack"));
	public static final RegistryObject<FormationAttackType> FIRE_AT_WILL = FORMATION_ATTACK_TYPES.register("fire_at_will", () -> new FormationAttackType("fire_at_will"));
	public static final RegistryObject<FormationAttackType> FIRE_BY_RANK = FORMATION_ATTACK_TYPES.register("fire_by_rank", () -> new FormationAttackType("fire_by_rank"));
	public static final RegistryObject<FormationAttackType> FIRE_BY_FILE = FORMATION_ATTACK_TYPES.register("fire_by_file", () -> new FormationAttackType("fire_by_file"));
	public static final RegistryObject<FormationAttackType> FIRE_BY_COMPANY = FORMATION_ATTACK_TYPES.register("fire_by_company", () -> new FormationAttackType("fire_by_company"));
	
}
