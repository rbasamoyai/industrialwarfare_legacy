package rbasamoyai.industrialwarfare.client.rendering;

import java.util.OptionalDouble;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.RenderType;

public class SelectionRenderType extends RenderType {

	private SelectionRenderType(String name, VertexFormat format, VertexFormat.Mode mode, int bufSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable runPre, Runnable runPost) {
		super(name, format, mode, bufSize, affectsCrumbling, sortOnUpload, runPre, runPost);
	}
	
	private static final LineStateShard SELECTION_LINE = new LineStateShard(OptionalDouble.of(5.0f));
	
	public static final RenderType TYPE =
			create("unit_selection", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.LINES, 256, false, false,
					RenderType.CompositeState.builder()
					.setLineState(SELECTION_LINE)
					.setLayeringState(VIEW_OFFSET_Z_LAYERING)
					.setTransparencyState(NO_TRANSPARENCY)
					.setCullState(NO_CULL)
					.setLightmapState(NO_LIGHTMAP)
					.setWriteMaskState(COLOR_WRITE)
					.createCompositeState(false));

}
