package rbasamoyai.industrialwarfare.client.screen.widgets.page;

/**
 * An extension of ScreenPageDecorator that facilitates widget operations such
 * as activation. The methods of this interface cannot be accessed from an
 * IScreenPage object; thus aggregation must be used to access these methods,
 * e.g.:
 * 
 * <pre>
 * {@code
 * IScreenPage page = ...;
 * AbstractWidgetDecorator dec = new WidgetDecorator(page, widget);
 * IScreenPage page1 = new ScreenPageDecorator(dec);
 * 
 * // ...
 * 
 * dec.setActive(...);
 * dec.setVisible(...);
 * }
 * </pre>
 * 
 * Unlike its superclass methods, its methods <em>do not propagate through the
 * class hierarchy.</em>
 * 
 * @author rbasamoyai
 */
public abstract class AbstractWidgetDecorator extends ScreenPageDecorator {

	public AbstractWidgetDecorator(IScreenPage page) {
		super(page);
	}
	
	public abstract void setActive(boolean active);
	public abstract void setVisible(boolean visible);
	
}
