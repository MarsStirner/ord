package ru.efive.uifaces.component.html;

import ru.efive.uifaces.component.ComponentFamily;

import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponentBase;

/**
 * Represents an error marker that collects error messages of children components of target.
 * <p>
 * By default, the <code>rendererType</code> property is set to
 * <code>"ru.efive.uifaces.MultiMarker"</code>. This value can be changed by calling
 * the <code>setRendererType()</code> method.
 *
 * @author Ramil_Habirov
 */
@FacesComponent("ru.efive.uifaces.MultiMarker")
public class HtmlMultiMarker extends UIComponentBase {

    /**
     * <code>Renderer</code> type for component
     */
    public static final String RENDERER_TYPE = "ru.efive.uifaces.MultiMarker";

    /**
     * Default constructor.
     */
    public HtmlMultiMarker() {
        setRendererType(RENDERER_TYPE);
    }

    @Override
    public String getFamily() {
        return ComponentFamily.MULTI_MARKER;
    }

    public DisplayMode getDisplayMode() {
        return (DisplayMode) getStateHelper().eval(PropertyKeys.displayMode, DisplayMode.hint);
    }

    public void setDisplayMode(DisplayMode displayMode) {
        getStateHelper().put(PropertyKeys.displayMode, displayMode);
    }

    public enum PropertyKeys {
        displayMode
    }

    public enum DisplayMode {
        hint, text, window
    }
}
