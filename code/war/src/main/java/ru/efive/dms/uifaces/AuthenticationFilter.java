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
        //1 Проверяем требуется ли наличие контроля сессисии (по ходу он не нужен только для страницы логина =))
        if(isSessionControlRequiredForThisResource(request)){
            //Проверяем авторизацию
            if (request.getSession().getAttribute(SessionManagementBean.AUTH_KEY) == null) {
               //ее нет
                LOGGER.error("{} >>  has not been authorized", getClientIpAddr(request));
                makeRedirect(request, resp);
            } else if (isSessionInvalid(request)){
                //Проверяем валидность сессии
                LOGGER.error("{} >>  has invalid session [{}]", getClientIpAddr(request),request.getRequestedSessionId());
                makeRedirect(request, resp);
            } else {
                //Все ок
                chain.doFilter(request, resp);
            }

        }   else {
            //Иначе пропускаем дальше
            chain.doFilter(request, resp);
        }
    }

    private void makeRedirect(HttpServletRequest request, ServletResponse resp) throws IOException {
        final String requestPath = request.getRequestURI();
        final String queryPart = request.getQueryString();
        LOGGER.warn("Requested URL is \"{}\" params \"{}\"", requestPath, queryPart);
        final StringBuilder redirectTo = new StringBuilder(requestPath);
        if(queryPart != null && !queryPart.contains("cid=")){
            redirectTo.append('?').append(queryPart);
        }
        request.getSession().setAttribute(SessionManagementBean.BACK_URL, redirectTo.toString());
        HttpServletResponse httpServletResponse = (HttpServletResponse) resp;
        ((HttpServletResponse) resp).sendRedirect(request.getContextPath().concat("/").concat(LOGIN_PAGE));
    }

    private boolean isSessionControlRequiredForThisResource(HttpServletRequest httpServletRequest) {
        String requestPath = httpServletRequest.getRequestURI();
        return !StringUtils.contains(requestPath, LOGIN_PAGE);
    }

    private boolean isSessionInvalid(HttpServletRequest httpServletRequest) {
        return (httpServletRequest.getRequestedSessionId() != null) && !httpServletRequest.isRequestedSessionIdValid();
    }


    public static String getClientIpAddr(HttpServletRequest request) {
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

    @Override
    public void destroy() {
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    private static final String LOGIN_PAGE = "index.xhtml";

    private static final Logger LOGGER = LoggerFactory.getLogger("FILTER");
}