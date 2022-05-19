package rbasamoyai.industrialwarfare.common.blocks;

import java.util.stream.Stream;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.ToolType;

public class SpoolBlock extends Block {

	public static final VoxelShape SHAPE = Stream.of(Block.box(0, 0, 0, 16, 1, 16), Block.box(5, 1, 5, 11, 15, 11), Block.box(0, 15, 0, 16, 16, 16))
			.reduce((a, b) -> VoxelShapes.joinUnoptimized(a, b, IBooleanFunction.OR)).get();
	
	public SpoolBlock() {
		super(AbstractBlock.Properties.of(Material.WOOL, MaterialColor.WOOD).strength(1.0f).harvestTool(ToolType.AXE).noOcclusion());
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext context) {
		return SHAPE;
	}
	
}
