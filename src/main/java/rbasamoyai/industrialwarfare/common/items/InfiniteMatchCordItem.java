package rbasamoyai.industrialwarfare.common.items;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import rbasamoyai.industrialwarfare.IndustrialWarfare;

public class InfiniteMatchCordItem extends MatchCordItem {

	public InfiniteMatchCordItem() {
		super(new Item.Properties().rarity(Rarity.EPIC));
	}
	
	@Override
	public boolean isDamageable(ItemStack stack) {
		return false;
	}
	
	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
		if (level.isClientSide) return;
		if (isLit(stack) && (selected || entity instanceof LivingEntity && ((LivingEntity) entity).getOffhandItem() == stack)) {
			((ServerLevel) level).sendParticles(ParticleTypes.SMOKE, entity.getX(), entity.getY() + 1.0d, entity.getZ(), 1, 0.0d, 0.0d, 0.0d, 0.01d);
		}
	}
	
	@Override
	public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
		if (!entity.level.isClientSide) {
			if (isLit(stack)) {
				((ServerLevel) entity.level).sendParticles(ParticleTypes.SMOKE, entity.getX(), entity.getY() + 1.0d, entity.getZ(), 1, 0.0d, 0.0d, 0.0d, 0.01d);
			}
		}
		return false;
	}
	
	@Override
	public boolean isFoil(ItemStack stack) {
		return true;
	}
	
	private static final Component TOOLTIP_TEXT =
			new TranslatableComponent("tooltip." + IndustrialWarfare.MOD_ID + ".infinite_match_cord.tooltip")
					.withStyle(ChatFormatting.ITALIC, ChatFormatting.GOLD);
	
	@Override
	public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(TOOLTIP_TEXT);
	}
	
}
