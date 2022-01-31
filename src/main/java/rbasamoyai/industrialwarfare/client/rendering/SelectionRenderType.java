package rbasamoyai.industrialwarfare.client.rendering;

import java.util.OptionalDouble;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;

public class SelectionRenderType extends RenderType {

	private SelectionRenderType(String name, VertexFormat format, int mode, int bufSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable runPre, Runnable runPost) {
		super(name, format, mode, bufSize, affectsCrumbling, sortOnUpload, runPre, runPost);
	}
	
	private static final LineState SELECTION_LINE = new LineState(OptionalDouble.of(5.0f));
	
	public static final RenderType TYPE =
			create("unit_selection", DefaultVertexFormats.POSITION_COLOR, GL11.GL_LINES, 256,
					RenderType.State.builder()
					.setLineState(SELECTION_LINE)
					.setLayeringState(VIEW_OFFSET_Z_LAYERING)
					.setTransparencyState(NO_TRANSPARENCY)
					.setCullState(NO_CULL)
					.setLightmapState(NO_LIGHTMAP)
					.setWriteMaskState(COLOR_WRITE)
					.createCompositeState(false));

}
