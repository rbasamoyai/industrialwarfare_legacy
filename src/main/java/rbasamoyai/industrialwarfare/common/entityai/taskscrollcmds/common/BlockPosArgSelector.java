package rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.common;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceContext.BlockMode;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.items.taskscroll.ArgSelector;
import rbasamoyai.industrialwarfare.common.items.taskscroll.ArgWrapper;
import rbasamoyai.industrialwarfare.utils.TooltipUtils;

public class BlockPosArgSelector extends ArgSelector<BlockPos> {
	
	private static final String TOOLTIP_TRANSLATION_KEY = "gui." + IndustrialWarfare.MOD_ID + ".task_scroll.tooltip.selector.block_pos";
	
	private static final IFormattableTextComponent TOOLTIP_HEADER = new TranslationTextComponent(TOOLTIP_TRANSLATION_KEY).withStyle(ArgSelector.HEADER_STYLE);
	private static final IFormattableTextComponent TOOLTIP_OLD_POS = new TranslationTextComponent(TOOLTIP_TRANSLATION_KEY + ".old_position");
	private static final IFormattableTextComponent TOOLTIP_CURRENT_POS = new TranslationTextComponent(TOOLTIP_TRANSLATION_KEY + ".standing_at");
	private static final IFormattableTextComponent TOOLTIP_LOOKING_AT = new TranslationTextComponent(TOOLTIP_TRANSLATION_KEY + ".looking_at");
	private static final IFormattableTextComponent TOOLTIP_NOT_LOOKING_AT = new TranslationTextComponent(TOOLTIP_TRANSLATION_KEY + ".not_looking_at").withStyle(ArgSelector.CANNOT_SELECT_STYLE);
	
	public BlockPosArgSelector(PlayerEntity player, BlockPos oldOrderPos) {
		super(getArgs(player, oldOrderPos));
	}
	
	/**
	 * Raytracing code taken from <a href="https://stackoverflow.com/a/66891421">this Stack Overflow answer.</a>
	 */
	private static List<BlockPos> getArgs(PlayerEntity player, BlockPos oldOrderPos) {
		double rayLength = 10.0d;
		
		Vector3d playerRot = player.getLookAngle();
		Vector3d rayPath = playerRot.scale(rayLength);
		
		Vector3d from = player.getEyePosition(0);
		Vector3d to = from.add(rayPath);
		
		RayTraceContext context = new RayTraceContext(from, to, BlockMode.OUTLINE, FluidMode.ANY, null);
		BlockRayTraceResult result = player.level.clip(context);
		
		ArrayList<BlockPos> args = new ArrayList<>();
		args.add(oldOrderPos);
		args.add(player.blockPosition());
		if (result.getType() == RayTraceResult.Type.BLOCK) args.add(result.getBlockPos());
		
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
	public List<ITextComponent> getComponentTooltip() {
		List<ITextComponent> tooltip = new ArrayList<>();
		tooltip.add(TOOLTIP_HEADER);
		
		BlockPos lookingAt = this.possibleArgs.size() >= 3 ? this.possibleArgs.get(2) : null;
		
		IFormattableTextComponent choice0 = TooltipUtils.formatAsStyle(
				formatBlockPos(this.possibleArgs.get(0))
						.append(ArgSelector.SPACER.copy())
						.append(TOOLTIP_OLD_POS.copy()),
				ArgSelector.NOT_SELECTED_STYLE
				);
		IFormattableTextComponent choice1 = TooltipUtils.formatAsStyle( 
				formatBlockPos(this.possibleArgs.get(1))
						.append(ArgSelector.SPACER.copy())
						.append(TOOLTIP_CURRENT_POS.copy()),
				ArgSelector.NOT_SELECTED_STYLE
				);
		IFormattableTextComponent choice2 = lookingAt == null
				? TOOLTIP_NOT_LOOKING_AT.copy()
				: TooltipUtils.formatAsStyle(
						formatBlockPos(lookingAt)
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
	public ITextComponent getTitle() {
		return this.selectedArg != 2 || this.possibleArgs.size() >= 3 ? formatBlockPos(this.possibleArgs.get(this.selectedArg)) : TooltipUtils.NOT_AVAILABLE.copy();
	}
	
	private static StringTextComponent formatBlockPos(BlockPos pos) {
		return new StringTextComponent(pos.getX() + ", " + pos.getY() + ", " + pos.getZ());
	}

}
