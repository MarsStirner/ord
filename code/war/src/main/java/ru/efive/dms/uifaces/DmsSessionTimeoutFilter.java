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

public class DmsSessionTimeoutFilter implements Filter {

    public void init(FilterConfig filterConfig) throws ServletException {

    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        if ((request instanceof HttpServletRequest) && (response instanceof HttpServletResponse)) {
            HttpServletRequest httpServletRequest = (HttpServletRequest) request;
            HttpServletResponse httpServletResponse = (HttpServletResponse) response;
            if (isSessionControlRequiredForThisResource(httpServletRequest)) {
                if (isSessionInvalid(httpServletRequest)) {
                    String requestUrl = "";
                    if (request instanceof HttpServletRequest) {
                        String uri = ((HttpServletRequest) request).getRequestURL().toString();
                        String queryString = ((HttpServletRequest) request).getQueryString();
                        int pos = StringUtils.indexOf(uri, "/component/");
                        if (pos != -1 && StringUtils.containsNone(queryString, "cid=")) {
                            uri = StringUtils.right(uri, StringUtils.length(uri) - pos);
                            if (StringUtils.isNotEmpty(uri)) {
                                requestUrl = uri;
                            }
                            if (StringUtils.isNotEmpty(queryString)) {
                                requestUrl = requestUrl + "?" + queryString;
                            }
                            System.out.println("requestUrl> " + requestUrl);
                        }
                    }
                    if (StringUtils.isNotEmpty(requestUrl)) {
                        System.out.println("Setting requestUrl session parameter: " + requestUrl);
                        ((HttpServletRequest) request).getSession().setAttribute(SessionManagementBean.BACK_URL, requestUrl);
                    }
                    httpServletResponse.sendRedirect(httpServletRequest.getContextPath() + "/" + getTimeoutPage());
                    return;
                }
            }
        }
        filterChain.doFilter(request, response);

    }

    private boolean isSessionControlRequiredForThisResource(HttpServletRequest httpServletRequest) {
        String requestPath = httpServletRequest.getRequestURI();
        boolean controlRequired = !StringUtils.contains(requestPath, getTimeoutPage());
        return controlRequired;
    }

    private boolean isSessionInvalid(HttpServletRequest httpServletRequest) {
        boolean sessionInValid = (httpServletRequest.getRequestedSessionId() != null) && !httpServletRequest.isRequestedSessionIdValid();
        return sessionInValid;
    }

    public void destroy() {

    }

    public String getTimeoutPage() {
        return timeoutPage;
    }

    public void setTimeoutPage(String timeoutPage) {
        this.timeoutPage = timeoutPage;
    }


    private String timeoutPage = "index.xhtml";

    private final static Logger logger = LoggerFactory.getLogger(DmsSessionTimeoutFilter.class);
}