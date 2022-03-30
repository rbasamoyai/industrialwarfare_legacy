package rbasamoyai.industrialwarfare.core.init;

import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.entityai.formation.FormationAttackType;

public class FormationAttackTypeInit {

	public static final DeferredRegister<FormationAttackType> FORMATION_ATTACK_TYPES = DeferredRegister.create(FormationAttackType.class, IndustrialWarfare.MOD_ID);
	
	public static final RegistryObject<FormationAttackType> NO_ATTACK = FORMATION_ATTACK_TYPES.register("no_attack", () -> new FormationAttackType("no_attack"));
	public static final RegistryObject<FormationAttackType> FIRE_AT_WILL = FORMATION_ATTACK_TYPES.register("fire_at_will", () -> new FormationAttackType("fire_at_will"));
	
}
