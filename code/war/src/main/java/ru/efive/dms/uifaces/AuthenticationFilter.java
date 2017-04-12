package ru.efive.dms.uifaces;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.efive.dms.uifaces.beans.SessionManagementBean;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class AuthenticationFilter implements Filter {
    private static final String LOGIN_PAGE = "index.xhtml";
    private static final Logger LOGGER = LoggerFactory.getLogger("FILTER");

    private static String getClientIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest request = (HttpServletRequest) req;
        //1 Проверяем требуется ли наличие контроля сессисии (по ходу он не нужен только для страницы логина =))
        if (isSessionControlRequiredForThisResource(request)) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[{}] {} || {}://{}{}{}{}",
                        request.getSession().getId(),
                        request.getMethod(),
                        request.getScheme(),
                        request.getServerName(),
                        ("http".equals(request.getScheme()) && request.getServerPort() == 80 || "https".equals(request.getScheme()) && request.getServerPort() == 443 ? "" : ":" + request.getServerPort()),
                        request.getRequestURI(),
                        (request.getQueryString() != null ? "?" + request.getQueryString() : "")
                );
            }
            //Проверяем авторизацию
            if (request.getSession().getAttribute(SessionManagementBean.AUTH_KEY) == null) {
                //ее нет
                LOGGER.error("{} >>  has not been authorized", getClientIpAddr(request));
                makeRedirect(request, resp);
            } else if (isSessionInvalid(request)) {
                //Проверяем валидность сессии
                LOGGER.error("{} >>  has invalid session [{}]", getClientIpAddr(request), request.getRequestedSessionId());
                makeRedirect(request, resp);
            } else {
                //Все ок
                chain.doFilter(request, resp);
            }

        } else {
            //Иначе пропускаем дальше
            chain.doFilter(request, resp);
        }
    }

    private void makeRedirect(HttpServletRequest request, ServletResponse resp) throws IOException {
        final String requestPath = request.getRequestURI();
        final String queryPart = request.getQueryString();
        LOGGER.warn("Requested URL is \"{}\" params \"{}\"", requestPath, queryPart);
        final StringBuilder redirectTo = new StringBuilder(requestPath);
        if (queryPart != null && !queryPart.contains("cid=")) {
            redirectTo.append('?').append(queryPart);
        }
        if (requestPath.contains("/component/")) {
            request.getSession().setAttribute(SessionManagementBean.BACK_URL, redirectTo.toString());
        }
        ((HttpServletResponse) resp).sendRedirect(request.getContextPath().concat("/").concat(LOGIN_PAGE));
    }

    private boolean isSessionControlRequiredForThisResource(HttpServletRequest httpServletRequest) {
        String requestPath = httpServletRequest.getRequestURI();
        return !StringUtils.contains(requestPath, LOGIN_PAGE);
    }

    private boolean isSessionInvalid(HttpServletRequest httpServletRequest) {
        return (httpServletRequest.getRequestedSessionId() != null) && !httpServletRequest.isRequestedSessionIdValid();
    }

    @Override
    public void destroy() {
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }
}