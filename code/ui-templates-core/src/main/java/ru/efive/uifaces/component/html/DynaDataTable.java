package ru.efive.uifaces.component.html;

import javax.faces.component.FacesComponent;
import ru.efive.uifaces.component.ComponentFamily;
import ru.efive.uifaces.renderkit.html_basic.DynaDataTableRenderer;

/**
 *
 * @author Pavel Porubov
 */
@FacesComponent(DynaDataTable.COMPONENT)
public class DynaDataTable extends HtmlDataTable {

    /** Component name */
    public static final String COMPONENT = "ru.efive.uifaces.DynaDataTable";
    
    /** Default constructor. */
    public DynaDataTable() {
        setRendererType(DynaDataTableRenderer.RENDERER_TYPE);
    }

    @Override
    public String getFamily() {
        return ComponentFamily.DATA_TABLE;
    }
}
