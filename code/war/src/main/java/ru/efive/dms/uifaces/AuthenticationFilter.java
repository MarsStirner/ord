package ru.efive.dms.uifaces;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ru.efive.dms.uifaces.beans.SessionManagementBean;


public class AuthenticationFilter implements Filter {


    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        if (((HttpServletRequest) req).getSession().getAttribute(SessionManagementBean.AUTH_KEY) == null) {
            if ((req instanceof HttpServletRequest) && isSessionControlRequiredForThisResource((HttpServletRequest) req)) {
                String requestUrl = "";
                if (req instanceof HttpServletRequest) {
                    String uri = ((HttpServletRequest) req).getRequestURL().toString();
                    String queryString = ((HttpServletRequest) req).getQueryString();
                    int pos = StringUtils.indexOf(uri, "/component/");
                    if (pos != -1 && StringUtils.containsNone(queryString, "cid=")) {
                        uri = StringUtils.right(uri, StringUtils.length(uri) - pos);
                        if (StringUtils.isNotEmpty(uri)) {
                            requestUrl = uri;
                        }
                        if (StringUtils.isNotEmpty(requestUrl)) {
                            requestUrl = requestUrl + "?" + queryString;
                        }
                        System.out.println("requestUrl> " + requestUrl);
                    }
                }
                if (StringUtils.isNotEmpty(requestUrl)) {
                    System.out.println("Setting requestUrl session parameter: " + requestUrl);
                    ((HttpServletRequest) req).getSession().setAttribute(SessionManagementBean.BACK_URL, requestUrl);
                }
                ((HttpServletResponse) resp).sendRedirect(((HttpServletRequest) req).getContextPath() + "/" + getTimeoutPage());
            }
        } else {
            chain.doFilter(req, resp);
        }
    }

    private boolean isSessionControlRequiredForThisResource(HttpServletRequest httpServletRequest) {
        String requestPath = httpServletRequest.getRequestURI();
        boolean controlRequired = !StringUtils.contains(requestPath, getTimeoutPage());
        return controlRequired;
    }

    public void init(FilterConfig config) throws ServletException {
        this.config = config;
    }

    public void destroy() {
        config = null;
    }

    public String getTimeoutPage() {
        return timeoutPage;
    }

    public void setTimeoutPage(String timeoutPage) {
        this.timeoutPage = timeoutPage;
    }


    private String timeoutPage = "index.xhtml";

    private FilterConfig config;

    private final static Logger logger = Logger.getLogger(AuthenticationFilter.class);
}