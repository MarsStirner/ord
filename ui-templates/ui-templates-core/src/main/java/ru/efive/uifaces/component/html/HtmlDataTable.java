package ru.efive.uifaces.component.html;

import javax.faces.component.FacesComponent;
import javax.faces.component.UIData;
import ru.efive.uifaces.component.ComponentFamily;
import ru.efive.uifaces.util.ConverterUtils;

/**
 *
 * @author Denis Kotegov
 */
@FacesComponent("ru.efive.uifaces.DataTable")
public class HtmlDataTable extends javax.faces.component.html.HtmlDataTable {
    
    private static enum PropertyKeys {
        GROUPING, MAX_LEVEL;
    }

    /** <code>Renderer</code> type for component */
    public static final String RENDERER_TYPE = "ru.efive.uifaces.DataTable";

    /** Default constructor. */
    public HtmlDataTable() {
        setRendererType(RENDERER_TYPE);
    }

    @Override
    public String getFamily() {
        return ComponentFamily.DATA_TABLE;
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
