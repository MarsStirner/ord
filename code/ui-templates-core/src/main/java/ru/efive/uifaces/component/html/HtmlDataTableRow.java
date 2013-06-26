package ru.efive.uifaces.component.html;

import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;
import ru.efive.uifaces.component.ComponentFamily;

/**
 *
 * @author Denis Kotegov
 */
@FacesComponent("ru.efive.uifaces.DataTableRow")
public class HtmlDataTableRow extends UIComponentBase {

    public static final String FACET_GROUP_PREFIX = "fullRowLevel";

    public static final String FACET_GROUP_DEFAULT = "fullRowDefault";

    public static enum PropertyKeys {
        group, level, collapsed;
    }

    // ----------------------------------------------------------------------------------------------------------------

    @Override
    public String getFamily() {
        return ComponentFamily.DATA_TABLE;
    }

    // ----------------------------------------------------------------------------------------------------------------

    /**
     * Returns grouping line facet for given level.
     * 
     * @param level 0-based level of the group.
     * @return facet for group.
     */
    public UIComponent getFacetForGroup(int level) {
        UIComponent facet = getFacet(FACET_GROUP_PREFIX + level);
        return facet == null? getDefaultFacetForGroup(): facet;
    }

    public UIComponent getDefaultFacetForGroup() {
        return getFacet(FACET_GROUP_DEFAULT);
    }

    // ----------------------------------------------------------------------------------------------------------------

    public Object getGroup() {
        return getStateHelper().eval(PropertyKeys.group);
    }

    public void setGroup(Object group) {
        getStateHelper().put(PropertyKeys.group, group);
    }

    public Object getLevel() {
        return getStateHelper().eval(PropertyKeys.level);
    }

    public void setLevel(Object level) {
        getStateHelper().put(PropertyKeys.level, level);
    }

    public boolean isCollapsed() {
        return (Boolean) getStateHelper().eval(PropertyKeys.collapsed, false);
    }

    public void setCollapsed(boolean collapsed) {
        getStateHelper().put(PropertyKeys.collapsed, collapsed);
    }
}
