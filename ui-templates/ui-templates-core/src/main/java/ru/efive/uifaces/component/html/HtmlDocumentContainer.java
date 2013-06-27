package ru.efive.uifaces.component.html;

import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponentBase;

import ru.efive.uifaces.component.ComponentFamily;

/**
 * <p>Represents a container for document.</p>
 * 
 * <p>By default, the <code>rendererType</code> property is set to <code>"ru.efive.uifaces.DocumentContainer"</code>.
 * This value can be changed by calling the <code>setRendererType()</code> method.</p>
 * 
 * @author Ramil_Habirov
 */
@FacesComponent("ru.efive.uifaces.DocumentContainer")
public class HtmlDocumentContainer extends UIComponentBase {

    /** <code>Renderer</code> type for component */
    public static final String RENDERER_TYPE = "ru.efive.uifaces.DocumentContainer";

    /** Default constructor. */
    public HtmlDocumentContainer() {
        setRendererType(RENDERER_TYPE);
    }

    @Override
    public String getFamily() {
        return ComponentFamily.DOCUMENT_CONTAINER;
    }
}
