package ru.efive.uifaces.component.html;

import ru.efive.uifaces.component.ComponentFamily;

import javax.faces.component.FacesComponent;

/**
 * @author Denis Kotegov
 */
@FacesComponent("ru.efive.uifaces.DataTable")
public class HtmlDataTable extends javax.faces.component.html.HtmlDataTable {

    /**
     * <code>Renderer</code> type for component
     */
    public static final String RENDERER_TYPE = "ru.efive.uifaces.DataTable";

    /**
     * Default constructor.
     */
    public HtmlDataTable() {
        setRendererType(RENDERER_TYPE);
    }

    @Override
    public String getFamily() {
        return ComponentFamily.DATA_TABLE;
    }

    private enum PropertyKeys {
        GROUPING, MAX_LEVEL
    }

    // ----------------------------------------------------------------------------------------------------------------

//    public Object getGrouping() {
//        return getStateHelper().eval(PropertyKeys.GROUPING);
//    }
//
//    public void setGrouping(Object grouping) {
//        getStateHelper().put(PropertyKeys.GROUPING, grouping);
//    }
//
//    public Object getLevelCount() {
//        return getStateHelper().eval(PropertyKeys.MAX_LEVEL);
//    }
//
//    public void setLevelCount(Object maxLevel) {
//        getStateHelper().put(PropertyKeys.MAX_LEVEL, maxLevel);
//    }

}
