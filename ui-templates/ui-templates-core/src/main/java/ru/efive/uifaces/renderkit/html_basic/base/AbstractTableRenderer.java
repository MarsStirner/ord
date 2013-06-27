package ru.efive.uifaces.renderkit.html_basic.base;

import java.util.Collection;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.faces.component.UIColumn;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import ru.efive.uifaces.component.html.HtmlDataTable;
import ru.efive.uifaces.component.html.HtmlDataTableColumn;
import ru.efive.uifaces.component.html.HtmlDataTableRow;
import ru.efive.uifaces.util.ConverterUtils;
import static java.lang.String.format;
import static ru.efive.uifaces.renderkit.html_basic.base.AdvancedResponseWriter.writeStyle;
import static ru.efive.uifaces.renderkit.html_basic.base.AdvancedResponseWriter.writeStyleClass;
import static ru.efive.uifaces.renderkit.html_basic.base.AdvancedResponseWriter.passThruAttributes;

/**
 *
 * @author Denis Kotegov
 */
public abstract class AbstractTableRenderer extends HtmlBasicRenderer {
    public static final String SPACE_CLASS = "e5ui-space";
    public static final String MARKER_CLASS = "e5ui-row-marker";
    public static final String COLLAPSED_CLASS = "collapsed";

    public static final String STYLE_CLASS = "e5ui-dataTable";

    public static final String FACET_CAPTION = "caption";

    protected static final int ROW_INDEX_CLEAR = -1;

    protected static final String CALL_TOGGLE_ROW_GROUP = "e5ui_dataTable.toggleRowGroup(%s);";
    protected static final String TOGGLE_ROW_GROUP = format(CALL_TOGGLE_ROW_GROUP, "this");

    protected abstract Iterable<HtmlAttribute> getAttributesToPassThru();

    private Collection<String> collapsedIds;

    @Override
    protected boolean shouldEncodeIdAttribute(FacesContext context, UIComponent component) throws IOException {
        return true;
    }

    protected void encodeComponentStart(AdvancedResponseWriter writer) throws IOException {
        writer.startElement(HtmlElement.TABLE);
        encodeIdAttributeIfShould(writer);
        writer.writeEvaluatedAttribute(HtmlAttribute.CLASS, CE_CSS_CLASS, null, STYLE_CLASS);
        writer.writeComponentAttribute(HtmlAttribute.STYLE, ComponentAttribute.STYLE, null);
        writer.passThruAttributes(getAttributesToPassThru());
    }
    
    protected void encodeComponentEnd(AdvancedResponseWriter writer) throws IOException {
        writer.endElement(HtmlElement.TABLE);
    }
    
    protected void encodeCaption(AdvancedResponseWriter writer, HtmlDataTable dataTable) throws IOException {
        if (dataTable.getFacet(FACET_CAPTION) == null || !dataTable.getFacet(FACET_CAPTION).isRendered()) {
            return;
        }
        
        writer.startElement(HtmlElement.CAPTION);
        writer.writeAttribute(HtmlAttribute.STYLE, dataTable.getCaptionStyle(), null);
        writer.writeAttribute(HtmlAttribute.CLASS, dataTable.getCaptionClass(), null);
        writer.writeFacet(FACET_CAPTION);
        writer.endElement(HtmlElement.CAPTION);
    }

    @Override
    protected void encodeBegin(AdvancedResponseWriter writer) throws IOException {
        collapsedIds = new ArrayList<String>();

        HtmlDataTable dataTable = (HtmlDataTable) writer.getComponent();

        encodeComponentStart(writer);
        encodeCaption(writer, dataTable);
    }

    protected void encodeHeader(AdvancedResponseWriter writer, HtmlDataTable dataTable,
            List<HtmlDataTableColumn> columns, int levelCount) throws IOException {

        boolean columnHeads = false;
        boolean tableHeads = dataTable.getHeader() != null && dataTable.getHeader().isRendered();
        for (HtmlDataTableColumn column : columns) {
            if (column.getHeader() != null && column.getHeader().isRendered()) {
                columnHeads = true;
                break;
            }
        }

        if (!columnHeads && !tableHeads) {
            return;
        }

        writer.startElement(HtmlElement.THEAD);

        if (tableHeads) {
            writer.startElement(HtmlElement.TR);
            writer.startElement(HtmlElement.TH);
            writer.writeAttribute(HtmlAttribute.CLASS, dataTable.getHeaderClass(), null);
            if (columns.size() + levelCount > 1) {
                writer.writeAttribute(HtmlAttribute.COLSPAN, columns.size() + levelCount, null);
            }
            dataTable.getHeader().encodeAll(writer.getContext());
            writer.endElement(HtmlElement.TH);
            writer.endElement(HtmlElement.TR);
        }

        if (columnHeads) {
            writer.startElement(HtmlElement.TR);
            boolean firstColumn = true;
            for (HtmlDataTableColumn column : columns) {
                writer.startElement(HtmlElement.TH);

                if (firstColumn) {
                    if (levelCount > 0) {
                        writer.writeAttribute(HtmlAttribute.COLSPAN, levelCount + 1, null);
                    }
                    firstColumn = false;
                }

                String cssClass = (column.getStyleClass() == null ? "" : column.getStyleClass())
                        + " "
                        + (column.getHeaderClass() == null
                                ? (dataTable.getHeaderClass() == null? "": dataTable.getHeaderClass())
                                : column.getHeaderClass()
                        ).trim();

                if (!cssClass.isEmpty()) {
                    writer.writeAttribute(HtmlAttribute.CLASS, cssClass, null);
                }

                String cssStyle = (column.getStyle() == null? "": column.getStyle()) + " "
                        + (column.getHeaderStyle() == null ? "" : column.getHeaderStyle()).trim();

                if (!cssStyle.isEmpty()) {
                    writer.writeAttribute(HtmlAttribute.STYLE, cssStyle, null);
                }

                if (column.getHeader() != null && column.getHeader().isRendered()) {
                    column.getHeader().encodeAll(writer.getContext());
                }

                writer.endElement(HtmlElement.TH);
            }

            writer.endElement(HtmlElement.TR);
        }

        writer.endElement(HtmlElement.THEAD);
    }

    protected void encodeFooter(AdvancedResponseWriter writer, HtmlDataTable dataTable,
            List<HtmlDataTableColumn> columns, int levelCount) throws IOException {

        boolean columnFooter = false;
        boolean tableFooter = dataTable.getFooter() != null && dataTable.getFooter().isRendered();
        for (HtmlDataTableColumn column : columns) {
            if (column.getFooter() != null && column.getFooter().isRendered()) {
                columnFooter = true;
                break;
            }
        }

        if (!columnFooter && !tableFooter) {
            return;
        }

        writer.startElement(HtmlElement.TFOOT);

        if (columnFooter) {
            writer.startElement(HtmlElement.TR);
            boolean firstColumn = true;
            for (HtmlDataTableColumn column : columns) {
                writer.startElement(HtmlElement.TF);

                if (firstColumn) {
                    if (levelCount > 0) {
                        writer.writeAttribute(HtmlAttribute.COLSPAN, levelCount + 1, null);
                    }
                    firstColumn = false;
                }

                String cssClass = (column.getStyleClass() == null? "": column.getStyleClass())
                        + " "
                        + (column.getFooterClass() == null
                                ? (dataTable.getFooterClass() == null? "": dataTable.getFooterClass())
                                : column.getFooterClass()
                        ).trim();
                if (!cssClass.isEmpty()) {
                    writer.writeAttribute(HtmlAttribute.CLASS, cssClass, null);
                }

                String cssStyle = (column.getStyle() == null? "": column.getStyle()) + " "
                        + (column.getFooterStyle() == null? "": column.getFooterStyle()).trim();

                if (!cssStyle.isEmpty()) {
                    writer.writeAttribute(HtmlAttribute.STYLE, cssStyle, null);
                }

                if (column.getFooter() != null && column.getFooter().isRendered()) {
                    column.getFooter().encodeAll(writer.getContext());
                }
                writer.endElement(HtmlElement.TF);
            }

            writer.endElement(HtmlElement.TR);
        }

        if (tableFooter) {
            writer.startElement(HtmlElement.TR);
            writer.startElement(HtmlElement.TF);
            writer.writeAttribute(HtmlAttribute.CLASS, dataTable.getHeaderClass(), null);
            if (columns.size() + levelCount > 1) {
                writer.writeAttribute(HtmlAttribute.COLSPAN, columns.size() + levelCount, null);
            }
            dataTable.getFooter().encodeAll(writer.getContext());
            writer.endElement(HtmlElement.TF);
            writer.endElement(HtmlElement.TR);
        }

        writer.endElement(HtmlElement.TFOOT);
    }

    protected String encodeRow(AdvancedResponseWriter writer, HtmlDataTable dataTable, HtmlDataTableRow row,
            List<? extends UIColumn> columns, int levelCount, int rowIndex) throws IOException {

        boolean group = false;
        boolean fullRow = false;
        int level = -1;

        if (row != null && levelCount > 0) {
            group = ConverterUtils.objectAsBoolean(row.getAttributes().get("group"), Boolean.FALSE);
            fullRow = ConverterUtils.objectAsBoolean(row.getAttributes().get("fullRow"), Boolean.FALSE);
            level = Math.max(0, Math.min(ConverterUtils.objectAsInteger(row.getAttributes().get("level"), 0), levelCount - 1));
        }

        if (level > -1) {
            writer.writeAttribute(HtmlAttribute.E5UI_LEVEL, level, null);
        }

        String collapsedId = null;

        writer.startElement(HtmlElement.TR);
        String oddClass = rowIndex % 2 == 0 ? "even" : "odd";
        if (row != null) {
            if (group && row.isCollapsed()) {
                collapsedId = row.getClientId() + "-" + rowIndex;
                writer.writeAttribute(HtmlAttribute.ID, collapsedId, null);
            }
            writeStyleClass(row, writer, oddClass);
            writeStyle(row, writer);
            passThruAttributes(row, writer,
                    HtmlAttribute.ONCLICK, HtmlAttribute.ONDBLCLICK, HtmlAttribute.ONKEYDOWN,
                    HtmlAttribute.ONKEYPRESS, HtmlAttribute.ONKEYUP, HtmlAttribute.ONMOUSEDOWN,
                    HtmlAttribute.ONMOUSEMOVE, HtmlAttribute.ONMOUSEOUT, HtmlAttribute.ONMOUSEOVER,
                    HtmlAttribute.ONMOUSEUP);
        } else {
            writeStyleClass(row, writer, oddClass);
        }

        for (int i = 0; i <= level; i++) {
            writer.startElement(HtmlElement.TD);

            String classAttribute = SPACE_CLASS;

            if (i == level && group) {
                classAttribute += " " + MARKER_CLASS;
                writer.writeAttribute(HtmlAttribute.ONCLICK, TOGGLE_ROW_GROUP, null);
            }

            writer.writeAttribute(HtmlAttribute.CLASS, classAttribute, null);

            writer.endElement(HtmlElement.TD);
        }

        if (fullRow) {
            writer.startElement(HtmlElement.TD);
            int colSpan = columns.size() + levelCount - level - 1;
            if (colSpan > 1) {
                writer.writeAttribute(HtmlAttribute.COLSPAN, colSpan, null);
            }
            UIComponent facet = row.getFacetForGroup(level);
            if (facet != null) {
                facet.encodeAll(writer.getContext());
            } else {
                writer.write("&nbsp;");
            }
            writer.endElement(HtmlElement.TD);
        } else {
            boolean first = true;
            for (UIColumn column : columns) {
                writer.startElement(HtmlElement.TD);

                if (first) {
                    int colSpan = levelCount - level;
                    if (colSpan > 1) {
                        writer.writeAttribute(HtmlAttribute.COLSPAN, colSpan, null);
                    }
                    first = false;
                }

                if (column instanceof HtmlDataTableColumn) {
                    HtmlDataTableColumn htmlDataTableColumn = (HtmlDataTableColumn) column;

                    String cssClass = 
                            htmlDataTableColumn.getStyleClass() == null? "": htmlDataTableColumn.getStyleClass().trim();

                    if (!cssClass.isEmpty()) {
                        writer.writeAttribute(HtmlAttribute.CLASS, cssClass, null);
                    }

                    String cssStyle = htmlDataTableColumn.getStyle() == null ? "": htmlDataTableColumn.getStyle();

                    if (!cssStyle.isEmpty()) {
                        writer.writeAttribute(HtmlAttribute.STYLE, cssStyle, null);
                    }
                }

                column.encodeAll(writer.getContext());
                writer.endElement(HtmlElement.TD);
            }
        }

        writer.endElement(HtmlElement.TR);

        return collapsedId;
    }

    @Override
    protected void encodeChildren(AdvancedResponseWriter writer, List<UIComponent> childrens) throws IOException {
        HtmlDataTable dataTable = (HtmlDataTable) writer.getComponent();
        List<HtmlDataTableColumn> columns = getColumnsToRender(childrens);
        HtmlDataTableRow row = getRowToRender(childrens);

        boolean grouping = ConverterUtils.objectAsBoolean(dataTable.getAttributes().get("grouping"), false);
        int levelCount = grouping?  ConverterUtils.objectAsInteger(dataTable.getAttributes().get("levelCount"), 0):0;

        encodeHeader(writer, dataTable, columns, levelCount);

        dataTable.setRowIndex(ROW_INDEX_CLEAR);
        int lastRow = dataTable.getRows() == 0? dataTable.getRowCount() - 1: dataTable.getFirst() + dataTable.getRowCount() - 1;
        for (int rowIndex = dataTable.getFirst(); rowIndex <= lastRow; rowIndex++) {
            dataTable.setRowIndex(rowIndex);
            String collapsedId = encodeRow(writer, dataTable, row, columns, levelCount, rowIndex);
            if (collapsedId != null) {
                collapsedIds.add(collapsedId);
            }
        }

        encodeFooter(writer, dataTable, columns, levelCount);
    }

    @Override
    protected void encodeEnd(AdvancedResponseWriter writer) throws IOException {
        encodeComponentEnd(writer);

        if (collapsedIds.size() > 0) {
            writer.startElement(HtmlElement.SCRIPT);
            writer.writeAttribute(HtmlAttribute.TYPE, HtmlAttributeValue.TYPE_TEXT_JAVASCRIPT, null);
            for (String collapsedId : collapsedIds) {
                writer.writeText(format(CALL_TOGGLE_ROW_GROUP, "document.getElementById('" + collapsedId + "')"), null);
            }
            writer.endElement(HtmlElement.SCRIPT);
        }
    }

    private List<HtmlDataTableColumn> getColumnsToRender(List<UIComponent> childrens) {
        List<HtmlDataTableColumn> result = new ArrayList<HtmlDataTableColumn>();

        for (UIComponent column : childrens) {
            if (column instanceof HtmlDataTableColumn && column.isRendered()) {
                result.add((HtmlDataTableColumn) column);
            }
        }

        Collections.sort(result, new Comparator<HtmlDataTableColumn>() {
            @Override
            public int compare(HtmlDataTableColumn o1, HtmlDataTableColumn o2) {
                Integer key1 = o1.getOrderAsInteger();
                Integer key2 = o2.getOrderAsInteger();

                return key1.compareTo(key2);
            }
        });

        return result;
    }

    private HtmlDataTableRow getRowToRender(List<UIComponent> childrens) {
        for (UIComponent component : childrens) {
            if (component instanceof HtmlDataTableRow && component.isRendered()) {
                return (HtmlDataTableRow) component;
            }
        }

        return null;
    }
}
