package ru.efive.uifaces.renderkit.html_basic.base;

import javax.servlet.http.Cookie;
import java.io.IOException;
import javax.el.ELContext;
import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.component.UIForm;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.context.ResponseWriterWrapper;

import static java.lang.String.format;

/**
 * <code>AdvancedResponseWriter</code> wraps default <code>ResponseWriter</code> provided by default JSF implementor
 * and allows number of custom operations. In fact, this class is one <code>UIComponent</code> rendering
 * process context, which is contains <code>FacesContext</code> and rendered component.
 *
 * @author Denis Kotegov
 */
public class AdvancedResponseWriter extends ResponseWriterWrapper {

    /** Faces Context. */
    private final FacesContext context;

    /** Rendered component. */
    private final UIComponent component;

    /**
     * Constructs advances response writer with given faces context and rendered component.
     * 
     * @param context faces context.
     * @param component  rendered component.
     * @throws NullPointerException if one of given parameters is <code>null</code>
     */
    public AdvancedResponseWriter(final FacesContext context, final UIComponent component) {
        if (context == null || component == null) {
            throw new NullPointerException("All parameters must be non null.");
        }
        
        this.context = context;
        this.component = component;
    }

    // ----------------------------------------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     * @deprecated you should use {@link #writeAttribute(ru.efive.uifaces.renderkit.html_basic.base.HtmlAttribute,
     * java.lang.Object, java.lang.String)} instead.
     */
    @Deprecated
    @Override
    public final void writeAttribute(final String name, final Object value, final String property) throws IOException {
        super.writeAttribute(name, value, property);
    }

    /**
     * {@inheritDoc}
     * @deprecated you should use {@link #startElement(ru.efive.uifaces.renderkit.html_basic.base.HtmlElement)}
     * instead.
     */
    @Override
    @Deprecated
    public final void startElement(final String name, final UIComponent uiComponent) throws IOException {
        super.startElement(name, uiComponent);
    }

    /**
     * {@inheritDoc}
     * @deprecated you should use {@link #endElement(ru.efive.uifaces.renderkit.html_basic.base.HtmlElement)} instead.
     */
    @Deprecated
    @Override
    public final void endElement(final String name) throws IOException {
        super.endElement(name);
    }
    
    // ----------------------------------------------------------------------------------------------------------------

    /**
     * Calling <code>endElement</code> of underlying <code>ResponseWriter</code> for given HTML element value.
     * 
     * @param element HTML element which processing is ends.
     * @see ResponseWriter#endElement(java.lang.String)
     * @throws IOException if an input/output error occurs.
     * @throws NullPointerException if given element or its value is <code>null</code>
     */
    public final void endElement(final HtmlElement element) throws IOException {
        if (element == null) {
            throw new NullPointerException("Element to end is null");
        }
        
        super.endElement(element.getName());
    }
    
    /**
     * Writes an list of HTML attributes with values from same named component attributes.
     * 
     * @param component component which attributes to write
     * @param writer writer used to write attributes
     * @param attributes list of HTML attributes to write.
     * @throws IOException if an input/output error occurs
     */
    public static void passThruAttributes(UIComponent component, AdvancedResponseWriter writer,
            final HtmlAttribute... attributes) throws IOException {
        for (HtmlAttribute attr : attributes) {
            writeComponentAttribute(component, writer, attr, attr.getName(), null);
        }
    }

    /**
     * Writes an list of HTML attributes with values from same named component attributes.
     * 
     * @param component component which attributes to write
     * @param writer writer used to write attributes
     * @param attributes list of HTML attributes to write.
     * @throws IOException if an input/output error occurs
     */
    public static void passThruAttributes(UIComponent component, AdvancedResponseWriter writer,
            final Iterable<HtmlAttribute> attributes) throws IOException {
        for (HtmlAttribute attr : attributes) {
            writeComponentAttribute(component, writer, attr, attr.getName(), null);
        }
    }
    
    /**
     * Writes an list of HTML attributes with values from same named component attributes.
     * 
     * @param attributes list of HTML attributes to write.
     * @throws IOException if an input/output error occurs
     */
    public final void passThruAttributes(final HtmlAttribute... attributes) throws IOException {
        passThruAttributes(component, this, attributes);
    }
    
    /**
     * Writes an list of HTML attributes with values from same named component attributes.
     * 
     * @param attributes collection of HTML attributes to write.
     * @throws IOException if an input/output error occurs
     */
    public final void passThruAttributes(final Iterable<HtmlAttribute> attributes) throws IOException {
        passThruAttributes(component, this, attributes);
    }

    /**
     * Calling <code>startElement</code> of underlying <code>ResponseWriter</code> for given HTML element value.
     * 
     * @param element HTML element which processing is started.
     * @see ResponseWriter#startElement(java.lang.String, javax.faces.component.UIComponent)
     * @throws IOException if an input/output error occurs
     * @throws NullPointerException if given element or its value is <code>null</code>
     */
    public final void startElement(final HtmlElement element) throws IOException {
        if (element == null) {
            throw new NullPointerException("Element to start is null");
        }
        
        super.startElement(element.getName(), component);
    }
    
    /**
     * Calling <code>writeAttribute</code> of underlying <code>ResponseWriter</code> for given HTML attribute and
     * value.
     * 
     * @param htmlAttr HTML attribute to encode.
     * @param value value of attribute to encode.
     * @param componentProperty name of component property which given <code>htmlAttr</code> is corresponds for.
     * @see ResponseWriter#writeAttribute(java.lang.String, java.lang.Object, java.lang.String)
     * @throws IOException if an input/output error occurs
     * @throws NullPointerException if given attribute or its value is <code>null</code>
     */
    public final void writeAttribute(final HtmlAttribute htmlAttr, final Object value, final String componentProperty)
            throws IOException {
        
        if (htmlAttr == null) {
            throw new NullPointerException("Given HTML attribute is null");
        }
        
        super.writeAttribute(htmlAttr.getName(), value, componentProperty);
    }
    
    /**
     * Calling <code>writeAttribute</code> of underlying <code>ResponseWriter</code> for given HTML attribute and
     * value which is evaluated from given component attribute with given.
     * 
     * @param component component which attributes to write
     * @param writer writer used to write attributes
     * @param htmlAttr HTML attribute to encode.
     * @param componentAttr name of component attribute for evaluating value to pass into underlying writer.
     * @param componentProperty name of component property which given <code>htmlAttr</code> is corresponds for.
     * @see ResponseWriter#writeAttribute(java.lang.String, java.lang.Object, java.lang.String)
     * @throws IOException if an input/output error occurs
     * @throws NullPointerException if given HTML attribute or its value or given component attribute is
     * <code>null</code>
     */
    public static void writeComponentAttribute(UIComponent component, AdvancedResponseWriter writer,
            final HtmlAttribute htmlAttr, final String componentAttr,
            final String componentProperty) throws IOException {

        if (htmlAttr == null || componentAttr == null) {
            throw new NullPointerException("One or more of 'htmlAttr' and 'componentAttr' is null");
        }

        writer.writeAttribute(
                htmlAttr, component.getAttributes().get(componentAttr), componentProperty);
    }
    
    /**
     * Calling <code>writeAttribute</code> of underlying <code>ResponseWriter</code> for given HTML attribute and
     * value which is evaluated from given component attribute with given.
     * 
     * @param htmlAttr HTML attribute to encode.
     * @param componentAttr name of component attribute for evaluating value to pass into underlying writer.
     * @param componentProperty name of component property which given <code>htmlAttr</code> is corresponds for.
     * @see ResponseWriter#writeAttribute(java.lang.String, java.lang.Object, java.lang.String)
     * @throws IOException if an input/output error occurs
     * @throws NullPointerException if given HTML attribute or its value or given component attribute is
     * <code>null</code>
     */
    public final void writeComponentAttribute(final HtmlAttribute htmlAttr, final String componentAttr,
            final String componentProperty) throws IOException {
        writeComponentAttribute(component, this, htmlAttr, componentAttr, componentProperty);
    }

    /**
     * Calling <code>writeAttribute</code> of underlying <code>ResponseWriter</code> for given HTML attribute and
     * value which is evaluated by given <code>ComponentEvaluator</code>.
     * 
     * @param <T> type of component evaluator value.
     * @param htmlAttr HTML attribute to encode.
     * @param evaluator written value evaluator.
     * @param componentProperty name of component property which given <code>attribute</code> is corresponds for.
     * @param params number of parameters that all are passed to evaluator.
     * @see ResponseWriter#writeAttribute(java.lang.String, java.lang.Object, java.lang.String)
     * @throws IOException if an input/output error occurs
     * @throws NullPointerException if given HTML attribute or its value or evaluator is
     * <code>null</code>
     */
    public final <T> void writeEvaluatedAttribute(final HtmlAttribute htmlAttr, final ComponentEvaluator<T> evaluator,
            final String componentProperty, final Object... params) throws IOException {
        
        if (htmlAttr == null || evaluator == null) {
            throw new NullPointerException("One or more of 'htmlAttr' and 'evaluator' is null");
        }
        
        super.writeAttribute(htmlAttr.getName(), evaluator.evaluate(context, component, params), componentProperty);
    }
    
    /**
     * Renders facet of specified component if the one is exists and visible.
     * 
     * @param facetName name of facet to render.
     * @param component component which facet to render
     * @return <code>true</code> if facet was rendered and <code>false</code> otherwise.
     * @throws IOException if an input/output error occurs
     */
    public final boolean writeFacet(final String facetName, final UIComponent component) throws IOException {
        UIComponent facet = component.getFacet(facetName);

        if (facet != null && facet.isRendered()) {
            facet.encodeAll(context);
            return true;
        }

        return false;
    }
    
    /**
     * Renders facet if the one is exists and visible.
     * 
     * @param facetName name of facet to render.
     * @return <code>true</code> if facet was rendered and <code>false</code> otherwise.
     * @throws IOException if an input/output error occurs
     */
    public final boolean writeFacet(final String facetName) throws IOException {
        return writeFacet(facetName, component);
    }

    /**
     * Calling <code>writeText</code> of underlying <code>ResponseWriter</code>. Text to write is evaluated from
     * component attribute.
     * 
     * @param componentAttr attribute name to evaluate written text.
     * @param componentProperty name of component property which given <code>attribute</code> is corresponds for.
     * @see ResponseWriter#writeText(java.lang.Object, javax.faces.component.UIComponent, java.lang.String)
     * @throws IOException if an input/output error occurs
     * @throws NullPointerException if <code>componentAttr</code> is <code>null</code>.
     */
    public final void writeAttributeText(final String componentAttr, final String componentProperty)
            throws IOException {
        
        if (componentAttr == null) {
            throw new NullPointerException("componentAttr is null");
        }
        
        super.writeText(component.getAttributes().get(componentAttr), component, componentProperty);
    }
    
    /**
     * Calling <code>writeText</code> of underlying <code>ResponseWriter</code>. Text to write is evaluated by
     * given evaluator.
     *
     * @param <T> type of evaluator's value.
     * @param evaluator evaluator to evaluate written text.
     * @param componentProperty name of component property which given <code>attribute</code> is corresponds for.
     * @param params number of parameters passed to evaluator.
     * @see ResponseWriter#writeText(java.lang.Object, javax.faces.component.UIComponent, java.lang.String)
     * @throws IOException if an input/output error occurs
     * @throws NullPointerException if <code>evaluator</code> is <code>null</code>.
     */
    public final <T> void writeEvaluatedText(final ComponentEvaluator<T> evaluator, final String componentProperty,
            final Object... params) throws IOException {
        
        if (evaluator == null) {
            throw new NullPointerException("Evaluator is null");
        }
        
        super.writeText(evaluator.evaluate(context, component, params), component, componentProperty);
    }
    
    // ----------------------------------------------------------------------------------------------------------------
    
    @Override
    public final ResponseWriter getWrapped() {
        return context.getResponseWriter();
    }

    /**
     * @return faces context for this writer.
     */
    public final FacesContext getContext() {
        return context;
    }

    /**
     * @return currently rendered component.
     */
    public final UIComponent getComponent() {
        return component;
    }

    // ---- component utils ----//

    private static void writeStyleAndClassAttribute(String componentAttribute, HtmlAttribute htmlAttribute,
            UIComponent component, AdvancedResponseWriter writer, String... defaultAttribute)
            throws IOException {
        StringBuilder attribute = new StringBuilder();
        for (String da : defaultAttribute) {
            if (da != null && !da.isEmpty()) {
                attribute.append(da).append(" ");
            }
        }
        String cav = component != null
                ? (String) component.getAttributes().get(componentAttribute) : null;
        if (cav != null && !cav.isEmpty()) {
            attribute.append(cav);
        } else if (attribute.length() > 0) {
            attribute.setLength(attribute.length() - 1);
        }
        if (attribute.length() > 0) {
            writer.writeAttribute(htmlAttribute, attribute.toString(), null);
        }
    }

    public static void writeStyleClass(UIComponent component, AdvancedResponseWriter writer, String... defaultClass)
            throws IOException {
        writeStyleAndClassAttribute(ComponentAttribute.STYLE_CLASS, HtmlAttribute.CLASS, component, writer, defaultClass);
    }

    public static void writeStyle(UIComponent component, AdvancedResponseWriter writer, String... defaultStyle)
            throws IOException {
        writeStyleAndClassAttribute(ComponentAttribute.STYLE, HtmlAttribute.STYLE, component, writer, defaultStyle);
    }

    public static boolean equals(Object o1, Object o2) {
        return o1 == o2 || o1 != null && o1.equals(o2);
    }

    public static void updateComponentAttribute(FacesContext context, UIComponent component,
            String attrubute, Object value) {
        ValueExpression ve = component.getValueExpression(attrubute);
        if (ve != null) {
            ELContext elCtx = context.getELContext();
            Object oldValue = ve.getValue(elCtx);
            if (!equals(value, oldValue)) {
                ve.setValue(elCtx, value);
            }
        }
    }

    private static final String NO_FORM = "There are no enclosing form for %s component with clientId %s";

    public static UIComponent findEnclosingForm(UIComponent childComponent) {
        UIComponent parent = childComponent;
        for (;;) {
            parent = parent.getParent();
            if (parent == null) {
                throw new IllegalArgumentException(format(NO_FORM, childComponent.getClass().getName(), childComponent.
                        getClientId()));
            } else if (parent instanceof UIForm) {
                return parent;
            }
        }
    }

    // ---- HTTP Utils ---- //

    /**
     * Returns cookie with specified name
     * @param cookieName
     * @return cookie's value
     */
    public static Cookie getCookie(String cookieName) {
        return (Cookie) FacesContext.getCurrentInstance().getExternalContext().getRequestCookieMap().get(cookieName);
    }
}
