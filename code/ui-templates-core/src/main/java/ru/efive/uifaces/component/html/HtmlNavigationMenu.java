package ru.efive.uifaces.component.html;

import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponentBase;
import ru.efive.uifaces.component.ComponentFamily;

/**
 * <p>Represents a container for navigation menu items.</p>
 * 
 * <p>By default, the <code>rendererType</code> property is set to <code>"ru.efive.uifaces.NavigationMenu"</code>.
 * This value can be changed by calling the <code>setRendererType()</code> method.</p>
 * 
 * @author Denis Kotegov
 */
@FacesComponent("ru.efive.uifaces.NavigationMenu")
public class HtmlNavigationMenu extends UIComponentBase {
    
    /** <code>Renderer</code> type for component */ 
    public static final String RENDERER_TYPE = "ru.efive.uifaces.NavigationMenu";
    
    /** Default constructor. */
    public HtmlNavigationMenu() {
        setRendererType(RENDERER_TYPE);
    }

    @Override
    public String getFamily() {
        return ComponentFamily.NAVIGATION_MENU;
    }
        
}
