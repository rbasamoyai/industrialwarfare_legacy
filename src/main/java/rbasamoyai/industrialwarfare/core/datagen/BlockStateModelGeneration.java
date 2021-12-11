package rbasamoyai.industrialwarfare.core.datagen;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile.UncheckedModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.blocks.TaskScrollShelfBlock;
import rbasamoyai.industrialwarfare.core.init.BlockInit;

public class BlockStateModelGeneration extends BlockStateProvider {

	private static final Logger LOGGER = LogManager.getLogger();
	
	public BlockStateModelGeneration(DataGenerator gen, ExistingFileHelper exFileHelper) {
		super(gen, IndustrialWarfare.MOD_ID, exFileHelper);
	}

	@Override
	protected void registerStatesAndModels() {
		LOGGER.debug("Generating block states and models for rbasamoyai's Industrial Warfare mod");
		ItemModelProvider itemModels = itemModels();
		
		getVariantBuilder(BlockInit.ASSEMBLER_WORKSTATION.get())
				.forAllStates(state -> {
					return ConfiguredModel.builder()
							.modelFile(new UncheckedModelFile(new ResourceLocation(IndustrialWarfare.MOD_ID, "block/assembler_workstation")))
							.build();
				});
		itemModels.getBuilder("assembler_workstation")
				.parent(new UncheckedModelFile(new ResourceLocation(IndustrialWarfare.MOD_ID, "block/assembler_workstation")));
		
		getVariantBuilder(BlockInit.TASK_SCROLL_SHELF.get())
				.forAllStates(state -> {
					return ConfiguredModel.builder()
							.modelFile(new UncheckedModelFile(new ResourceLocation(IndustrialWarfare.MOD_ID, "block/task_scroll_shelf")))
							.rotationY(((int) state.getValue(TaskScrollShelfBlock.HORIZONTAL_FACING).toYRot() + 180) % 360)
							.build();
				});
		itemModels.getBuilder("task_scroll_shelf")
				.parent(new UncheckedModelFile(new ResourceLocation(IndustrialWarfare.MOD_ID, "block/task_scroll_shelf")));
		
	}
	
	@Override
	public void run(DirectoryCache cache) throws IOException {
		super.run(cache);
	}
	
	@Override
	public String getName() {
		return "Industrial Warfare block states";
	}

}
