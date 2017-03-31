package ru.efive.uifaces.component.html;

import ru.efive.uifaces.component.ComponentFamily;

import javax.faces.component.UIOutput;

/**
 * Abstract base class for navigation menu items.
 *
 * @author Denis Kotegov
 */
public abstract class AbstractNavigationMenuItem extends UIOutput {

    protected AbstractNavigationMenuItem() {
        super();
    }

    @Override
    public String getFamily() {
        return ComponentFamily.NAVIGATION_MENU;
    }

    public boolean isCollapsable() {
        return (java.lang.Boolean) getStateHelper().eval(PropertyKeys.collapsable, false);

    }

    public void setCollapsable(boolean collapsable) {
        getStateHelper().put(PropertyKeys.collapsable, collapsable);
    }

    public boolean isCollapsed() {
        return (java.lang.Boolean) getStateHelper().eval(PropertyKeys.collapsed, false);

    }

    public void setCollapsed(boolean collapsed) {
        getStateHelper().put(PropertyKeys.collapsed, collapsed);
    }

    public enum PropertyKeys {
        collapsable,
        collapsed
    }
}
