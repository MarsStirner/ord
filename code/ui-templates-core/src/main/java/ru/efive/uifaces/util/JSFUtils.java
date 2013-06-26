package ru.efive.uifaces.util;

import javax.faces.FactoryFinder;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextFactory;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Utils for working with JSF.
 *
 * @author Pavel Porubov
 */
public final class JSFUtils {

    /**
     * Constructs or acuires {@link FacesContext}.
     * @param request
     * @param response
     * @return current instance of {@link FacesContext}
     */
    public static FacesContext getFacesContext(HttpServletRequest request, HttpServletResponse response) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext == null) {
            FacesContextFactory contextFactory =
                    (FacesContextFactory) FactoryFinder.getFactory(FactoryFinder.FACES_CONTEXT_FACTORY);
            LifecycleFactory lifecycleFactory =
                    (LifecycleFactory) FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
            Lifecycle lifecycle = lifecycleFactory.getLifecycle(LifecycleFactory.DEFAULT_LIFECYCLE);
            facesContext = contextFactory.getFacesContext(request.getServletContext(),
                    request, response, lifecycle);
            InnerFacesContext.setFacesContextAsCurrentInstance(facesContext);
            UIViewRoot view = facesContext.getApplication().getViewHandler().createView(facesContext, "");
            facesContext.setViewRoot(view);
        }
        return facesContext;
    }

    /**
     * Evaluates the expression with specified {@link FacesContext}.
     * @param expression expression to evaluate. Should contain only the expression self with
     * no <code>"#{"</code> or <code>"}"</code> around.
     * @param facesContext {@link FacesContext} to be used to evaluate the expression
     * @return evaluated expression's value
     */
    public static Object getExpressionValue(String expression, FacesContext facesContext) {
        return facesContext.getApplication().getELResolver().getValue(facesContext.getELContext(), null, expression);
    }

    /**
     * Acquires managed bean with specified name from specified {@link FacesContext}.
     * @param beanName the name of required bean
     * @param facesContext {@link FacesContext} used for aquiring of bean.
     * @return the reference to managed bean with specified name
     */
    public static Object getManagedBean(String beanName, FacesContext facesContext) {
        return getExpressionValue(beanName, facesContext);
    }

    private abstract static class InnerFacesContext extends FacesContext {

        protected static void setFacesContextAsCurrentInstance(FacesContext facesContext) {
            FacesContext.setCurrentInstance(facesContext);
        }
    }
}
