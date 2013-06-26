package ru.efive.uifaces.renderkit.html_basic;

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.servlet.http.Cookie;
import ru.efive.uifaces.component.html.AbstractNavigationMenuItem;
import ru.efive.uifaces.component.html.HtmlNavigationMenu;
import ru.efive.uifaces.renderkit.html_basic.base.AdvancedResponseWriter;
import ru.efive.uifaces.renderkit.html_basic.base.ComponentAttribute;
import ru.efive.uifaces.renderkit.html_basic.base.ComponentEvaluator;
import ru.efive.uifaces.renderkit.html_basic.base.HtmlAttribute;
import ru.efive.uifaces.renderkit.html_basic.base.HtmlBasicRenderer;
import ru.efive.uifaces.renderkit.html_basic.base.HtmlElement;

import static java.lang.String.format;
import static ru.efive.uifaces.renderkit.html_basic.base.AdvancedResponseWriter.getCookie;

/**
 *
 * @author Denis Kotegov
 */
public abstract class AbstractNavigationMenuItemRenderer extends HtmlBasicRenderer {

    public static final String CSS_COMPONENT = "e5ui-menuItem";
    
    public static final String CSS_COMPONENT_CONTENT = "e5ui-menuItem-content";
    
    public static final String CSS_COMPONENT_CHILDREN = "e5ui-menuItem-children";
    
    public static final String FACET_LABEL = "label";
    
    public static final String COLLAPSED_COOKIE = "e5ui-menuItem-collapsed-%s";
    public static final String CSS_COLLAPSED_MARKER_LEFT = "e5ui-menuItem-collapsed-left";
    public static final String CSS_COLLAPSED_MARKER_RIGHT = "e5ui-menuItem-collapsed-right";
    public static final String CSS_COLLAPSABLE_EMPTY = "empty";
    
    private static final String TOGGLE_COLLAPSED_ONCLICK = "e5ui_menu.toggleCollapsed('%s');";
    private static final String CSS_TABLE =
            "border-width: 0px; border-spacing: 0px; border-collapse: collapse; table-layout: auto;";

    private static final ComponentEvaluator<Integer> CE_LEVEL = new ComponentEvaluator<Integer>() {
        @Override
        public Integer evaluate(FacesContext context, UIComponent component, Object... params) {
            int level = 0;

            while (component != null && !(component instanceof HtmlNavigationMenu)) {
                component = component.getParent();

                if (component instanceof AbstractNavigationMenuItem) {
                    level++;
                }
            }

            return level;
        }
    };
    
    protected abstract boolean encodeContent(AdvancedResponseWriter writer) throws IOException;
    
    protected abstract void encodeHref(AdvancedResponseWriter writer) throws IOException;
    
    protected abstract void encodeCurrent(AdvancedResponseWriter writer) throws IOException;

    protected abstract void encodeSelected(AdvancedResponseWriter writer) throws IOException;

    @Override
    protected boolean shouldEncodeIdAttribute(FacesContext context, UIComponent component) throws IOException {
        return true;
    }

    private boolean hasCookie(String cn) {
        Cookie c = getCookie(format(COLLAPSED_COOKIE, cn));
        return c != null && !"null".equals(c.getValue());
    }

    @Override
    protected void encodeBegin(AdvancedResponseWriter writer) throws IOException {
        AbstractNavigationMenuItem component = (AbstractNavigationMenuItem) writer.getComponent();

        writer.startElement(HtmlElement.DIV); //frame
        encodeIdAttributeIfShould(writer);
        writer.writeComponentAttribute(HtmlAttribute.STYLE, ComponentAttribute.STYLE, null);
        boolean collapsed = component.isCollapsed() ^ hasCookie(component.getClientId()),
                collepsable = component.isCollapsable();
        writer.writeEvaluatedAttribute(HtmlAttribute.CLASS, CE_CSS_CLASS, null, CSS_COMPONENT,
                collepsable ? AbstractNavigationMenuItem.PropertyKeys.collapsable.name() : null,
                collapsed ? AbstractNavigationMenuItem.PropertyKeys.collapsed.name() : null,
                collepsable && component.getChildCount() == 0 ? CSS_COLLAPSABLE_EMPTY : null);
        writer.writeEvaluatedAttribute(HtmlAttribute.E5UI_LEVEL, CE_LEVEL, null);
        encodeCurrent(writer);
        encodeSelected(writer);

        writer.startElement(HtmlElement.DIV); //content
        writer.writeAttribute(HtmlAttribute.CLASS, CSS_COMPONENT_CONTENT, null);

        if (collepsable) {
            writer.startElement(HtmlElement.TABLE);
            writer.writeAttribute(HtmlAttribute.STYLE, CSS_TABLE, null);
            writer.startElement(HtmlElement.TR);
        }

        String toggleCollapsedOnclick = null;
        if (collepsable) {
            toggleCollapsedOnclick = format(TOGGLE_COLLAPSED_ONCLICK, component.getClientId());
            writer.startElement(HtmlElement.TD);
            writer.writeAttribute(HtmlAttribute.CLASS, CSS_COLLAPSED_MARKER_LEFT, null);
            writer.writeAttribute(HtmlAttribute.ONCLICK, toggleCollapsedOnclick, null);
            writer.endElement(HtmlElement.TD);
        }

        if (collepsable) {
            writer.startElement(HtmlElement.TD);
        }
        if (!encodeContent(writer)) {
            writer.startElement(HtmlElement.A);
            writer.writeComponentAttribute(HtmlAttribute.TARGET, ComponentAttribute.TARGET, null);
            encodeHref(writer);

            if (!writer.writeFacet(FACET_LABEL)) {
                writer.writeAttributeText(ComponentAttribute.LABEL, null);
            }

            writer.endElement(HtmlElement.A);
        }
        if (collepsable) {
            writer.endElement(HtmlElement.TD);
        }

        if (collepsable) {
            writer.startElement(HtmlElement.TD);
            writer.writeAttribute(HtmlAttribute.CLASS, CSS_COLLAPSED_MARKER_RIGHT, null);
            writer.writeAttribute(HtmlAttribute.ONCLICK, toggleCollapsedOnclick, null);
            writer.endElement(HtmlElement.TD);
        }

        if (collepsable) {
            writer.endElement(HtmlElement.TR);
            writer.endElement(HtmlElement.TABLE);
        }

        writer.endElement(HtmlElement.DIV); //content

        if (component.getChildCount() > 0) {
            writer.startElement(HtmlElement.DIV); //children
            writer.writeAttribute(HtmlAttribute.CLASS, CSS_COMPONENT_CHILDREN, null);
        }
    }

    @Override
    protected void encodeEnd(AdvancedResponseWriter writer) throws IOException {
        if (writer.getComponent().getChildCount() > 0) {
            writer.endElement(HtmlElement.DIV); //children
        }
        writer.endElement(HtmlElement.DIV); //frame
    }

}
