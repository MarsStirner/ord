package ru.efive.uifaces.renderkit.html_basic;

import java.io.IOException;
import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;
import javax.faces.render.FacesRenderer;
import ru.efive.uifaces.component.ComponentFamily;
import ru.efive.uifaces.component.html.HtmlFacesViewMenuItem;
import ru.efive.uifaces.renderkit.html_basic.base.AdvancedResponseWriter;
import ru.efive.uifaces.renderkit.html_basic.base.ComponentAttribute;
import ru.efive.uifaces.renderkit.html_basic.base.HtmlAttribute;

/**
 *
 * @author Denis Kotegov
 */
@FacesRenderer(
        renderKitId = "HTML_BASIC",
        rendererType = HtmlFacesViewMenuItem.RENDERER_TYPE,
        componentFamily = ComponentFamily.NAVIGATION_MENU)
@ResourceDependencies({
        @ResourceDependency(name = "jquery.js", target = "head", library = "e5ui/js"),
        @ResourceDependency(name = "jquery.cookie.js", target = "head", library = "e5ui/js"),
        @ResourceDependency(name = "menu.js", target = "head", library = "e5ui/js"),
        @ResourceDependency(name = "menu.css", target = "head", library = "e5ui/css")})
public class HtmlFacesViewMenuItemRenderer extends AbstractNavigationMenuItemRenderer {

    @Override
    protected void encodeHref(AdvancedResponseWriter writer) throws IOException {
        Object value = writer.getComponent().getAttributes().get(ComponentAttribute.VALUE);
        String href = writer.getContext().getExternalContext().getRequestContextPath() + value;
        writer.writeAttribute(HtmlAttribute.HREF, href, null);
    }

    @Override
    protected void encodeCurrent(AdvancedResponseWriter writer) throws IOException {
        Object current = writer.getComponent().getAttributes().get(ComponentAttribute.CURRENT);
        writer.writeAttribute(
                HtmlAttribute.E5UI_CURRENT, 
                current instanceof String && writer.getContext().getViewRoot().getViewId().startsWith((String) current), 
                null);
    }

    @Override
    protected void encodeSelected(AdvancedResponseWriter writer) throws IOException {
        Object value = writer.getComponent().getAttributes().get(ComponentAttribute.VALUE);
        writer.writeAttribute(
                HtmlAttribute.E5UI_SELECTED, 
                writer.getContext().getViewRoot().getViewId().equals(value), 
                null);
    }

    @Override
    protected boolean encodeContent(AdvancedResponseWriter writer) throws IOException {
        return false;
    }
}
