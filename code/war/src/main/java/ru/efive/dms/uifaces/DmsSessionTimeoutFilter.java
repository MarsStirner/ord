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

    public void doFilter(ServletRequest req, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        final HttpServletRequest request = (HttpServletRequest) req;
        if (response instanceof HttpServletResponse) {
            HttpServletResponse httpServletResponse = (HttpServletResponse) response;
            if (isSessionControlRequiredForThisResource(request)) {
                if (isSessionInvalid(request)) {
                    String requestUrl = "";
                    String uri = request.getRequestURL().toString();
                    String queryString = request.getQueryString();
                    int pos = StringUtils.indexOf(uri, "/component/");
                    if (pos != -1 && StringUtils.containsNone(queryString, "cid=")) {
                        uri = StringUtils.right(uri, StringUtils.length(uri) - pos);
                        if (StringUtils.isNotEmpty(uri)) {
                            requestUrl = uri;
                        }
                        if (StringUtils.isNotEmpty(queryString)) {
                            requestUrl = requestUrl + "?" + queryString;
                        }
                        LOGGER.info("TIMEOUT: requestUrl={}", requestUrl);
                    }
                    if (StringUtils.isNotEmpty(requestUrl)) {
                        LOGGER.info("TIMEOUT: Setting requestUrl session parameter: {} ", requestUrl);
                        request.getSession().setAttribute(SessionManagementBean.BACK_URL, requestUrl);
                    }
                    httpServletResponse.sendRedirect(request.getContextPath() + "/" + getTimeoutPage());
                    return;
                }
            }
        }
        filterChain.doFilter(request, response);

    }

    private boolean isSessionControlRequiredForThisResource(HttpServletRequest httpServletRequest) {
        String requestPath = httpServletRequest.getRequestURI();
        return !StringUtils.contains(requestPath, getTimeoutPage());

    }

    private boolean isSessionInvalid(HttpServletRequest httpServletRequest) {
        return (httpServletRequest.getRequestedSessionId() != null) && !httpServletRequest.isRequestedSessionIdValid();
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

    private final static Logger LOGGER = LoggerFactory.getLogger("FILTER");
}