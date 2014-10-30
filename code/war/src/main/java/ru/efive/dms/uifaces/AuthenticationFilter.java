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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.efive.dms.uifaces.beans.SessionManagementBean;


public class AuthenticationFilter implements Filter {
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest request = (HttpServletRequest) req;
        if (request.getSession().getAttribute(SessionManagementBean.AUTH_KEY) == null) {
            if (isSessionControlRequiredForThisResource(request)) {
                String requestUrl = "";
                String uri = request.getRequestURL().toString();
                String queryString = request.getQueryString();
                int pos = StringUtils.indexOf(uri, "/component/");
                if (pos != -1 && (queryString!= null &&!queryString.contains("cid="))) {
                    uri = StringUtils.right(uri, uri.length() - pos);
                    if (StringUtils.isNotEmpty(uri)) {
                        requestUrl = uri;
                    }
                    if (StringUtils.isNotEmpty(requestUrl)) {
                        requestUrl = requestUrl + "?" + queryString;
                    }
                    LOGGER.info("AUTH: requestUrl={}", requestUrl);
                }
                if (StringUtils.isNotEmpty(requestUrl)) {
                    LOGGER.info("AUTH: Setting requestUrl session parameter: {} ", requestUrl);
                    request.getSession().setAttribute(SessionManagementBean.BACK_URL, requestUrl);
                }
                ((HttpServletResponse) resp).sendRedirect(request.getContextPath() + "/" + getTimeoutPage());
            }
        } else {
            chain.doFilter(req, resp);
        }
    }

    private boolean isSessionControlRequiredForThisResource(HttpServletRequest httpServletRequest) {
        String requestPath = httpServletRequest.getRequestURI();
        return !StringUtils.contains(requestPath, getTimeoutPage());
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

    private final static Logger LOGGER = LoggerFactory.getLogger("FILTER");
}