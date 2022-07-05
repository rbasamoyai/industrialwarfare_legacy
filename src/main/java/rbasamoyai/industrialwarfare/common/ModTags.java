package rbasamoyai.industrialwarfare.common;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import rbasamoyai.industrialwarfare.IndustrialWarfare;

public class ModTags {

	public static class Entities {
		public static final TagKey<EntityType<?>> LIVESTOCK = bind("livestock");
		
		protected static TagKey<EntityType<?>> bind(String id) {
			return TagKey.create(Registry.ENTITY_TYPE_REGISTRY, new ResourceLocation(IndustrialWarfare.MOD_ID, id));
		}
	}

	public static class Items {
		public static final TagKey<Item> CHEAT_AMMO = create("cheat_ammo");
		public static final TagKey<Item> ENTRENCHING_TOOLS = create("entrenching_tools");
		public static final TagKey<Item> FIREARM_MELEE_ATTACHMENTS = create("attachments/firearm/melee");
		public static final TagKey<Item> FIREARM_OPTIC_ATTACHMENTS = create("attachments/firearm/optic");
		public static final TagKey<Item> FUNGUS = create("fungus");
	
		protected static final TagKey<Item> create(String id) {
			return ItemTags.create(new ResourceLocation(IndustrialWarfare.MOD_ID, id));
		}
	}
	
	public static class Blocks {

		public static final TagKey<Block> SHATTERABLE = bind("shatterable");
		public static final TagKey<Block> IGNORE_WHEN_MINING = bind("ignore_when_mining");
		public static final TagKey<Block> CAN_PLANT_SAPLING = bind("can_plant_sapling");
		public static final TagKey<Block> CAN_PLANT_FUNGUS = bind("can_plant_fungus");
		public static final TagKey<Block> CAN_PLANT_FORESTRY = bind("can_plant_forestry");
		public static final TagKey<Block> FORESTRY_HARVESTABLE = bind("forestry_harvestable");
		public static final TagKey<Block> FUNGUS = bind("fungus");
		public static final TagKey<Block> CLEARABLES = bind("clearables");

		protected static TagKey<Block> bind(String id) {
			return BlockTags.create(new ResourceLocation(IndustrialWarfare.MOD_ID, id));
		}
		
	}
	
}
