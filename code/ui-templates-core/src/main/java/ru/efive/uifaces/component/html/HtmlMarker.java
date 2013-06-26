package ru.efive.uifaces.component.html;

import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponentBase;

import ru.efive.uifaces.component.ComponentFamily;

/**
 * Represents an error marker.
 * 
 * By default, the <code>rendererType</code> property is set to
 * <code>"ru.efive.uifaces.Marker"</code>. This value can be changed by calling
 * the <code>setRendererType()</code> method.
 * 
 * @author Ramil_Habirov
 */
@FacesComponent("ru.efive.uifaces.Marker")
public class HtmlMarker extends UIComponentBase {

    /** <code>Renderer</code> type for component */
    public static final String RENDERER_TYPE = "ru.efive.uifaces.Marker";

    /** Default constructor. */
    public HtmlMarker() {
        setRendererType(RENDERER_TYPE);
    }

    @Override
    public String getFamily() {
        return ComponentFamily.MARKER;
    }
}
