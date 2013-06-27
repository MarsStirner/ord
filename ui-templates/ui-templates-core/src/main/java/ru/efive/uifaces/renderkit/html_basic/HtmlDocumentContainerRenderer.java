package ru.efive.uifaces.renderkit.html_basic;

import java.io.IOException;
import java.util.Map;

import javax.faces.render.FacesRenderer;

import ru.efive.uifaces.component.ComponentFamily;
import ru.efive.uifaces.component.html.HtmlDocumentContainer;
import ru.efive.uifaces.data.Document;
import ru.efive.uifaces.renderkit.html_basic.base.AdvancedResponseWriter;
import ru.efive.uifaces.renderkit.html_basic.base.ComponentAttribute;
import ru.efive.uifaces.renderkit.html_basic.base.HtmlAttribute;
import ru.efive.uifaces.renderkit.html_basic.base.HtmlBasicRenderer;
import ru.efive.uifaces.renderkit.html_basic.base.HtmlElement;

/**
 * Renderer class for {@link HtmlDocumentContainer} component class.
 * 
 * @author Ramil_Habirov
 */
@FacesRenderer(
        renderKitId = "HTML_BASIC",
        rendererType = HtmlDocumentContainer.RENDERER_TYPE,
        componentFamily = ComponentFamily.DOCUMENT_CONTAINER)
public class HtmlDocumentContainerRenderer extends HtmlBasicRenderer {

    /**
     * Facet names.
     */
    public static final String FACET_HEADER_CREATE = "headerCreate";
    public static final String FACET_HEADER_EDIT = "headerEdit";
    public static final String FACET_HEADER_VIEW = "headerView";
    public static final String FACET_FORBIDDEN = "forbidden";
    public static final String FACET_NOT_FOUND = "notFound";
    public static final String FACET_EDIT = "edit";
    public static final String FACET_VIEW = "view";

    /**
     * CSS class names.
     */
    public static final String CSS_COMPONENT = "e5ui-document";
    public static final String CSS_EDIT_LINK = "e5ui-document-edit-link";
    public static final String CSS_EDIT_BUTTON = "e5ui-document-edit-button";

    /**
     * Output texts.
     */
    public static final String FORBIDDEN = "403 - Forbidden";
    public static final String NOT_FOUND = "404 - Not Found";

    /**
     * Available edit mode controls.
     */
    public static final String EDIT_MODE_CONTROL_LINK = "link";
    public static final String EDIT_MODE_CONTROL_BUTTON = "button";

    /**
     * Default values.
     */
    public static final boolean CAN_CREATE_DEFAULT = true;
    public static final boolean CAN_EDIT_DEFAULT = true;
    public static final boolean CAN_VIEW_DEFAULT = true;
    public static final String ID_PARAM_DEFAULT = "id";
    public static final String EDIT_PARAM_DEFAULT = "edit";
    public static final String EDIT_MODE_CONTROL_DEFAULT = EDIT_MODE_CONTROL_LINK;
    public static final String EDIT_MODE_CAPTION_DEFAULT = "Edit";

    @Override
    protected void encodeBegin(AdvancedResponseWriter writer)
            throws IOException {
        writer.startElement(HtmlElement.DIV);
        encodeIdAttributeIfShould(writer);
        writer.writeComponentAttribute(HtmlAttribute.STYLE,
                ComponentAttribute.STYLE, null);
        writer.writeEvaluatedAttribute(HtmlAttribute.CLASS, CE_CSS_CLASS, null,
                CSS_COMPONENT);

        Map<String, String> requestParameterMap = writer.getContext()
                .getExternalContext().getRequestParameterMap();
        boolean idParamExists = requestParameterMap
                .containsKey(getIdParam(writer));
        boolean editParamExists = requestParameterMap
                .containsKey(getEditParam(writer));
        boolean canCreate = getCanCreate(writer);
        boolean canEdit = getCanEdit(writer);
        boolean canView = getCanView(writer);
        Object document = getDocument(writer);
        if (editParamExists) {
            if (idParamExists) {
                writer.writeFacet(FACET_HEADER_EDIT);
                if (canEdit) {
                    if (document != null && document instanceof Document) {
                        writeEdit(writer);
                    } else {
                        writeNotFound(writer);
                    }
                } else {
                    writeForbidden(writer);
                }
            } else {
                writer.writeFacet(FACET_HEADER_CREATE);
                if (canCreate) {
                    // TODO Is there writeEdit too?
                    writeEdit(writer);
                } else {
                    writeForbidden(writer);
                }
            }
        } else {
            writer.writeFacet(FACET_HEADER_VIEW);
            if (canView) {
                if (idParamExists && document != null
                        && document instanceof Document) {
                    writeView(writer);
                    if (canEdit) {
                        String editModeControl = getEditModeControl(writer);
                        if (EDIT_MODE_CONTROL_LINK
                                .equalsIgnoreCase(editModeControl)) {
                            writeEditLink(writer);
                        } else if (EDIT_MODE_CONTROL_BUTTON
                                .equalsIgnoreCase(editModeControl)) {
                            writeEditButton(writer);
                        }
                    }
                } else {
                    writeNotFound(writer);
                }
            } else {
                writeForbidden(writer);
            }
        }
    }

    @Override
    protected void encodeEnd(AdvancedResponseWriter writer) throws IOException {
        writer.endElement(HtmlElement.DIV);
    }

    /**
     * Is the document could be created.
     * 
     * @param writer
     *            writer.
     * @return <code>true</code> if document could be created, else
     *         <code>false</code>.
     */
    private Boolean getCanCreate(AdvancedResponseWriter writer) {
        return (Boolean) getComponentAttributeValue(writer,
                ComponentAttribute.CAN_CREATE, CAN_CREATE_DEFAULT);
    }

    /**
     * Is the document could be edited.
     * 
     * @param writer
     *            writer.
     * @return <code>true</code> if document could be edited, else
     *         <code>false</code>.
     */
    private Boolean getCanEdit(AdvancedResponseWriter writer) {
        return (Boolean) getComponentAttributeValue(writer,
                ComponentAttribute.CAN_EDIT, CAN_EDIT_DEFAULT);
    }

    /**
     * Is the document could be viewed.
     * 
     * @param writer
     *            writer.
     * @return <code>true</code> if document could be viewed, else
     *         <code>false</code>.
     */
    private Boolean getCanView(AdvancedResponseWriter writer) {
        return (Boolean) getComponentAttributeValue(writer,
                ComponentAttribute.CAN_VIEW, CAN_VIEW_DEFAULT);
    }

    /**
     * Returns request parameter name indicating document identifier.
     * 
     * @param writer
     *            writer.
     * @return request parameter name indicating document identifier.
     */
    private String getIdParam(AdvancedResponseWriter writer) {
        return (String) getComponentAttributeValue(writer,
                ComponentAttribute.ID_PARAM, ID_PARAM_DEFAULT);
    }

    /**
     * Returns request parameter name indicating that document is in edit mode.
     * 
     * @param writer
     *            writer.
     * @return request parameter name indicating that document is in edit mode.
     */
    private String getEditParam(AdvancedResponseWriter writer) {
        return (String) getComponentAttributeValue(writer,
                ComponentAttribute.EDIT_PARAM, EDIT_PARAM_DEFAULT);
    }

    /**
     * Returns a control type to switch document to the edit mode.
     * 
     * @param writer
     *            writer.
     * @return a control type to switch document to the edit mode.
     */
    private String getEditModeControl(AdvancedResponseWriter writer) {
        return (String) getComponentAttributeValue(writer,
                ComponentAttribute.EDIT_MODE_CONTROL, EDIT_MODE_CONTROL_DEFAULT);
    }

    /**
     * Returns caption of control to switch document to the edit mode.
     * 
     * @param writer
     *            writer.
     * @return caption of control to switch document to the edit mode.
     */
    // private String getEditModeCaption(AdvancedResponseWriter writer) {
    // return (String) getComponentAttributeValue(writer,
    // ComponentAttribute.EDIT_MODE_CAPTION, EDIT_MODE_CAPTION_DEFAULT);
    // }

    /**
     * Returns document.
     * 
     * @param writer
     *            writer.
     * @return document.
     */
    private Object getDocument(AdvancedResponseWriter writer) {
        return getComponentAttributeValue(writer, ComponentAttribute.DOCUMENT);
    }

    /**
     * Returns component attribute or default value if it is <code>null</code>.
     * 
     * @param writer
     *            writer.
     * @param htmlAttr
     *            HTML attribute.
     * @param componentAttr
     *            component attribute.
     * @param defaultValue
     *            default value.
     * @throws IOException
     *             when can't return component attribute.
     */
    // TODO May be need move to AdvancedResponseWriter.
    private void writeComponentAttribute(AdvancedResponseWriter writer,
            HtmlAttribute htmlAttr, String componentAttr, Object defaultValue)
            throws IOException {
        if (htmlAttr == null || componentAttr == null) {
            throw new NullPointerException(
                    "One or more of 'htmlAttr' and 'componentAttr' is null");
        }
        Object value = getComponentAttributeValue(writer, componentAttr,
                defaultValue);
        if (value != null) {
            writer.writeAttribute(htmlAttr, value, null);
        }
    }

    /**
     * Writes attribute as text or default value if it is <code>null</code>.
     * 
     * @param writer
     *            writer.
     * @param componentAttr
     *            component attribute.
     * @param defaultValue
     *            default value.
     * @throws IOException
     *             when can't write.
     */
    // TODO May be need move to AdvancedResponseWriter.
    private void writeAttributeText(AdvancedResponseWriter writer,
            String componentAttr, Object defaultValue) throws IOException {
        if (componentAttr == null) {
            throw new NullPointerException("'componentAttr' is null");
        }
        Object value = getComponentAttributeValue(writer, componentAttr,
                defaultValue);
        if (value != null) {
            writer.writeText(value, null);
        }
    }

    /**
     * Returns component attribute value or default value if it is
     * <code>null</code>.
     * 
     * @param writer
     *            writer.
     * @param componentAttr
     *            component attribute.
     * @param defaultValue
     *            default value.
     * @return component attribute value of default value if it is
     *         <code>null</code>.
     */
    // TODO May be need move to AdvancedResponseWriter.
    private Object getComponentAttributeValue(AdvancedResponseWriter writer,
            String componentAttr, Object defaultValue) {
        if (componentAttr == null) {
            throw new NullPointerException("'componentAttr' is null");
        }
        Object value = writer.getComponent().getAttributes().get(componentAttr);
        if (value != null) {
            return value;
        } else {
            return defaultValue;
        }
    }

    /**
     * Returns component attribute value.
     * 
     * @param writer
     *            writer.
     * @param componentAttr
     *            component attribute.
     * @return component attribute value.
     */
    // TODO May be need move to AdvancedResponseWriter.
    private Object getComponentAttributeValue(AdvancedResponseWriter writer,
            String componentAttr) {
        return getComponentAttributeValue(writer, componentAttr, null);
    }

    /**
     * Writes when edit.
     * 
     * @param writer
     *            writer.
     * @throws IOException
     *             when can't write.
     */
    private void writeEdit(AdvancedResponseWriter writer) throws IOException {
        writer.writeFacet(FACET_EDIT);
    }

    /**
     * Writes when view.
     * 
     * @param writer
     *            writer.
     * @throws IOException
     *             when can't write.
     */
    private void writeView(AdvancedResponseWriter writer) throws IOException {
        writer.writeFacet(FACET_VIEW);
    }

    /**
     * Writes when forbidden.
     * 
     * @param writer
     *            writer.
     * @throws IOException
     *             when can't write.
     */
    private void writeForbidden(AdvancedResponseWriter writer)
            throws IOException {
        if (!writer.writeFacet(FACET_FORBIDDEN)) {
            writer.write(FORBIDDEN);
        }
    }

    /**
     * Writes when not found.
     * 
     * @param writer
     *            writer.
     * @throws IOException
     *             when can't write.
     */
    private void writeNotFound(AdvancedResponseWriter writer)
            throws IOException {
        if (!writer.writeFacet(FACET_NOT_FOUND)) {
            writer.write(NOT_FOUND);
        }
    }

    /**
     * Writes edit link.
     * 
     * @param writer
     *            writer.
     * @throws IOException
     *             when can't write edit link.
     */
    private void writeEditLink(AdvancedResponseWriter writer)
            throws IOException {
        writer.startElement(HtmlElement.DIV);
        writer.writeEvaluatedAttribute(HtmlAttribute.CLASS, CE_CSS_CLASS, null,
                CSS_EDIT_LINK);

        writer.startElement(HtmlElement.A);
        writer.writeAttribute(HtmlAttribute.HREF, getEditHref(writer), null);
        writeAttributeText(writer, ComponentAttribute.EDIT_MODE_CAPTION,
                EDIT_MODE_CAPTION_DEFAULT);
        writer.endElement(HtmlElement.A);

        writer.endElement(HtmlElement.DIV);
    }

    /**
     * Writes edit button.
     * 
     * @param writer
     *            writer.
     * @throws IOException
     *             when can't write edit button.
     */
    private void writeEditButton(AdvancedResponseWriter writer)
            throws IOException {
        writer.startElement(HtmlElement.DIV);
        writer.writeEvaluatedAttribute(HtmlAttribute.CLASS, CE_CSS_CLASS, null,
                CSS_EDIT_BUTTON);

        writer.startElement(HtmlElement.INPUT);
        writer.writeAttribute(HtmlAttribute.TYPE, "button", null);
        writeComponentAttribute(writer, HtmlAttribute.VALUE,
                ComponentAttribute.EDIT_MODE_CAPTION, EDIT_MODE_CAPTION_DEFAULT);
        writer.writeAttribute(HtmlAttribute.ONCLICK, "location.href='"
                + getEditHref(writer) + "'", null);
        writer.endElement(HtmlElement.INPUT);

        writer.endElement(HtmlElement.DIV);
    }

    /**
     * Returns edit HTML reference.
     * 
     * @param writer
     *            writer.
     * @return edit HTML reference.
     * @throws IOException
     *             when can't return edit HTML reference.
     */
    private String getEditHref(AdvancedResponseWriter writer)
            throws IOException {
        String params = "";

        Object document = getDocument(writer);
        if (document != null && document instanceof Document) {
            Object id = ((Document<?>) document).getId();
            if (id != null) {
                params = getIdParam(writer) + "=" + id;
            }
        }

        if (!params.isEmpty()) {
            params += "&";
        }
        params += getEditParam(writer);

        return "?" + params;
    }
}
