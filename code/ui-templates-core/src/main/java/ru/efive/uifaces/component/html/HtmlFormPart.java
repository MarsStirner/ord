package ru.efive.uifaces.component.html;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.faces.component.ContextCallback;
import javax.faces.component.FacesComponent;
import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.component.UIForm;
import javax.faces.component.UINamingContainer;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.event.ListenerFor;
import javax.faces.event.PostRestoreStateEvent;
import ru.efive.uifaces.component.ComponentFamily;

/**
 * Component provides partial form submission.
 *
 * @author Denis Kotegov
 */
@FacesComponent("ru.efive.uifaces.FormPart")
@ListenerFor(systemEventClass=PostRestoreStateEvent.class)
public class HtmlFormPart extends UIComponentBase {

    private List<UICommand> formCommands = new ArrayList<UICommand>();

    private boolean shouldProcess = true;

    @Override
    public String getFamily() {
        return ComponentFamily.FORM_PART;
    }

    @Override
    public void processDecodes(FacesContext context) {
        UICommand command = getClickedCommand(context, formCommands);

        if (command != null) {
            shouldProcess = isCommandShouldProcessThis(command);
        } else {
            shouldProcess = false;
        }

        super.processDecodes(context);
    }

    @Override
    public void processValidators(FacesContext context) {
        if (shouldProcess) {
            super.processValidators(context);
        } else {
            processChildValidators(context, this);
        }
    }

    @Override
    public void processEvent(ComponentSystemEvent event) throws AbortProcessingException {
        UIComponent previousParent = null;
        UIComponent parent = getParent();
        while (parent != null && !(parent instanceof UINamingContainer) && !(parent instanceof UIForm)) {
            previousParent = parent;
            parent = parent.getParent();
        }

        if (parent != null) {
            UIComponent facet = parent.getFacet(COMPOSITE_FACET_NAME);
            if (facet != null && facet == previousParent) {
                parent = facet;
            }
        }

        if (parent != null) {
            processFormTree(parent);
        }

        super.processEvent(event);
    }

    private void processFormTree(UIComponent container) {
        for (UIComponent component : container.getChildren()) {
            if (component instanceof UICommand) {
                formCommands.add((UICommand) component);
            } else {
                processFormTree(component);
            }
        }
    }

    private UICommand getClickedCommand(FacesContext context, final List<UICommand> commands) {
        final Map<String, String> params = context.getExternalContext().getRequestParameterMap();
        final UICommand[] holder = {null};

        for (String id : params.keySet()) {
            context.getViewRoot().invokeOnComponent(context, id, new ContextCallback() {
                @Override
                public void invokeContextCallback(FacesContext context, UIComponent target) {
                    if (holder[0] == null && target instanceof UICommand && commands.contains((UICommand) target)) {
                        holder[0] = (UICommand) target;
                    }
                }
            });

            if (holder[0] != null) {
                break;
            }
        }

        return holder[0];
    }

    private boolean isCommandClick(FacesContext context, UICommand command) {
        final String clientId = command.getClientId(context);
        if (clientId == null || clientId.isEmpty()) {
            return false;
        }

        final Map<String, String> params = context.getExternalContext().getRequestParameterMap();
        if (params.get(clientId) == null) {
            if (isCommandClickAsPartialOrBehavior(context, clientId)) {
                return true;
            }

            StringBuilder builder = new StringBuilder(clientId);
            String xValue = builder.append(".x").toString();
            builder.setLength(clientId.length());
            String yValue = builder.append(".y").toString();
            return params.get(xValue) != null && params.get(yValue) != null;
        }
        
        return true;
    }

    private boolean isCommandClickAsPartialOrBehavior(FacesContext context, String clientId) {
        Map<String, String> params = context.getExternalContext().getRequestParameterMap();

        if ((clientId == null) || (clientId.length() == 0)) {
            return false;
        }
        if (!clientId.equals(params.get("javax.faces.source"))) {
            return false;
        }

        String behaviorEvent = params.get("javax.faces.behavior.event");
        if (null != behaviorEvent) {
            return ("action".equals(behaviorEvent));
        }

        String partialEvent = params.get("javax.faces.partial.event");
        return ("click".equals(partialEvent));
    }

    private boolean isCommandShouldProcessThis(UICommand command) {
        boolean result = true;

        for (UIComponent component : command.getChildren()) {
            if (component.getClass() == HtmlFormPartTarget.class) {
                result = false;
                HtmlFormPartTarget target = (HtmlFormPartTarget) component;
                if (target.isRendered() && target.getFormPart() != null && target.getFormPart().equals(this.getId())) {
                    result = true;
                    break;
                }
            }
        }

        UIComponent parent = command.getParent();
        while (!result && parent != null) {
            result = parent.getClass() == HtmlFormPartTarget.class
                    && parent.isRendered()
                    && this.getId().equals(((HtmlFormPartTarget) parent).getFormPart());
            
            parent = parent.getParent();
        }

        return result;
    }

    private void processChildValidators(FacesContext context, UIComponent parent) {
        if (parent != null) {
            Iterator<UIComponent> facetsAndChildrenIterator = parent
                    .getFacetsAndChildren();
            while (facetsAndChildrenIterator.hasNext()) {
                UIComponent facetOrChild = facetsAndChildrenIterator.next();
                if (facetOrChild instanceof HtmlFormPart) {
                    HtmlFormPart childHtmlFormPart = (HtmlFormPart) facetOrChild;
                    childHtmlFormPart.processValidators(context);
                } else {
                    facetOrChild.pushComponentToEL(context, null);
                    try {
                        processChildValidators(context, facetOrChild);
                    } finally {
                        facetOrChild.popComponentFromEL(context);
                    }
                }
            }
        }
    }
}
