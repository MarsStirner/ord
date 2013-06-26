package ru.efive.uifaces.component.html;

import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponentBase;

import ru.efive.uifaces.component.ComponentFamily;

/**
 * Represents an error marker that collects error messages of children components of target.
 * 
 * By default, the <code>rendererType</code> property is set to
 * <code>"ru.efive.uifaces.MultiMarker"</code>. This value can be changed by calling
 * the <code>setRendererType()</code> method.
 * 
 * @author Ramil_Habirov
 */
@FacesComponent("ru.efive.uifaces.MultiMarker")
public class HtmlMultiMarker extends UIComponentBase {

    /** <code>Renderer</code> type for component */
    public static final String RENDERER_TYPE = "ru.efive.uifaces.MultiMarker";

    /** Default constructor. */
    public HtmlMultiMarker() {
        setRendererType(RENDERER_TYPE);
    }

    @Override
    public String getFamily() {
        return ComponentFamily.MULTI_MARKER;
    }

    public enum PropertyKeys {
        displayMode
    }

    public enum DisplayMode {
        hint, text, window
    }

    public DisplayMode getDisplayMode() {
        return (DisplayMode) getStateHelper().eval(PropertyKeys.displayMode, DisplayMode.hint);
    }

    public void setDisplayMode(DisplayMode displayMode) {
        getStateHelper().put(PropertyKeys.displayMode, displayMode);
    }
}
