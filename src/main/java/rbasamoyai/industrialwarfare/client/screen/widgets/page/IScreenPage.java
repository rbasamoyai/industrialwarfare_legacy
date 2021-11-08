package rbasamoyai.industrialwarfare.client.screen.widgets.page;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;

public interface IScreenPage extends IGuiEventListener {

	default void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {}
	
	public void tick();
	
	public Screen getScreen();
	
	public static Builder builder(IScreenPage page) {
		return new Builder(page);
	}
	
	/**
	 * Builder for creating IScreenPage instances.
	 * <p>
	 * When building an IScreenPage instance you may want to get references to
	 * decorators with methods that are not specified by IScreenPage. The
	 * following example shows how you could do this:
	 * 
	 * <pre>
	 * {@code
	 * IScreenPage page =
	 * 	IScreenPage.builder(new BaseScreenPage())
	 * 	.add(DecoratorA::new)
	 * 	.add(page -> new DecoratorB(page, ...))
	 * 	.add(DecoratorC::new)
	 * 	// ...
	 * 	.build();
	 * 
	 * DecoratorWithMethod pageAtDecorator = new DecoratorWithMethod(page);
	 * 
	 * page = IScreenPage.builder(pageAtDecorator)
	 * 	// ...
	 * 	.build();
	 * 
	 * // ...
	 * 
	 * pageAtDecorator.method(...);
	 * page.render(...);
	 * }
	 * </pre>
	 * 
	 * @author rbasamoyai
	 */
	public static class Builder {
		private final IScreenPage page;
		private final List<Function<IScreenPage, IScreenPage>> decoratorProviders = new LinkedList<>();
		
		public Builder(IScreenPage page) {
			this.page = page;
		}
		
		public Builder add(Function<IScreenPage, IScreenPage> decoratorProvider) {
			this.decoratorProviders.add(decoratorProvider);
			return this;
		}
		
		public IScreenPage build() {
			IScreenPage finalPage = this.page;
			for (Function<IScreenPage, IScreenPage> decoratorProvider : this.decoratorProviders) {
				finalPage = decoratorProvider.apply(finalPage);
			}
			return finalPage;
		}
	}
	
}
