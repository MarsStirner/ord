package ru.efive.uifaces.filter;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import ru.efive.uifaces.filter.util.ModifyHeadersHttpServletRequestWrapper;

/**
 * The filter controls of caching of resources by generating or suppressing cache-control HTTP-headers.
 * <br>The filter is configured by specifying of init parameters:
 * <ul>
 * <li>{@code QUERY_STRING_PATTERN} - the pattern for resource's URL than should be
 * processed by the filter. By default is {@link ResourceCacheControlFilter#DEFAULT_QUERY_STRING_PATTERN};</li>
 * <li>{@code FORCE_NO_CACHE} - if exists and equals true then the filter forces for all URLs to be
 * processed that they shouldn't be cached. By default is false.</li>
 * </ul>
 * When a browser requests a resource it usually specifies last update time for that resource. The filter compares
 * that time with the project build time. If browser's time younger than project's time the filter forces the resource
 * to be updated. Otherwise the filter leaves the request with no change.
 * <br>The filter assumes that the project build time is stored in {@code /META-INF/version.properties} file in
 * the property with name {@code build.time} in the {@code "yyyy-MM-dd HH:mm:ss.SSS"} format.
 *
 * @author Pavel Porubov
 */
public class ResourceCacheControlFilter extends AbstractFilter {

    /**
     * Init parameter's name.
     */
    public static final String QUERY_STRING_PATTERN_PARAMETER = "QUERY_STRING_PATTERN";
    /**
     * The default value of {@code QUERY_STRING_PATTERN} init parameter. Equals to ".*".
     */
    public static final String DEFAULT_QUERY_STRING_PATTERN = ".*";
    /**
     * Init parameter's name.
     */
    public static final String FORCE_NO_CACHE_PARAMETER = "FORCE_NO_CACHE";
    /**
     * Determined build time. If there are no {@code /META-INF/version.properties} file or it doesn't contain
     * {@code build.time} property then equals the class's load time.
     */
    public static final Date BUILD_TIME;
    static {
        Properties properties = new Properties();
        try {
            properties.load(ResourceCacheControlFilter.class.getResourceAsStream("/META-INF/version.properties"));
        } catch (IOException ex) {
            Logger.getLogger(ResourceCacheControlFilter.class.getName()).log(Level.WARNING, null, ex);
        }
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String bts = properties.getProperty("build.time");
        Date bt = null;
        if (bts != null) {
            try {
                bt = df.parse(bts);
            } catch (ParseException ex) {
                Logger.getLogger(ResourceCacheControlFilter.class.getName()).log(Level.WARNING, null, ex);
            }
        }
        BUILD_TIME = bt != null ? bt : new Date();
    }
    public static final String HTTP_DATE = "EEE, dd MMM yyyy HH:mm:ss zzz";
    private Pattern queryStringPattern;
    private boolean forceNoCache;

    /**
     * Constructs uninitialized filter.
     */
    public ResourceCacheControlFilter() {
    }

    private static final String IF_MODIFIED_SINCE_HEADER = "if-modified-since";
    private static final String CACHE_CONTROL_HEADER = "cache-control";
    private static final String PRAGMA_HEADER = "pragma";
    private static final String NO_CACHE = "no-cache";

    /** {@inheritDoc} */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            boolean forceUpdate = false;
            HttpServletRequest hreq = (HttpServletRequest) request;

            String queryString = hreq.getQueryString();
            if (queryStringPattern.matcher(queryString != null ? queryString : "").matches()) {
                if (!forceNoCache) {
                    String imh = hreq.getHeader(IF_MODIFIED_SINCE_HEADER);
                    if (imh != null) {
                        try {
                            forceUpdate = BUILD_TIME.compareTo(new SimpleDateFormat(HTTP_DATE, Locale.US).parse(imh))
                                    >= 0;
                        } catch (ParseException ex) {
                            forceUpdate = true;
                        }
                    }
                } else {
                    forceUpdate = true;
                }
            }

            if (forceUpdate) {
                HttpServletRequest hreqW = new ModifyHeadersHttpServletRequestWrapper(
                        new String[]{IF_MODIFIED_SINCE_HEADER, CACHE_CONTROL_HEADER, PRAGMA_HEADER},
                        new String[]{CACHE_CONTROL_HEADER, NO_CACHE, PRAGMA_HEADER, NO_CACHE},
                        hreq);


                chain.doFilter(hreqW, response);
            } else {
                chain.doFilter(request, response);
            }
        } catch (Throwable t) {
            if (t instanceof ServletException) {
                throw (ServletException) t;
            }
            if (t instanceof IOException) {
                throw (IOException) t;
            }
            sendProcessingError(t, response);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void init(FilterConfig filterConfig) {
        super.init(filterConfig);
        if (filterConfig != null) {
            try {
                queryStringPattern = Pattern.compile(filterConfig.getInitParameter(QUERY_STRING_PATTERN_PARAMETER));
            } catch (PatternSyntaxException ex) {
            }
            forceNoCache = Boolean.parseBoolean(filterConfig.getInitParameter(FORCE_NO_CACHE_PARAMETER));
        }
        if (queryStringPattern == null) {
            queryStringPattern = Pattern.compile(DEFAULT_QUERY_STRING_PATTERN);
        }
    }
}