package rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.common;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.items.taskscroll.ArgSelector;
import rbasamoyai.industrialwarfare.common.items.taskscroll.ArgWrapper;
import rbasamoyai.industrialwarfare.utils.TooltipUtils;

public class BlockPosArgSelector extends ArgSelector<BlockPos> {
	
	private static final String TOOLTIP_TRANSLATION_KEY = "selector.tooltip." + IndustrialWarfare.MOD_ID + ".block_pos";
	
	private static final MutableComponent TOOLTIP_HEADER = new TranslatableComponent(TOOLTIP_TRANSLATION_KEY).withStyle(ArgSelector.HEADER_STYLE);
	private static final MutableComponent TOOLTIP_OLD_POS = new TranslatableComponent(TOOLTIP_TRANSLATION_KEY + ".old_position");
	private static final MutableComponent TOOLTIP_CURRENT_POS = new TranslatableComponent(TOOLTIP_TRANSLATION_KEY + ".standing_at");
	private static final MutableComponent TOOLTIP_LOOKING_AT = new TranslatableComponent(TOOLTIP_TRANSLATION_KEY + ".looking_at");
	private static final MutableComponent TOOLTIP_NOT_LOOKING_AT = new TranslatableComponent(TOOLTIP_TRANSLATION_KEY + ".not_looking_at").withStyle(ArgSelector.CANNOT_SELECT_STYLE);
	
	public BlockPosArgSelector(Player player, BlockPos oldOrderPos) {
		super(getArgs(player, oldOrderPos));
	}
	
	/**
	 * Raytracing code taken from <a href="https://stackoverflow.com/a/66891421">this Stack Overflow answer.</a>
	 */
	private static List<BlockPos> getArgs(Player player, BlockPos oldOrderPos) {
		double rayLength = 10.0d;
		
		Vec3 playerRot = player.getLookAngle();
		Vec3 rayPath = playerRot.scale(rayLength);
		
		Vec3 from = player.getEyePosition(0);
		Vec3 to = from.add(rayPath);
		
		ClipContext context = new ClipContext(from, to, ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, null);
		BlockHitResult result = player.level.clip(context);
		
		ArrayList<BlockPos> args = new ArrayList<>();
		args.add(oldOrderPos);
		args.add(player.blockPosition());
		if (result.getType() == HitResult.Type.BLOCK) args.add(result.getBlockPos());
		
		return args;
	}
	
	@Override
	public ArgWrapper getSelectedArg() {
		return this.getPossibleArg(this.selectedArg);
	}
	
	@Override
	public ArgWrapper getPossibleArg(int i) {
		return new ArgWrapper(this.possibleArgs.get(i));
	}

	@Override
	public List<Component> getComponentTooltip() {
		List<Component> tooltip = new ArrayList<>();
		tooltip.add(TOOLTIP_HEADER);
		
		BlockPos lookingAt = this.possibleArgs.size() >= 3 ? this.possibleArgs.get(2) : null;
		
		MutableComponent choice0 = TooltipUtils.formatAsStyle(
				new TextComponent(this.possibleArgs.get(0).toShortString())
						.append(ArgSelector.SPACER.copy())
						.append(TOOLTIP_OLD_POS.copy()),
				ArgSelector.NOT_SELECTED_STYLE
				);
		MutableComponent choice1 = TooltipUtils.formatAsStyle( 
				new TextComponent(this.possibleArgs.get(1).toShortString())
						.append(ArgSelector.SPACER.copy())
						.append(TOOLTIP_CURRENT_POS.copy()),
				ArgSelector.NOT_SELECTED_STYLE
				);
		MutableComponent choice2 = lookingAt == null
				? TOOLTIP_NOT_LOOKING_AT.copy()
				: TooltipUtils.formatAsStyle(
						new TextComponent(this.possibleArgs.get(2).toShortString())
								.append(ArgSelector.SPACER.copy())
								.append(TOOLTIP_LOOKING_AT.copy()),
						ArgSelector.NOT_SELECTED_STYLE
						);

		switch (this.selectedArg) {
		case 0: choice0 = ArgSelector.formatAsSelected(choice0); break;
		case 1: choice1 = ArgSelector.formatAsSelected(choice1); break;
		case 2:
			if (lookingAt == null) {
				IndustrialWarfare.LOGGER.warn("Selected the \"looking at\" argument but you are not looking at a nearby block!");
				this.warnInvalidSelection();
			} else choice2 = ArgSelector.formatAsSelected(choice2);
			break;
		default: this.warnInvalidSelection();
		}
		
		tooltip.add(choice0);
		tooltip.add(choice1);
		tooltip.add(choice2);
		
		return tooltip;
	}
	
	@Override
	public Component getTitle() {
		return this.selectedArg != 2 || this.possibleArgs.size() >= 3 ? new TextComponent(this.possibleArgs.get(this.selectedArg).toShortString()) : TooltipUtils.NOT_AVAILABLE.copy();
	}

}
