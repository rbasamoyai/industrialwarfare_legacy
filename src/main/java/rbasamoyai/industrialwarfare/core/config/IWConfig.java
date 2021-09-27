package rbasamoyai.industrialwarfare.core.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class IWConfig {

	public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
	public static final ForgeConfigSpec SPEC;
	
	// Quality weights
	public static final ForgeConfigSpec.ConfigValue<Float> part_weight;
	public static final ForgeConfigSpec.ConfigValue<Float> skill_weight;
	public static final ForgeConfigSpec.ConfigValue<Float> recipe_weight;
	public static final ForgeConfigSpec.ConfigValue<Integer> workstation_update;
	
	static {
		BUILDER.comment("Server config file for the \"Industrial Warfare\" mod by rbasamoyai");
		
		BUILDER.push("quality weights");
		
		BUILDER.comment("The weight that the quality of the parts have on the output, defaults to 2.0f.");
		part_weight = BUILDER.define("part_weight", 2.0f);
		
		BUILDER.comment("The weight that the crafting entity's skill (random skill if crafted by player) has on the output, defaults to 2.0f.");
		skill_weight = BUILDER.define("skill_weight", 2.0f);
		
		BUILDER.comment("The weight that the recipe item's quality has on the output, defaults to 1.0f.");
		recipe_weight = BUILDER.define("recipe_weight", 1.0f);
		
		BUILDER.pop();
		
		BUILDER.push("workstation config");
		
		BUILDER.comment("How often workstations will update, defaults to every 10 ticks. Should be and must be set to at least 1.");
		BUILDER.comment("It is recommended that this value is set to anywhere between 10 and 20, inclusive.");
		workstation_update = BUILDER.define("workstation_update", 10);
		
		BUILDER.pop();
		
		SPEC = BUILDER.build();
	}
	
}
