package ru.efive.uifaces.component.html;

import ru.efive.uifaces.component.ComponentFamily;
import ru.efive.uifaces.util.ConverterUtils;

import javax.faces.component.FacesComponent;
import javax.faces.component.html.HtmlColumn;

/**
 * @author Denis Kotegov
 */
@FacesComponent("ru.efive.uifaces.DataTableColumn")
public class HtmlDataTableColumn extends HtmlColumn {

    private transient Integer orderAsInteger = null;

    @Override
    public String getFamily() {
        return ComponentFamily.DATA_TABLE;
    }

    // ----------------------------------------------------------------------------------------------------------------

    public Object getOrder() {
        return getStateHelper().eval(PropertyKeys.ORDER, 0);
    }

    // ----------------------------------------------------------------------------------------------------------------

    public void setOrder(Object order) {
        getStateHelper().put(PropertyKeys.ORDER, order);
    }

    public Integer getOrderAsInteger() {
        if (orderAsInteger == null) {
            orderAsInteger = ConverterUtils.objectAsInteger(getOrder(), 0);
        }

        return orderAsInteger;
    }

    public String getStyle() {
        return (String) getStateHelper().eval(PropertyKeys.style);
    }

    public void setStyle(String style) {
        getStateHelper().put(PropertyKeys.style, style);
    }

    public String getStyleClass() {
        return (String) getStateHelper().eval(PropertyKeys.styleClass);
    }

    public void setStyleClass(String styleClass) {
        getStateHelper().put(PropertyKeys.styleClass, styleClass);
    }

    public String getHeaderStyle() {
        return (String) getStateHelper().eval(PropertyKeys.headerStyle);
    }

    public void setHeaderStyle(String headerStyle) {
        getStateHelper().put(PropertyKeys.headerStyle, headerStyle);
    }

    public String getFooterStyle() {
        return (String) getStateHelper().eval(PropertyKeys.footerStyle);
    }

    public void setFooterStyle(String footerStyle) {
        getStateHelper().put(PropertyKeys.footerStyle, footerStyle);
    }

    private enum PropertyKeys {
        ORDER,
        style,
        styleClass,
        headerStyle,
        footerStyle
    }

}
