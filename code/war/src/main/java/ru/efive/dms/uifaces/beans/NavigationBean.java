package ru.efive.dms.uifaces.beans;

import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;

@Named("navigationBean")
@RequestScoped
public class NavigationBean {

    public void navigate(String page) {
        FacesContext context = FacesContext.getCurrentInstance();
        Object obj1 = context.getExternalContext().getResponse();
        Object obj2 = context.getExternalContext().getRequest();
        if ((obj1 instanceof HttpServletResponse) && (obj2 instanceof HttpServletRequest)) {
            HttpServletResponse response = (HttpServletResponse) obj1;
            HttpServletRequest request = (HttpServletRequest) obj2;
            try {
                StringBuffer url = new StringBuffer();
                url.append("/");
                url.append(page);
                Enumeration<String> paramNames = request.getParameterNames();
                if(paramNames.hasMoreElements()) {
                    url.append("?");
                }
                while (paramNames.hasMoreElements()) {
                    String param = paramNames.nextElement();
                    url.append(param);
                    url.append("=");
                    url.append(request.getParameter(param));
                    if(paramNames.hasMoreElements()) {
                        url.append("&");
                    }
                }
                response.sendRedirect(response.encodeURL(url.toString()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
