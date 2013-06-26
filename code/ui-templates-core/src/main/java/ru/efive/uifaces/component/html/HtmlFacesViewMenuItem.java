package ru.efive.uifaces.component.html;

import javax.faces.component.FacesComponent;

/**
 * <p>Represents navigation menu item which is links to faces view directly.</p>
 * 
 * <p>By default <code>rendererType</code> property must be set to <code>ru.efive.uifaces.FacesViewMenuItem</code>.
 * This value can be changed by calling the {@link #setRendererType(rendererType)} method.
 * 
 * @author Denis Kotegov
 */
@FacesComponent("ru.efive.uifaces.FacesViewMenuItem")
public class HtmlFacesViewMenuItem extends AbstractNavigationMenuItem {
    
    /** <code>rendererTpe</code> for this component. */
    public static final String RENDERER_TYPE = "ru.efive.uifaces.FacesViewMenuItem";

    /** Default constructor */
    public HtmlFacesViewMenuItem() {
        setRendererType(RENDERER_TYPE);
    }
    
}
