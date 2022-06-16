package rbasamoyai.industrialwarfare.common.items.debugitems;

import java.util.function.Supplier;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.common.npcprofessions.NPCProfession;

public class SetProfessionItem extends Item {

	private static final String SET_PROFESSION_KEY = "gui." + IndustrialWarfare.MOD_ID + ".set_profession";
	
	private final Supplier<? extends NPCProfession> professionSup;
	
	public SetProfessionItem(Item.Properties properties, Supplier<? extends NPCProfession> profession) {
		super(properties);
		this.professionSup = profession;
	}
	
	@Override
	public ActionResultType interactLivingEntity(ItemStack stack, PlayerEntity player, LivingEntity entity, Hand hand) {
		if (entity instanceof NPCEntity) {
			((NPCEntity) entity).getDataHandler().ifPresent(h -> {
				NPCProfession profession = this.professionSup.get();
				h.setProfession(profession);
				ResourceLocation registryName = profession.getRegistryName();
				player.displayClientMessage(
						(new TranslationTextComponent(SET_PROFESSION_KEY, entity.getName()))
						.append(new TranslationTextComponent("profession." + registryName.getNamespace() + "." + registryName.getPath())), true);
			});
			return ActionResultType.sidedSuccess(player.level.isClientSide);
		}
		return super.interactLivingEntity(stack, player, entity, hand);
	}
	
}
