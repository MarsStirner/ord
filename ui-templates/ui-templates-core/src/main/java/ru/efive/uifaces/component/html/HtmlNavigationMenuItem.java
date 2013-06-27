package ru.efive.uifaces.component.html;

import javax.faces.component.FacesComponent;

/**
 * <p>Represents a container for navigation menu item.</p>
 * 
 * <p>By default, the <code>rendererType</code> property is set to <code>"ru.efive.uifaces.NavigationMenuItem"</code>.
 * This value can be changed by calling the <code>setRendererType()</code> method.</p>
 * 
 * @author Denis Kotegov
 */
@FacesComponent("ru.efive.uifaces.NavigationMenuItem")
public class HtmlNavigationMenuItem extends AbstractNavigationMenuItem {
    
    public static final String RENDERER_TYPE = "ru.efive.uifaces.NavigationMenuItem";
    
    public HtmlNavigationMenuItem() {
        setRendererType(RENDERER_TYPE);
    }
    
}
