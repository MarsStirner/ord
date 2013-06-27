package ru.efive.uifaces.renderkit.html_basic.base;

import java.io.IOException;
import java.util.List;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.render.Renderer;

/**
 *
 * @author Denis Kotegov
 */
public class HtmlBasicRenderer extends Renderer {

    /** <code>clientId</code> component property. */
    public static final String CP_CLIENT_ID = "clientId";

    /** <code>value</code> component property. */
    public static final String CP_VALUE = "value";
    
    // TODO: if component class will be replaced by component attribute then should be removed.
    public static final ComponentEvaluator<String> CE_CSS_CLASS = new ComponentEvaluator<String>() {
        @Override
        public String evaluate(FacesContext context, UIComponent component, Object... params) {
            StringBuilder sb = new StringBuilder();

            if (component != null) {
                Object styleClassVal = component.getAttributes().get(ComponentAttribute.STYLE_CLASS);
                if (styleClassVal != null) {
                    String sv = styleClassVal.toString();
                    if (!sv.isEmpty()) {
                        sb.append(sv).append(" ");
                    }
                }
            }
            
            for (Object param : params) {
                if (param != null) {
                    String pv = param.toString();
                    if (!pv.isEmpty()) {
                        sb.append(pv).append(" ");
                    }
                }
            }

            if (sb.length() > 0) {
                sb.setLength(sb.length() - 1);
            }
            
            String result = sb.toString();
            return result.isEmpty() ? null : result;
        }
    };
    
    // ----------------------------------------------------------------------------------------------------------------
    
    protected boolean shouldEncodeComponent(FacesContext context, UIComponent component) throws IOException {
        return component.isRendered();
    }
    
    protected boolean shouldEncodeChildren(FacesContext context, UIComponent component) throws IOException {
        return shouldEncodeComponent(context, component);
    }

    protected boolean shouldEncodeIdAttribute(FacesContext context, UIComponent component) throws IOException {
        return false;
    }
    
    // ----------------------------------------------------------------------------------------------------------------
    
    /**
     * Encodes <code>id</code> HTML attribute if 
     * {@link #shouldEncodeIdAttribute(javax.faces.context.FacesContext, javax.faces.component.UIComponent)}
     * returns <code>true</code>
     * 
     * @param writer advanced response writer.
     * @return <code>true</code> if attribute was rendered and <code>false</code> otherwise.
     * @throws IOException on rendering exceptions.
     */
    protected final boolean encodeIdAttributeIfShould(AdvancedResponseWriter writer) throws IOException {
        final FacesContext context = writer.getContext();
        final UIComponent component = writer.getComponent();
        final boolean result = shouldEncodeIdAttribute(context, component);
        if (result) {
            writer.writeAttribute(HtmlAttribute.ID, component.getClientId(context), CP_CLIENT_ID);
        }
        return result;
    }
    
    // ----------------------------------------------------------------------------------------------------------------
    
    protected void encodeBegin(AdvancedResponseWriter writer) throws IOException {
        // Do Nothing before overriding.
    }

    protected void encodeChildren(AdvancedResponseWriter writer, List<UIComponent> children) throws IOException {
        // Simply encoding children.
        for (UIComponent kid : children) {
            kid.encodeAll(writer.getContext());
        }
    }

    protected void encodeEnd(AdvancedResponseWriter writer) throws IOException {
        // Do Nothing before overriding.
    }
    
    // ----------------------------------------------------------------------------------------------------------------
    
    @Override
    public String convertClientId(FacesContext context, String clientId) {
        return clientId;
    }

    @Override
    public final void encodeBegin(FacesContext context, UIComponent component) throws IOException {
        if (shouldEncodeComponent(context, component)) {
            encodeBegin(new AdvancedResponseWriter(context, component));
        }
    }

    @Override
    public final void encodeChildren(FacesContext context, UIComponent component) throws IOException {
        if (shouldEncodeChildren(context, component)) {
            encodeChildren(new AdvancedResponseWriter(context, component), component.getChildren());
        }
    }

    @Override
    public final void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        if (shouldEncodeComponent(context, component)) {
            final AdvancedResponseWriter writer = new AdvancedResponseWriter(context, component);
            encodeEnd(writer);
            writer.flush();
        }
    }

    @Override
    public boolean getRendersChildren() {
        return true;
    }
}
