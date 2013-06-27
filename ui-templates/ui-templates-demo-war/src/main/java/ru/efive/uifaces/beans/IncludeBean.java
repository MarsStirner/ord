package ru.efive.uifaces.beans;

import java.io.IOException;
import java.util.Date;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

/**
 *
 *
 * @author Pavel Porubov
 */
@ManagedBean(name = "includeData")
@SessionScoped
public class IncludeBean {

    public String getTimestamp() {
        return new Date().toString();
    }

    private String pageText = "<?xml version='1.0' encoding='UTF-8' ?>\n"
            + "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n"
            + "<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:h=\"http://java.sun.com/jsf/html\">\n"
            + "  <div>\n"
            + "    <h2>Hello from included page</h2>\n"
            + "    <h:panelGroup>\n"
            + "      <div>Current timestapm: #{includeData.timestamp}</div>\n"
            + "    </h:panelGroup>\n"
            + "  </div>\n"
            + "</html>";

    public String getPageText() {
        return pageText;
    }

    public void setPageText(String pageText) {
        this.pageText = pageText;
    }

    public String update() throws IOException {
        FacesContext fctx = FacesContext.getCurrentInstance();
        ExternalContext ectx = fctx.getExternalContext();
        ectx.redirect(ectx.getRequestContextPath() + "/" + fctx.getViewRoot().getViewId());
        return null;
    }
}
