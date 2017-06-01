package ru.efive.dms.config;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import ru.efive.dms.uifaces.beans.utils.SessionManagementBean;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;


public class AuthenticationFilter implements Filter {
    private static final String LOGIN_PAGE = "index.xhtml";
    private static final Logger LOGGER = LoggerFactory.getLogger("ACCESS");
    private static final AtomicLong counter = new AtomicLong();

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

    private static String buildFullURL(final HttpServletRequest x) {
        return x.getScheme() + "://" + x.getServerName() + ":" + x.getServerPort() + x.getRequestURI() + (x.getQueryString() != null ? "?" + x.getQueryString() : "");
    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest request = (HttpServletRequest) req;
        final long startTime = System.nanoTime();
        final String url = request.getRequestURL().append(request.getQueryString() != null ? "?" + request.getQueryString() : "").toString();
        MDC.clear();
        MDC.put("requestNumber", String.valueOf(counter.incrementAndGet()));
        MDC.put("sessionId", request.getSession().getId());
        MDC.put("method", request.getMethod());
        MDC.put("URL", url );
        LOGGER.info("[{}][{}] start processing request", request.getMethod(), url);
        //1 Проверяем требуется ли наличие контроля сессисии (по ходу он не нужен только для страницы логина =))
        if (isSessionControlRequiredForThisResource(request)) {
            //Проверяем авторизацию
            if (request.getSession().getAttribute(SessionManagementBean.AUTH_KEY) == null) {
                //ее нет
                LOGGER.error("{} >>  has not been authorized", getClientIpAddr(request));
                makeRedirect(request, resp);
            } else if (request.getRequestedSessionId() != null && !request.isRequestedSessionIdValid()) {
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
        LOGGER.info("[{}][{}] end in [{}] ms.", request.getMethod(), url, (System.nanoTime() - startTime) / 1000000);
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
        return !StringUtils.contains(httpServletRequest.getRequestURI(), LOGIN_PAGE);
    }


    @Override
    public void destroy() {
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }
}