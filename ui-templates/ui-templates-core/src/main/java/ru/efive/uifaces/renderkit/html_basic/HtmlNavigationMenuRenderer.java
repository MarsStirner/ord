package ru.efive.uifaces.renderkit.html_basic;

import java.io.IOException;
import javax.faces.render.FacesRenderer;
import ru.efive.uifaces.component.ComponentFamily;
import ru.efive.uifaces.component.html.HtmlNavigationMenu;
import ru.efive.uifaces.renderkit.html_basic.base.AdvancedResponseWriter;
import ru.efive.uifaces.renderkit.html_basic.base.ComponentAttribute;
import ru.efive.uifaces.renderkit.html_basic.base.HtmlAttribute;
import ru.efive.uifaces.renderkit.html_basic.base.HtmlBasicRenderer;
import ru.efive.uifaces.renderkit.html_basic.base.HtmlElement;

/**
 * Renderer class for {@link HtmlNavigationMenu} component class.
 * 
 * @author Denis Kotegov
 */
@FacesRenderer(
        renderKitId="HTML_BASIC", 
        rendererType=HtmlNavigationMenu.RENDERER_TYPE, 
        componentFamily=ComponentFamily.NAVIGATION_MENU)
public class HtmlNavigationMenuRenderer extends HtmlBasicRenderer {

    public static final String FACET_HEADER = "header";

    public static final String FACET_FOOTER = "footer";

    public static final String CSS_COMPONENT = "e5ui-menu";

    @Override
    protected void encodeBegin(AdvancedResponseWriter writer) throws IOException {
        writer.startElement(HtmlElement.DIV);
        encodeIdAttributeIfShould(writer);
        writer.writeComponentAttribute(HtmlAttribute.STYLE, ComponentAttribute.STYLE, null);
        // TODO: think about replace css class by html attribute
        writer.writeEvaluatedAttribute(HtmlAttribute.CLASS, CE_CSS_CLASS, null, CSS_COMPONENT);
        writer.writeFacet(FACET_HEADER);
    }

    @Override
    protected void encodeEnd(AdvancedResponseWriter writer) throws IOException {
        writer.writeFacet(FACET_FOOTER);
        writer.endElement(HtmlElement.DIV);
    }

}
