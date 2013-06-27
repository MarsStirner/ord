package ru.efive.uifaces.renderkit.html_basic;

import java.io.IOException;
import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;
import javax.faces.render.FacesRenderer;
import ru.efive.uifaces.component.ComponentFamily;
import ru.efive.uifaces.component.html.HtmlNavigationMenuItem;
import ru.efive.uifaces.renderkit.html_basic.base.AdvancedResponseWriter;
import ru.efive.uifaces.renderkit.html_basic.base.ComponentAttribute;
import ru.efive.uifaces.renderkit.html_basic.base.HtmlAttribute;

/**
 * Renderer class for {@link HtmlNavigationMenuItem component}
 * 
 * @author Denis Kotegov
 */
@FacesRenderer(
        renderKitId = "HTML_BASIC",
        rendererType = HtmlNavigationMenuItem.RENDERER_TYPE,
        componentFamily = ComponentFamily.NAVIGATION_MENU)
@ResourceDependencies({
        @ResourceDependency(name = "jquery.js", target = "head", library = "e5ui/js"),
        @ResourceDependency(name = "jquery.cookie.js", target = "head", library = "e5ui/js"),
        @ResourceDependency(name = "menu.js", target = "head", library = "e5ui/js"),
        @ResourceDependency(name = "menu.css", target = "head", library = "e5ui/css")})
public class HtmlNavigationMenuItemRenderer extends AbstractNavigationMenuItemRenderer {

    public static final String FACET_CONTENT = "content";

    @Override
    protected void encodeHref(AdvancedResponseWriter writer) throws IOException {
        writer.writeComponentAttribute(HtmlAttribute.HREF, ComponentAttribute.VALUE, CP_VALUE);
    }

    @Override
    protected void encodeCurrent(AdvancedResponseWriter writer) throws IOException {
        writer.writeComponentAttribute(HtmlAttribute.E5UI_CURRENT, ComponentAttribute.CURRENT, null);
    }

    @Override
    protected void encodeSelected(AdvancedResponseWriter writer) throws IOException {
        writer.writeComponentAttribute(HtmlAttribute.E5UI_SELECTED, ComponentAttribute.SELECTED, null);
    }

    @Override
    protected boolean encodeContent(AdvancedResponseWriter writer) throws IOException {
        return writer.writeFacet(FACET_CONTENT);
    }
}
