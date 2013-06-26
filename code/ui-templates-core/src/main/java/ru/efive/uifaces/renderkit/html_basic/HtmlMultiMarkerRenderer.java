package ru.efive.uifaces.renderkit.html_basic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.render.FacesRenderer;

import ru.efive.uifaces.component.ComponentFamily;
import ru.efive.uifaces.component.html.HtmlMultiMarker;
import ru.efive.uifaces.renderkit.html_basic.base.AdvancedResponseWriter;
import ru.efive.uifaces.renderkit.html_basic.base.ComponentAttribute;

/**
 * Renderer class for {@link HtmlMultiMarker} component class.
 * 
 * @author Ramil_Habirov
 */
@FacesRenderer(renderKitId = "HTML_BASIC", rendererType = HtmlMultiMarker.RENDERER_TYPE, componentFamily = ComponentFamily.MULTI_MARKER)
@ResourceDependencies({
        @ResourceDependency(name = "modalWindow.css", target = "head", library = "e5ui/css"),
        @ResourceDependency(name = "multiMarker.css", target = "head", library = "e5ui/css"),
        @ResourceDependency(name = "simpletip.css", target = "head", library = "e5ui/css"),
        @ResourceDependency(name = "jquery.js", target = "head", library = "e5ui/js"),
        @ResourceDependency(name = "jquery.simpletip.js", target = "head", library = "e5ui/js"),
        @ResourceDependency(name = "util.js", target = "head", library = "e5ui/js"),
        @ResourceDependency(name = "multiMarker.js", target = "head", library = "e5ui/js")
})
public class HtmlMultiMarkerRenderer extends HtmlMarkerRenderer {

    /**
     * Default CSS classes.
     */
    public static final String INFO_STYLE_CLASS_DEFAULT = "e5ui-multimarker e5ui-multimarker-info";
    public static final String WARN_STYLE_CLASS_DEFAULT = "e5ui-multimarker e5ui-multimarker-warn";
    public static final String ERROR_STYLE_CLASS_DEFAULT = "e5ui-multimarker e5ui-multimarker-error";
    public static final String FATAL_STYLE_CLASS_DEFAULT = "e5ui-multimarker e5ui-multimarker-fatal";

    /**
     * Returns CSS class when there are info messages only.
     * 
     * @param writer
     *            writer.
     * @return CSS class.
     */
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
    protected String getHintWindowFatalStyleClass(AdvancedResponseWriter writer) {
        return (String) getComponentAttributeValue(writer,
                ComponentAttribute.HINT_WINDOW_FATAL_STYLE_CLASS);
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
    @Override
    protected Iterator<FacesMessage> getMessageIterator(
            AdvancedResponseWriter writer, String forComponentId) {
        Iterator<FacesMessage> messageIter;
        FacesContext context = writer.getContext();
        if (forComponentId != null && !forComponentId.isEmpty()) {
            UIComponent result = findComponent(writer, forComponentId, writer
                    .getComponent().getParent());
            if (result == null) {
                messageIter = new ArrayList<FacesMessage>().iterator();
            } else {
                messageIter = getMessages(context, result).iterator();
            }
        } else {
            messageIter = context.getMessages();
        }
        return messageIter;

    }

    private List<FacesMessage> getMessages(FacesContext context,
            UIComponent component) {
        List<FacesMessage> messages = new ArrayList<FacesMessage>();
        if (component != null) {
            Iterator<FacesMessage> messageIterator = context
                    .getMessages(component.getClientId(context));
            while (messageIterator.hasNext()) {
                FacesMessage message = messageIterator.next();
                messages.add(message);
            }
            Iterator<UIComponent> facetsAndChildrenIterator = component
                    .getFacetsAndChildren();
            while (facetsAndChildrenIterator.hasNext()) {
                UIComponent facetOrChild = facetsAndChildrenIterator.next();
                facetOrChild.pushComponentToEL(context, null);
                try {
                    messages.addAll(getMessages(context, facetOrChild));
                } finally {
                    facetOrChild.popComponentFromEL(context);
                }
            }
        }
        return messages;
    }
}
