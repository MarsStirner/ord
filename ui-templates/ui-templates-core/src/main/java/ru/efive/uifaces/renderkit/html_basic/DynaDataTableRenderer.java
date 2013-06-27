package ru.efive.uifaces.renderkit.html_basic;

import java.util.Map;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.render.FacesRenderer;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;
import ru.efive.uifaces.component.ComponentFamily;
import ru.efive.uifaces.component.html.DynaDataTable;
import ru.efive.uifaces.renderkit.html_basic.base.AbstractTableRenderer;
import ru.efive.uifaces.renderkit.html_basic.base.AdvancedResponseWriter;
import ru.efive.uifaces.renderkit.html_basic.base.HtmlAttribute;
import ru.efive.uifaces.renderkit.html_basic.base.HtmlAttributeValue;
import ru.efive.uifaces.renderkit.html_basic.base.HtmlElement;
import static ru.efive.uifaces.renderkit.html_basic.base.AdvancedResponseWriter.writeStyleClass;
import static ru.efive.uifaces.renderkit.html_basic.base.AdvancedResponseWriter.writeStyle;
import static java.lang.String.format;
import static javax.faces.context.PartialViewContext.PARTIAL_EXECUTE_PARAM_NAME;
import static javax.faces.context.PartialViewContext.ALL_PARTIAL_PHASE_CLIENT_IDS;
import static javax.faces.context.PartialViewContext.PARTIAL_RENDER_PARAM_NAME;

/**
 *
 * @author Pavel Porubov
 */
@FacesRenderer(
        renderKitId = "HTML_BASIC",
        rendererType = DynaDataTableRenderer.RENDERER_TYPE,
        componentFamily = ComponentFamily.DATA_TABLE)
@ResourceDependencies({
    @ResourceDependency(name = "jquery.js", target = "head", library = "e5ui/js"),
    @ResourceDependency(name = "jquery.cookie.js", target = "head", library = "e5ui/js"),
    //@ResourceDependency(name = "datatable.js", target = "head", library = "e5ui/js"),
    @ResourceDependency(name = "datatable.css", target = "head", library = "e5ui/css"),
    @ResourceDependency(name = "dynadatatable.js", target = "head", library = "e5ui/js")
})
public class DynaDataTableRenderer extends AbstractTableRenderer {

    /** <code>Renderer</code> type for component */
    public static final String RENDERER_TYPE = "ru.efive.uifaces.DynaDataTable";

    public static final Iterable<HtmlAttribute> ATTRIBUTES_TO_PASS_THRU = Collections.unmodifiableList(Arrays.asList(
            HtmlAttribute.BORDER, HtmlAttribute.CELLPADDING, HtmlAttribute.CELLSPACING, HtmlAttribute.WIDTH));

    @Override
    protected Iterable<HtmlAttribute> getAttributesToPassThru() {
        return ATTRIBUTES_TO_PASS_THRU;
    }

    private DynaDataTable tbl;
    private String id;
    private AbstractDocumentListHolderBean bean;
    private boolean sel;
    private boolean partial;
    private int wOffset;
    private int wSize;
    private static final int wSizeScale = 5;

    private void setTbl(UIComponent component) {
        tbl = (DynaDataTable) component;
        id = tbl.getClientId();
        bean = (AbstractDocumentListHolderBean) tbl.getAttributes().get("bean");
    }

    @Override
    protected void encodeBegin(AdvancedResponseWriter writer) throws IOException {
        setTbl(writer.getComponent());

        int pg = 10;
        Object pageSize = tbl.getAttributes().get("pageSize");
        if (pageSize != null) {
            if (pageSize instanceof Number) {
                pg = ((Number) pageSize).intValue();
            } else {
                pg = Integer.parseInt((String) pageSize);
            }
        }
        wSize = pg;
        pg *= wSizeScale;

        if (bean.getPagination() == null || bean.getPagination().getPageSize() != pg) {
            int first = bean.getPagination() != null ? bean.getPagination().getOffset() : 0;
            bean.changePageSize(pg);
            first = first + wOffset;
            wOffset = first % pg;
            first -= wOffset;
            int rowCount = bean.getPagination().getTotalCount();
            if (first + pg > rowCount) {
                int nf = rowCount - pg;
                wOffset += first - nf;
                first = nf;
                if (wOffset + wSize > pg) wOffset = pg - wSize;
            }
            bean.changeOffset(first);
        }

        super.encodeBegin(writer);
    }

    @Override
    protected void encodeComponentStart(AdvancedResponseWriter writer) throws IOException {
        if (!partial) {
            writer.startElement(HtmlElement.TABLE); //frame
            writer.writeAttribute(HtmlAttribute.BORDER, "0", null);
            writer.writeAttribute(HtmlAttribute.CELLPADDING, "0", null);
            writer.writeAttribute(HtmlAttribute.CELLSPACING, "0", null);
            writer.startElement(HtmlElement.TD);
            writeStyle(null, writer, "width: 100%;");
        }

        writer.startElement(HtmlElement.DIV); //data
        writer.writeAttribute(HtmlAttribute.ID, id, null);
        writeStyle(null, writer, "overflow: hidden; overflow-x: auto;");

        if (wSize == 0) {
            wSize = bean.getPagination().getPageSize() / wSizeScale;
        }
        int first = bean.getPagination().getOffset(), rows = bean.getPagination().getPageSize(),
                rowCount = bean.getPagination().getTotalCount();

        String iid;
        writer.startElement(HtmlElement.INPUT);
        iid = id + "-first";
        writer.writeAttribute(HtmlAttribute.ID, iid, null);
        writer.writeAttribute(HtmlAttribute.NAME, iid, null);
        writer.writeAttribute(HtmlAttribute.TYPE, HtmlAttributeValue.TYPE_HIDDEN, null);
        writer.writeAttribute(HtmlAttribute.VALUE, first, null);
        writer.endElement(HtmlElement.INPUT);
        writer.startElement(HtmlElement.INPUT);
        iid = id + "-rows";
        writer.writeAttribute(HtmlAttribute.ID, iid, null);
        writer.writeAttribute(HtmlAttribute.NAME, iid, null);
        writer.writeAttribute(HtmlAttribute.TYPE, HtmlAttributeValue.TYPE_HIDDEN, null);
        writer.writeAttribute(HtmlAttribute.VALUE, rows, null);
        writer.endElement(HtmlElement.INPUT);
        writer.startElement(HtmlElement.INPUT);
        iid = id + "-rowCount";
        writer.writeAttribute(HtmlAttribute.ID, iid, null);
        writer.writeAttribute(HtmlAttribute.NAME, iid, null);
        writer.writeAttribute(HtmlAttribute.TYPE, HtmlAttributeValue.TYPE_HIDDEN, null);
        writer.writeAttribute(HtmlAttribute.VALUE, rowCount, null);
        writer.endElement(HtmlElement.INPUT);
        writer.startElement(HtmlElement.INPUT);
        iid = id + "-inSelector";
        writer.writeAttribute(HtmlAttribute.ID, iid, null);
        writer.writeAttribute(HtmlAttribute.NAME, iid, null);
        writer.writeAttribute(HtmlAttribute.TYPE, HtmlAttributeValue.TYPE_HIDDEN, null);
        writer.writeAttribute(HtmlAttribute.VALUE, sel, null);
        writer.endElement(HtmlElement.INPUT);
        writer.startElement(HtmlElement.INPUT);
        iid = id + "-wOffset";
        writer.writeAttribute(HtmlAttribute.ID, iid, null);
        writer.writeAttribute(HtmlAttribute.NAME, iid, null);
        writer.writeAttribute(HtmlAttribute.TYPE, HtmlAttributeValue.TYPE_HIDDEN, null);
        writer.writeAttribute(HtmlAttribute.VALUE, wOffset, null);
        writer.endElement(HtmlElement.INPUT);
        writer.startElement(HtmlElement.INPUT);
        iid = id + "-wSize";
        writer.writeAttribute(HtmlAttribute.ID, iid, null);
        writer.writeAttribute(HtmlAttribute.NAME, iid, null);
        writer.writeAttribute(HtmlAttribute.TYPE, HtmlAttributeValue.TYPE_HIDDEN, null);
        writer.writeAttribute(HtmlAttribute.VALUE, wSize, null);
        writer.endElement(HtmlElement.INPUT);

        writer.startElement(HtmlElement.TABLE);
        writeStyleClass(tbl, writer, AbstractTableRenderer.STYLE_CLASS);
        writeStyle(tbl, writer);
        writer.passThruAttributes(getAttributesToPassThru());
    }

    @Override
    protected void encodeComponentEnd(AdvancedResponseWriter writer) throws IOException {
        int first = bean.getPagination().getOffset(), rows = bean.getPagination().getPageSize(),
                rowCount = bean.getPagination().getTotalCount();

        String msca = format("e5ui_dynaDataTable.%s({ti: \"%s\", first: %d, rows: %d, rowCount: %d, selector: %b, wOffset: %d, wSize: %d});",
                "%s", id, first, rows, rowCount, sel, wOffset, wSize);

        wOffset = 0;
        wSize = 0;

        writer.endElement(HtmlElement.TABLE);

        if (partial) {
            writer.startElement(HtmlElement.SCRIPT);
            writer.writeAttribute(HtmlAttribute.TYPE, HtmlAttributeValue.TYPE_TEXT_JAVASCRIPT, null);
            writer.writeText(format(msca, "resize"), null);
            writer.endElement(HtmlElement.SCRIPT);
        }

        writer.endElement(HtmlElement.DIV); //data

        if (!partial) {
            writer.endElement(HtmlElement.TD);
            writer.startElement(HtmlElement.TD);

            writer.startElement(HtmlElement.A); //selector
            writer.writeAttribute(HtmlAttribute.ID, id + "-selector", null);
            writer.writeAttribute(HtmlAttribute.HREF, "#", null);
            writer.writeAttribute(HtmlAttribute.TABINDEX, "-1", null);
            writeStyle(null, writer, "width: 1px; height: 1px;");
            writer.endElement(HtmlElement.A); //selector

            writer.startElement(HtmlElement.DIV); //scroller
            writer.writeAttribute(HtmlAttribute.ID, id + "-scroller", null);
            writeStyle(null, writer, "overflow: scroll; overflow-x: hidden;");

            writer.startElement(HtmlElement.DIV); //scroller-size
            writer.writeAttribute(HtmlAttribute.ID, id + "-scroller-size", null);
            writeStyle(null, writer, "width: 1px;");
            writer.endElement(HtmlElement.DIV); //scroller-size

            writer.endElement(HtmlElement.DIV); //scroller

            writer.endElement(HtmlElement.TD);
            writer.endElement(HtmlElement.TABLE); //frame

            writer.startElement(HtmlElement.SCRIPT);
            writer.writeAttribute(HtmlAttribute.TYPE, HtmlAttributeValue.TYPE_TEXT_JAVASCRIPT, null);
            writer.writeText(format(msca, "makeup"), null);
            writer.endElement(HtmlElement.SCRIPT);
        }

        partial = false;
    }

    @Override
    public void decode(FacesContext context, UIComponent component) {
        super.decode(context, component);

        setTbl(component);
        sel = false;

        Map<String, String> requestParameterMap = context.getExternalContext().getRequestParameterMap();
        boolean event = false;
        String execute = requestParameterMap.get(PARTIAL_EXECUTE_PARAM_NAME);
        if (execute != null && !execute.isEmpty()) {
            event = execute.contains(ALL_PARTIAL_PHASE_CLIENT_IDS);
            if (!event) {
                String fuId = tbl.getClientId();
                for (String ex : execute.split("\\s+")) {
                    if (fuId.equals(ex)) {
                        event = true;
                        break;
                    }
                }
            }
        } else event = true;
        if (event) {
            String iid;
            iid = id + "-first";
            int first;
            if (requestParameterMap.containsKey(iid)) {
                first = Integer.parseInt(requestParameterMap.get(iid));
                if (first != bean.getPagination().getOffset()) {
                    bean.changeOffset(first);
                }
            } else first = bean.getPagination().getOffset();
            iid = id + "-rows";
            int rows;
            if (requestParameterMap.containsKey(iid)) {
                rows = Integer.parseInt(requestParameterMap.get(iid));
                if (rows != bean.getPagination().getPageSize()) {
                    bean.changePageSize(rows);
                }
            } else rows = bean.getPagination().getPageSize();
            iid = id + "-inSelector";
            sel = requestParameterMap.containsKey(iid) ? Boolean.parseBoolean(requestParameterMap.get(iid)) : false;
            iid = id + "-wOffset";
            wOffset = requestParameterMap.containsKey(iid) ? Integer.parseInt(requestParameterMap.get(iid)) : 0;
            iid = id + "-wSize";
            wSize = requestParameterMap.containsKey(iid) ? Integer.parseInt(requestParameterMap.get(iid)) : 0;
        }
        partial = requestParameterMap.containsKey(PARTIAL_RENDER_PARAM_NAME);
    }
}
