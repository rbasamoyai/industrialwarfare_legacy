package rbasamoyai.industrialwarfare.common.items.debugitems;

import java.util.function.Supplier;

import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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
	public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
		if (entity instanceof NPCEntity) {
			((NPCEntity) entity).getDataHandler().ifPresent(h -> {
				NPCProfession profession = this.professionSup.get();
				h.setProfession(profession);
				ResourceLocation registryName = profession.getRegistryName();
				player.displayClientMessage(
						(new TranslatableComponent(SET_PROFESSION_KEY, entity.getName()))
						.append(new TranslatableComponent("profession." + registryName.getNamespace() + "." + registryName.getPath())), true);
			});
			return InteractionResult.sidedSuccess(player.level.isClientSide);
		}
		return super.interactLivingEntity(stack, player, entity, hand);
	}
	
}
