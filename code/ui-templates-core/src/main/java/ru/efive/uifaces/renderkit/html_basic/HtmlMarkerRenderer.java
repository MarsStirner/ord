package ru.efive.uifaces.renderkit.html_basic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;
import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.render.FacesRenderer;

import ru.efive.uifaces.component.ComponentFamily;
import ru.efive.uifaces.component.html.HtmlMarker;
import ru.efive.uifaces.component.html.HtmlMultiMarker;
import ru.efive.uifaces.renderkit.html_basic.base.AdvancedResponseWriter;
import ru.efive.uifaces.renderkit.html_basic.base.ComponentAttribute;
import ru.efive.uifaces.renderkit.html_basic.base.HtmlAttribute;
import ru.efive.uifaces.renderkit.html_basic.base.HtmlBasicRenderer;
import ru.efive.uifaces.renderkit.html_basic.base.HtmlElement;

import static ru.efive.uifaces.renderkit.html_basic.base.AdvancedResponseWriter.writeStyleClass;
import static java.lang.String.format;

/**
 * Renderer class for {@link HtmlMarker} component class.
 * 
 * @author Ramil_Habirov
 */
@FacesRenderer(renderKitId = "HTML_BASIC", rendererType = HtmlMarker.RENDERER_TYPE, componentFamily = ComponentFamily.MARKER)
@ResourceDependencies({
        @ResourceDependency(name = "marker.css", target = "head", library = "e5ui/css"),
        @ResourceDependency(name = "simpletip.css", target = "head", library = "e5ui/css"),
        @ResourceDependency(name = "jquery.js", target = "head", library = "e5ui/js"),
        @ResourceDependency(name = "jquery.simpletip.js", target = "head", library = "e5ui/js")
})
public class HtmlMarkerRenderer extends HtmlBasicRenderer {

    /**
     * Default CSS classes.
     */
    public static final String INFO_STYLE_CLASS_DEFAULT = "e5ui-marker e5ui-marker-info";
    public static final String WARN_STYLE_CLASS_DEFAULT = "e5ui-marker e5ui-marker-warn";
    public static final String ERROR_STYLE_CLASS_DEFAULT = "e5ui-marker e5ui-marker-error";
    public static final String FATAL_STYLE_CLASS_DEFAULT = "e5ui-marker e5ui-marker-fatal";

    private static final String SHADOW_CLASS = "e5ui-modal-overlay-shadow";
    private static final String OVERLAY_CLASS = "e5ui-modal-overlay";
    private static final String WINDOW_CONTAINER_CLASS = "e5ui-multimarker-window-container";
    private static final String WINDOW_CLASS = "e5ui-modal e5ui-modal-content e5ui-multimarker-window";
    private static final String CLOSE_BUTTON_CLASS = "e5ui-multimarker-window-close";
    private static final String CLOSE_BUTTON_CLICK = "jQuery('#%s').remove();";

    private boolean encoded;

    @Override
    protected void encodeBegin(AdvancedResponseWriter writer)
            throws IOException {
        UIComponent component = writer.getComponent();

        if (!shouldEncodeComponent(writer.getContext(), writer.getComponent())) {
            return;
        }

        FacesMessage.Severity maximumSeverity = null;
        List<String> infoMessages = new ArrayList<String>();
        List<String> warnMessages = new ArrayList<String>();
        List<String> errorMessages = new ArrayList<String>();
        List<String> fatalMessages = new ArrayList<String>();
        String forComponentId = getFor(writer);

        Iterator<FacesMessage> messageIterator = getMessageIterator(writer,
                forComponentId);

        while (messageIterator.hasNext()) {
            FacesMessage message = messageIterator.next();
            FacesMessage.Severity messageSeverity = message.getSeverity();
            if (FacesMessage.SEVERITY_INFO.equals(messageSeverity)) {
                infoMessages.add(message.getSummary());
            } else if (FacesMessage.SEVERITY_WARN.equals(messageSeverity)) {
                warnMessages.add(message.getSummary());
            } else if (FacesMessage.SEVERITY_ERROR.equals(messageSeverity)) {
                errorMessages.add(message.getSummary());
            } else if (FacesMessage.SEVERITY_FATAL.equals(messageSeverity)) {
                fatalMessages.add(message.getSummary());
            }
            if (maximumSeverity == null
                    || maximumSeverity.compareTo(messageSeverity) < 0) {
                maximumSeverity = messageSeverity;
            }
        }

        if (maximumSeverity == null) {
            encoded = false;
            return;
        }
        encoded = true;

        HtmlMultiMarker.DisplayMode displayMode = component instanceof HtmlMultiMarker ?
                ((HtmlMultiMarker)component).getDisplayMode() : HtmlMultiMarker.DisplayMode.hint;
        switch (displayMode) {
            case hint:
                writer.startElement(HtmlElement.SPAN);
                encodeIdAttributeIfShould(writer);

                if (FacesMessage.SEVERITY_INFO.equals(maximumSeverity)) {
                    writer.writeComponentAttribute(HtmlAttribute.STYLE,
                            ComponentAttribute.INFO_STYLE, null);
                    writer.writeEvaluatedAttribute(HtmlAttribute.CLASS,
                            CE_CSS_CLASS, null, getInfoStyleClass(writer));
                } else if (FacesMessage.SEVERITY_WARN.equals(maximumSeverity)) {
                    writer.writeComponentAttribute(HtmlAttribute.STYLE,
                            ComponentAttribute.WARN_STYLE, null);
                    writer.writeEvaluatedAttribute(HtmlAttribute.CLASS,
                            CE_CSS_CLASS, null, getWarnStyleClass(writer));
                } else if (FacesMessage.SEVERITY_ERROR.equals(maximumSeverity)) {
                    writer.writeComponentAttribute(HtmlAttribute.STYLE,
                            ComponentAttribute.ERROR_STYLE, null);
                    writer.writeEvaluatedAttribute(HtmlAttribute.CLASS,
                            CE_CSS_CLASS, null, getErrorStyleClass(writer));
                } else if (FacesMessage.SEVERITY_FATAL.equals(maximumSeverity)) {
                    writer.writeComponentAttribute(HtmlAttribute.STYLE,
                            ComponentAttribute.FATAL_STYLE, null);
                    writer.writeEvaluatedAttribute(HtmlAttribute.CLASS,
                            CE_CSS_CLASS, null, getFatalStyleClass(writer));
                }

                writer.write("&nbsp;");
                writer.endElement(HtmlElement.SPAN);
                writer.write("\n");
                writer.startElement(HtmlElement.SCRIPT);
                writer.writeAttribute(HtmlAttribute.TYPE, "text/javascript", null);

                final String componentId = writer.getComponent().getClientId().replaceAll(":", "\\\\\\\\:");

                writer.writeText("\njQuery('#" + componentId + "').simpletip({\n",
                        null);
                writer.writeText("    fixed: true,\n", null);
                writer.writeText("    position: [\"10\", \"5\"],\n", null);
                writer.writeText("    content: '", null);

                writer.startElement(HtmlElement.DIV);
                writer.writeComponentAttribute(HtmlAttribute.STYLE,
                        ComponentAttribute.HINT_WINDOW_STYLE, null);
                writer.writeEvaluatedAttribute(HtmlAttribute.CLASS, CE_CSS_CLASS,
                        null, getHintWindowStyleClass(writer));
                break;
            case text:
                writer.startElement(HtmlElement.DIV);
                break;
            case window:
                String id = component.getClientId() + "-e5uiMultiMarker";
                // all marker
                writer.startElement(HtmlElement.DIV);
                writer.writeAttribute(HtmlAttribute.ID, id, null);
                // shadow
                writer.startElement(HtmlElement.DIV);
                writeStyleClass(null, writer, SHADOW_CLASS);
                writer.endElement(HtmlElement.DIV); // shadow
                // overlay
                writer.startElement(HtmlElement.DIV);
                writeStyleClass(null, writer, OVERLAY_CLASS);
                // window container
                writer.startElement(HtmlElement.TABLE);
                writeStyleClass(null, writer, WINDOW_CONTAINER_CLASS);
                writer.startElement(HtmlElement.TD);
                // window
                writer.startElement(HtmlElement.DIV);
                writeStyleClass(null, writer, WINDOW_CLASS);

                List<UIComponent> children = component.getChildren();
                if (children == null || children.isEmpty()) {
                    writer.startElement(HtmlElement.DIV);
                    writer.writeAttribute(HtmlAttribute.ONCLICK, format(CLOSE_BUTTON_CLICK, id.replaceAll(":", "\\\\:")), null);
                    writeStyleClass(null, writer, CLOSE_BUTTON_CLASS);
                    writer.writeText("X", null);
                    writer.endElement(HtmlElement.DIV);
                }
        }

        if (!fatalMessages.isEmpty()) {
            writer.startElement(HtmlElement.DIV);
            writer.writeComponentAttribute(HtmlAttribute.STYLE,
                    ComponentAttribute.HINT_WINDOW_FATAL_STYLE, null);
            writer.writeEvaluatedAttribute(HtmlAttribute.CLASS, CE_CSS_CLASS,
                    null, getHintWindowFatalStyleClass(writer));
            for (String fatalMessage : fatalMessages) {
                writer.startElement(HtmlElement.DIV);
                writer.write(htmlEscape(fatalMessage));
                writer.endElement(HtmlElement.DIV);
            }
            writer.endElement(HtmlElement.DIV);
        }
        if (!errorMessages.isEmpty()) {
            writer.startElement(HtmlElement.DIV);
            writer.writeComponentAttribute(HtmlAttribute.STYLE,
                    ComponentAttribute.HINT_WINDOW_ERROR_STYLE, null);
            writer.writeEvaluatedAttribute(HtmlAttribute.CLASS, CE_CSS_CLASS,
                    null, getHintWindowErrorStyleClass(writer));
            for (String errorMessage : errorMessages) {
                writer.startElement(HtmlElement.DIV);
                writer.write(htmlEscape(errorMessage));
                writer.endElement(HtmlElement.DIV);
            }
            writer.endElement(HtmlElement.DIV);
        }
        if (!warnMessages.isEmpty()) {
            writer.startElement(HtmlElement.DIV);
            writer.writeComponentAttribute(HtmlAttribute.STYLE,
                    ComponentAttribute.HINT_WINDOW_WARN_STYLE, null);
            writer.writeEvaluatedAttribute(HtmlAttribute.CLASS, CE_CSS_CLASS,
                    null, getHintWindowWarnStyleClass(writer));
            for (String warnMessage : warnMessages) {
                writer.startElement(HtmlElement.DIV);
                writer.write(htmlEscape(warnMessage));
                writer.endElement(HtmlElement.DIV);
            }
            writer.endElement(HtmlElement.DIV);
        }
        if (!infoMessages.isEmpty()) {
            writer.startElement(HtmlElement.DIV);
            writer.writeComponentAttribute(HtmlAttribute.STYLE,
                    ComponentAttribute.HINT_WINDOW_INFO_STYLE, null);
            writer.writeEvaluatedAttribute(HtmlAttribute.CLASS, CE_CSS_CLASS,
                    null, getHintWindowInfoStyleClass(writer));
            for (String infoMessage : infoMessages) {
                writer.startElement(HtmlElement.DIV);
                writer.write(htmlEscape(infoMessage));
                writer.endElement(HtmlElement.DIV);
            }
            writer.endElement(HtmlElement.DIV);
        }
    }

    @Override
    protected void encodeEnd(AdvancedResponseWriter writer) throws IOException {
        UIComponent component = writer.getComponent();

        if (!shouldEncodeComponent(writer.getContext(), writer.getComponent()) || !encoded) {
            return;
        }

        super.encodeEnd(writer);

        HtmlMultiMarker.DisplayMode displayMode = component instanceof HtmlMultiMarker ?
                ((HtmlMultiMarker)component).getDisplayMode() : HtmlMultiMarker.DisplayMode.hint;
        switch (displayMode) {
            case hint:
                writer.endElement(HtmlElement.DIV);
                writer.write("'");
                writer.writeText("\n});\n", null);
                writer.endElement(HtmlElement.SCRIPT);
                break;
            case text:
                writer.endElement(HtmlElement.DIV);
                break;
            case window:
                writer.endElement(HtmlElement.DIV); // window
                writer.endElement(HtmlElement.TD); // window container
                writer.endElement(HtmlElement.TABLE);
                writer.endElement(HtmlElement.DIV); // overlay
        }
    }

    @Override
    protected void encodeChildren(AdvancedResponseWriter writer, List<UIComponent> children) throws IOException {
        UIComponent component = writer.getComponent();

        if (!shouldEncodeComponent(writer.getContext(), writer.getComponent()) || !encoded) {
            return;
        }

        super.encodeChildren(writer, children);
    }

    @Override
    protected boolean shouldEncodeIdAttribute(FacesContext context,
            UIComponent component) throws IOException {
        return true;
    }

    /**
     * Returns identifier of component messages belongs to.
     * 
     * @param writer
     *            writer.
     * @return identifier of component.
     */
    protected String getFor(AdvancedResponseWriter writer) {
        return (String) getComponentAttributeValue(writer,
                ComponentAttribute.FOR);
    }

    /**
     * Returns CSS class when there are info messages only.
     * 
     * @param writer
     *            writer.
     * @return CSS class.
     */
    protected String getInfoStyleClass(AdvancedResponseWriter writer) {
        return (String) getComponentAttributeValue(writer,
                ComponentAttribute.INFO_STYLE_CLASS, INFO_STYLE_CLASS_DEFAULT);
    }

    /**
     * Returns CSS class when there are warn messages.
     * 
     * @param writer
     *            writer.
     * @return CSS class.
     */
    protected String getWarnStyleClass(AdvancedResponseWriter writer) {
        return (String) getComponentAttributeValue(writer,
                ComponentAttribute.WARN_STYLE_CLASS, WARN_STYLE_CLASS_DEFAULT);
    }

    /**
     * Returns CSS class when there are error messages.
     * 
     * @param writer
     *            writer.
     * @return CSS class.
     */
    protected String getErrorStyleClass(AdvancedResponseWriter writer) {
        return (String) getComponentAttributeValue(writer,
                ComponentAttribute.ERROR_STYLE_CLASS, ERROR_STYLE_CLASS_DEFAULT);
    }

    /**
     * Returns CSS class when there are fatal messages.
     * 
     * @param writer
     *            writer.
     * @return CSS class.
     */
    protected String getFatalStyleClass(AdvancedResponseWriter writer) {
        return (String) getComponentAttributeValue(writer,
                ComponentAttribute.FATAL_STYLE_CLASS, FATAL_STYLE_CLASS_DEFAULT);
    }

    /**
     * Returns CSS class of hint window.
     * 
     * @param writer
     *            writer.
     * @return CSS class.
     */
    protected String getHintWindowStyleClass(AdvancedResponseWriter writer) {
        return (String) getComponentAttributeValue(writer,
                ComponentAttribute.HINT_WINDOW_STYLE_CLASS);
    }

    /**
     * Returns CSS class of info messages on the hint window.
     * 
     * @param writer
     *            writer.
     * @return CSS class.
     */
    protected String getHintWindowInfoStyleClass(AdvancedResponseWriter writer) {
        return (String) getComponentAttributeValue(writer,
                ComponentAttribute.HINT_WINDOW_INFO_STYLE_CLASS);
    }

    /**
     * Returns CSS class of warn messages on the hint window.
     * 
     * @param writer
     *            writer.
     * @return CSS class.
     */
    protected String getHintWindowWarnStyleClass(AdvancedResponseWriter writer) {
        return (String) getComponentAttributeValue(writer,
                ComponentAttribute.HINT_WINDOW_WARN_STYLE_CLASS);
    }

    /**
     * Returns CSS class of error messages on the hint window.
     * 
     * @param writer
     *            writer.
     * @return CSS class.
     */
    protected String getHintWindowErrorStyleClass(AdvancedResponseWriter writer) {
        return (String) getComponentAttributeValue(writer,
                ComponentAttribute.HINT_WINDOW_ERROR_STYLE_CLASS);
    }

    /**
     * Returns CSS class of fatal messages on the hint window.
     * 
     * @param writer
     *            writer.
     * @return CSS class.
     */
    protected String getHintWindowFatalStyleClass(AdvancedResponseWriter writer) {
        return (String) getComponentAttributeValue(writer,
                ComponentAttribute.HINT_WINDOW_FATAL_STYLE_CLASS);
    }

    /**
     * Returns component attribute value if it is not null, unless default value
     * is returned.
     * 
     * @param writer
     *            writer.
     * @param componentAttr
     *            attribute.
     * @param defaultValue
     *            default value.
     * @return attribute value or default value.
     */
    // TODO May be need move to AdvancedResponseWriter.
    protected Object getComponentAttributeValue(AdvancedResponseWriter writer,
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
     *            attribute.
     * @return attribute value.
     */
    // TODO May be need move to AdvancedResponseWriter.
    protected Object getComponentAttributeValue(AdvancedResponseWriter writer,
            String componentAttr) {
        return getComponentAttributeValue(writer, componentAttr, null);
    }

    /**
     * Returns message iterator.
     * 
     * @param writer
     *            writer.
     * @param forComponentId
     *            identifier of component messages belongs to.
     * @return message iterator.
     */
    protected Iterator<FacesMessage> getMessageIterator(
            AdvancedResponseWriter writer, String forComponentId) {
        Iterator<FacesMessage> messageIter;
        FacesContext context = writer.getContext();
        if (null != forComponentId) {
            if (forComponentId.length() == 0) {
                messageIter = context.getMessages(null);
            } else {
                UIComponent result = findComponent(writer, forComponentId,
                        writer.getComponent().getParent());
                if (result == null) {
                    messageIter = new ArrayList<FacesMessage>().iterator();
                } else {
                    messageIter = context.getMessages(result
                            .getClientId(context));
                }
            }
        } else {
            messageIter = context.getMessages();
        }
        return messageIter;

    }

    /**
     * Finds component.
     * 
     * @param writer
     *            writer.
     * @param componentId
     *            identifier of component.
     * @param parentComponent
     *            parent of component.
     * @return component.
     */
    // TODO May be need move to AdvancedResponseWriter.
    protected UIComponent findComponent(AdvancedResponseWriter writer,
            String componentId, UIComponent parentComponent) {
        if (null == componentId || componentId.length() == 0) {
            return null;
        }
        UIComponent result = null;
        UIComponent currentParent = parentComponent;
        try {
            while (currentParent != null) {
                result = currentParent.findComponent(componentId);
                if (result != null) {
                    break;
                }
                currentParent = currentParent.getParent();
            }
            if (result == null) {
                result = findComponentBelow(writer.getContext().getViewRoot(),
                        componentId);
            }
        } catch (Exception e) {
        }
        return result;
    }

    /**
     * Finds child component of parent component.
     * 
     * @param component
     *            parent component
     * @param componentId
     *            identifier of child component.
     * @return child component.
     */
    // TODO May be need move to AdvancedResponseWriter.
    protected static UIComponent findComponentBelow(UIComponent component,
            String componentId) {
        UIComponent retComp = null;
        if (component.getChildCount() > 0) {
            List<UIComponent> children = component.getChildren();
            for (int i = 0, size = children.size(); i < size; i++) {
                UIComponent comp = children.get(i);

                if (comp instanceof NamingContainer) {
                    try {
                        retComp = comp.findComponent(componentId);
                    } catch (IllegalArgumentException iae) {
                        continue;
                    }
                }
                if (retComp == null) {
                    if (comp.getChildCount() > 0) {
                        retComp = findComponentBelow(comp, componentId);
                    }
                }
                if (retComp != null) {
                    break;
                }
            }
        }
        return retComp;
    }

    /**
     * Escapes HTML string.
     * 
     * @param string
     *            HTML string.
     * @return escaped HTML string.
     */
    protected static String htmlEscape(String string) {
        if (string == null) {
            return null;
        } else {
            StringBuilder htmlEscapedStringBuilder = new StringBuilder();
            for (int i = 0; i < string.length(); i++) {
                char charAt = string.charAt(i);
                if (charAt == '&') {
                    htmlEscapedStringBuilder.append("&amp;");
                } else if (charAt == '"') {
                    htmlEscapedStringBuilder.append("&quot;");
                } else if (charAt == '>') {
                    htmlEscapedStringBuilder.append("&gt;");
                } else if (charAt == '<') {
                    htmlEscapedStringBuilder.append("&lt;");
                } else if (charAt == '\'') {
                    htmlEscapedStringBuilder.append("&#39;");
                } else {
                    htmlEscapedStringBuilder.append(charAt);
                }
            }
            return htmlEscapedStringBuilder.toString();
        }
    }
}
