package ru.efive.uifaces.renderkit.html_basic.base;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

/**
 *
 * @author Denis Kotegov
 */
public interface ComponentEvaluator<T> {
    
    T evaluate(FacesContext context, UIComponent component, Object... params);
    
}
