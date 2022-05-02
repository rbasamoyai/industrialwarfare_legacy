package rbasamoyai.industrialwarfare.common.items;

import java.util.List;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
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
	public void inventoryTick(ItemStack stack, World level, Entity entity, int slot, boolean selected) {
		if (level.isClientSide) return;
		if (isLit(stack) && (selected || entity instanceof LivingEntity && ((LivingEntity) entity).getOffhandItem() == stack)) {
			((ServerWorld) level).sendParticles(ParticleTypes.SMOKE, entity.getX(), entity.getY() + 1.0d, entity.getZ(), 1, 0.0d, 0.0d, 0.0d, 0.01d);
		}
	}
	
	@Override
	public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
		if (!entity.level.isClientSide) {
			if (isLit(stack)) {
				((ServerWorld) entity.level).sendParticles(ParticleTypes.SMOKE, entity.getX(), entity.getY() + 1.0d, entity.getZ(), 1, 0.0d, 0.0d, 0.0d, 0.01d);
			}
		}
		return false;
	}
	
	@Override
	public boolean isFoil(ItemStack stack) {
		return true;
	}
	
	private static final ITextComponent TOOLTIP_TEXT =
			new TranslationTextComponent("tooltip." + IndustrialWarfare.MOD_ID + ".infinite_match_cord.tooltip")
					.withStyle(TextFormatting.ITALIC, TextFormatting.GOLD);
	
	@Override
	public void appendHoverText(ItemStack stack, World level, List<ITextComponent> tooltip, ITooltipFlag flag) {
		tooltip.add(TOOLTIP_TEXT);
	}
	
}
