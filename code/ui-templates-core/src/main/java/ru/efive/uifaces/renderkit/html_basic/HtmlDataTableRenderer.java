package ru.efive.uifaces.renderkit.html_basic;

import java.util.Arrays;
import java.util.Collections;
import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;
import javax.faces.render.FacesRenderer;
import ru.efive.uifaces.component.ComponentFamily;
import ru.efive.uifaces.component.html.HtmlDataTable;
import ru.efive.uifaces.renderkit.html_basic.base.AbstractTableRenderer;
import ru.efive.uifaces.renderkit.html_basic.base.HtmlAttribute;

/**
 *
 * @author Denis Kotegov
 */
@FacesRenderer(
        renderKitId = "HTML_BASIC",
        rendererType = HtmlDataTable.RENDERER_TYPE,
        componentFamily = ComponentFamily.DATA_TABLE)
@ResourceDependencies({
        @ResourceDependency(name = "jquery.js", target = "head", library = "e5ui/js"),
        @ResourceDependency(name = "datatable.js", target = "head", library = "e5ui/js"),
        @ResourceDependency(name = "datatable.css", target = "head", library = "e5ui/css")})
public class HtmlDataTableRenderer extends AbstractTableRenderer {

    public static final Iterable<HtmlAttribute> ATTRIBUTES_TO_PASS_THRU = Collections.unmodifiableList(Arrays.asList(
            HtmlAttribute.BORDER, HtmlAttribute.CELLPADDING, HtmlAttribute.CELLSPACING, HtmlAttribute.WIDTH));
    
    @Override
    protected Iterable<HtmlAttribute> getAttributesToPassThru() {
        return ATTRIBUTES_TO_PASS_THRU;
    }
    
}
